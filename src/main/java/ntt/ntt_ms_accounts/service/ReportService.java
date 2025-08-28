package ntt.ntt_ms_accounts.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ntt.ntt_ms_accounts.dto.CustomerDailyAverageReport;
import ntt.ntt_ms_accounts.dto.ProductDailyAverage;
import ntt.ntt_ms_accounts.dto.CommissionItem;
import ntt.ntt_ms_accounts.dto.CommissionReport;
import ntt.ntt_ms_accounts.models.Account;
import ntt.ntt_ms_accounts.models.AccountMovement;
import ntt.ntt_ms_accounts.models.MovementType;
import ntt.ntt_ms_accounts.repository.AccountMovementRepository;
import ntt.ntt_ms_accounts.repository.AccountRepository;
import ntt.ntt_ms_accounts.repository.CreditCardRepository;
import ntt.ntt_ms_accounts.repository.LoanRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {
    private final AccountRepository accountRepo;
    private final AccountMovementRepository movementRepo;
    private final CreditCardRepository creditCardRepo;
    private final LoanRepository loanRepo;

    // REPORTE 1: Saldos promedio diarios (mes en curso) por cliente
    public Mono<CustomerDailyAverageReport> dailyAveragesForCustomerCurrentMonth(String customerId, ZoneId zone) {
        final LocalDate today = LocalDate.now(zone);
        final LocalDate first = today.withDayOfMonth(1);
        final Instant fromInstant = first.atStartOfDay(zone).toInstant();
        final Instant toInstant = today.plusDays(1).atStartOfDay(zone).toInstant().minusMillis(1);
        final int days = (int) (today.toEpochDay() - first.toEpochDay() + 1);
        final String monthLabel = "%d-%02d".formatted(today.getYear(), today.getMonthValue());

        // A) Accounts → reconstruir saldo diario con movimientos del mes
        Mono<List<ProductDailyAverage>> accountPart = accountRepo.findByCustomerId(customerId)
                .flatMap(acc -> movementRepo
                        .findByAccountIdAndDateBetweenOrderByDateDesc(acc.getId(), fromInstant, toInstant)
                        .collectList()
                        .map(movs -> computeAccountDailyAverage(acc, movs, first, today, zone)))
                .collectList();

        // B) Credit Cards → aproximación con available actual
        Mono<List<ProductDailyAverage>> cardPart = creditCardRepo.findByCustomerId(customerId)
                .map(cc -> new ProductDailyAverage(
                        "CREDIT_CARD", cc.getId(), cc.getCardNumber(), nvl(cc.getAvailable())))
                .collectList();

        // C) Loans → aproximación con remaining actual
        Mono<List<ProductDailyAverage>> loanPart = loanRepo.findByCustomerId(customerId)
                .map(ln -> new ProductDailyAverage("LOAN", ln.getId(), null, nvl(ln.getRemaining())))
                .collectList();

        return Mono.zip(accountPart, cardPart, loanPart)
                .map(t -> {
                    List<ProductDailyAverage> all = Stream.of(t.getT1(), t.getT2(), t.getT3())
                            .flatMap(List::stream).toList();
                    BigDecimal total = all.stream()
                            .map(ProductDailyAverage::dailyAverage)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new CustomerDailyAverageReport(
                            customerId, monthLabel, first, today, days, all, total
                    );
                });
    }

    private ProductDailyAverage computeAccountDailyAverage(Account acc,
                                                           List<AccountMovement> monthMovs,
                                                           LocalDate fromDate,
                                                           LocalDate toDate,
                                                           ZoneId zone) {
        // sumatoria firmada del mes (DEPOSIT=+, WITHDRAWAL=-)
        final BigDecimal sumSignedMonth = monthMovs.stream()
                .map(m -> m.getType() == MovementType.DEPOSIT ? nvl(m.getAmount()) : nvl(m.getAmount()).negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final BigDecimal endBalance = nvl(acc.getBalance());
        final BigDecimal startBalance = endBalance.subtract(sumSignedMonth);

        // cambios por día (yyyy-MM-dd) -> sumatoria firmada del día
        final Map<LocalDate, BigDecimal> deltaByDay = monthMovs.stream()
                .collect(Collectors.groupingBy(
                        m -> LocalDateTime.ofInstant(m.getDate(), zone).toLocalDate(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                m -> m.getType() == MovementType.DEPOSIT ? nvl(m.getAmount()) : nvl(m.getAmount()).negate(),
                                BigDecimal::add
                        )
                ));

        BigDecimal running = startBalance;
        BigDecimal sumEod = BigDecimal.ZERO;
        for (LocalDate d = fromDate; !d.isAfter(toDate); d = d.plusDays(1)) {
            running = running.add(deltaByDay.getOrDefault(d, BigDecimal.ZERO));
            sumEod = sumEod.add(running);
        }

        final int days = (int) (toDate.toEpochDay() - fromDate.toEpochDay() + 1);
        final BigDecimal avg = days == 0
                ? BigDecimal.ZERO
                : sumEod.divide(BigDecimal.valueOf(days), MathContext.DECIMAL64);

        return new ProductDailyAverage("ACCOUNT", acc.getId(), acc.getAccountNumber(), avg);
    }

    // REPORTE 2: Comisiones cobradas por producto en un periodo
    public Mono<CommissionReport> commissionsByProduct(Instant from, Instant to) {
        // 1) Movimientos de cuenta en el rango
        Mono<List<AccountMovement>> accountMovs = movementRepo.findByDateBetween(from, to)
                .collectList();

        // 2) Join en memoria para obtener accountNumber
        Mono<Map<String, Account>> accountsById = accountRepo.findAll()
                .collectMap(Account::getId, a -> a);

        return Mono.zip(accountMovs, accountsById)
                .map(t -> {
                    List<AccountMovement> movs = t.getT1();
                    Map<String, Account> accounts = t.getT2();

                    List<AccountMovement> feeMovs = movs.stream()
                            .filter(m -> m.getType() == MovementType.COMMISSION
                                    || Optional.ofNullable(m.getReference()).orElse("")
                                    .toUpperCase().matches(".*(FEE|COMMISSION|COMISION).*"))
                            .toList();

                    // Grouping: (accountId, commissionType) -> total
                    Map<List<String>, BigDecimal> totals = feeMovs.stream()
                            .collect(Collectors.groupingBy(
                                    m -> List.of(m.getAccountId(),
                                            Optional.ofNullable(m.getReference()).orElse("FEE")),
                                    Collectors.reducing(BigDecimal.ZERO, m -> nvl(m.getAmount()), BigDecimal::add)
                            ));

                    // Map a DTOs
                    List<CommissionItem> items = totals.entrySet().stream()
                            .map(e -> {
                                String accountId = e.getKey().get(0);
                                String commissionType = e.getKey().get(1);
                                Account acc = accounts.get(accountId);
                                return new CommissionItem(
                                        "ACCOUNT",
                                        accountId,
                                        acc != null ? acc.getAccountNumber() : null,
                                        commissionType,
                                        e.getValue()
                                );
                            })
                            .toList();

                    BigDecimal grand = items.stream()
                            .map(CommissionItem::totalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return new CommissionReport(from, to, items, grand);
                });
    }

    private static BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

}
