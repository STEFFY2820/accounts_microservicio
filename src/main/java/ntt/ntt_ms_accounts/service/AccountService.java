package ntt.ntt_ms_accounts.service;

import lombok.RequiredArgsConstructor;
import ntt.ntt_ms_accounts.client.CustomerClient;
import ntt.ntt_ms_accounts.models.Account;
import ntt.ntt_ms_accounts.models.AccountMovement;
import ntt.ntt_ms_accounts.models.AccountStatus;
import ntt.ntt_ms_accounts.models.MovementType;
import ntt.ntt_ms_accounts.models.AccountType;
import ntt.ntt_ms_accounts.models.CustomerSubType;
import ntt.ntt_ms_accounts.models.CustomerType;
import ntt.ntt_ms_accounts.repository.AccountMovementRepository;
import ntt.ntt_ms_accounts.repository.AccountRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Clock;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Objects;


@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepo;
    private final AccountMovementRepository movementRepo;
    private final CustomerClient customerClient;

    // Create account
    public Mono<Account> create(Account acc) {
        log.info("Creating account with number {}", acc.getAccountNumber());
        log.debug("Account details: {}", acc);

        return customerClient.getCustomer(acc.getCustomerId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Customer not found")))
                // 1) Reglas por tipo (PERSONAL/BUSINESS)
                .flatMap(cust -> validateAccountCreation(cust.type(), acc)
                        // 2) Reglas por subtipo (VIP / PYME)
                        .then(applySubtypeRules(cust.subType(), acc))
                )
                // 3) Unicidad de accountNumber y guardado con defaults por tipo
                .then(accountRepo.findByAccountNumber(acc.getAccountNumber())
                        .flatMap(a -> Mono.<Account>error(new IllegalStateException("accountNumber already exists")))
                        .switchIfEmpty(Mono.defer(() -> {
                            // defaults por tipo de cuenta
                            applyTypeDefaults(acc);
                            if (acc.getStatus() == null) acc.setStatus(AccountStatus.ACTIVE);
                            if (acc.getCreatedAt() == null) acc.setCreatedAt(Instant.now());
                            return accountRepo.save(acc);
                        }))
                        .cast(Account.class)
                );
    }

    private Mono<Void> applySubtypeRules(CustomerSubType subType, Account acc) {
        return switch (subType) {
            case PERSONAL_VIP -> {
                // VIP: solo permite cuenta de AHORRO (SAVINGS)
                if (acc.getType() != AccountType.SAVINGS) {
                    yield Mono.<Void>error(new IllegalStateException(
                            "PERSONAL_VIP only allowed to open SAVINGS accounts"));
                }
                // Validar tarjeta de crédito vigente o promedio diario mínimo
                yield Mono.<Void>empty();
            }
            case BUSINESS_PYME -> {
                // PYME: solo cuenta CORRIENTE y sin comisión de mantenimiento
                if (acc.getType() != AccountType.CURRENT) {
                    yield Mono.<Void>error(new IllegalStateException(
                            "BUSINESS_PYME only allowed to open CURRENT accounts"));
                }
                acc.setMaintenanceFee(BigDecimal.ZERO);
                yield Mono.<Void>empty();
            }
            // STANDARD (o cualquier otro) → sin reglas adicionales
            default -> Mono.<Void>empty();
        };
    }

    private void applyTypeDefaults(Account acc) {
        switch (acc.getType()) {
            case SAVINGS -> {
                // Ahorro: sin mantenimiento; límite de movimientos por defecto
                acc.setMaintenanceFee(BigDecimal.ZERO);
                if (acc.getMonthlyMovementLimit() == null || acc.getMonthlyMovementLimit() <= 0) {
                    acc.setMonthlyMovementLimit(10);
                }
                acc.setFixedDayAllowed(null);
            }
            case CURRENT -> {
                // Corriente: si no lo fijó (ej. PYME=0), poner default
                if (acc.getMaintenanceFee() == null) {
                    acc.setMaintenanceFee(new BigDecimal("5.00"));
                }
                // Sin límite mensual → usa null
                acc.setMonthlyMovementLimit(null);
                acc.setFixedDayAllowed(null);
            }
            case FIXED_TERM -> {
                // Plazo fijo: sin mantenimiento; 1 movimiento; día fijo
                acc.setMaintenanceFee(BigDecimal.ZERO);
                if (acc.getMonthlyMovementLimit() == null) acc.setMonthlyMovementLimit(1);
                if (acc.getFixedDayAllowed() == null) acc.setFixedDayAllowed(25);
            }
        }
    }


    public Flux<Account> getAccounts() {
        return accountRepo.findAll();
    }

    public Mono<Account> get(String id) {
        log.info("Search account with id {}", id);
        return accountRepo.findById(id);
    }

    public Flux<Account> listByCustomer(String customerId) {
        log.info("Search account with CustomerId {}", customerId);
        return accountRepo.findByCustomerId(customerId);
    }

    public Mono<Void> delete(String id) {
        log.info("Delete account with Id {}", id);
        return accountRepo.deleteById(id);
    }

    // MOVEMENTS
    public Mono<AccountMovement> deposit(String accountId, BigDecimal amount, String ref, Clock clock) {
        return operate(accountId, MovementType.DEPOSIT, amount, ref, clock);
    }

    public Mono<AccountMovement> withdraw(String accountId, BigDecimal amount, String ref, Clock clock) {
        return operate(accountId, MovementType.WITHDRAWAL, amount.negate(), ref, clock);
    }

    // Now returns entities, not DTO
    public Flux<AccountMovement> movements(String accountId, Instant from, Instant to) {
        return (from == null || to == null)
                ? movementRepo.findByAccountIdOrderByDateDesc(accountId)
                : movementRepo.findByAccountIdAndDateBetweenOrderByDateDesc(accountId, from, to);
    }

    // RULES

    private Mono<Void> validateAccountCreation(CustomerType  ct, Account acc) {
        log.debug("Validating account creation for customerId={}, type={}", acc.getCustomerId(), acc.getType());

        if (acc.getCustomerId() == null || acc.getCustomerId().isBlank()) {
            return Mono.error(new IllegalArgumentException("customerId is required"));
        }
        if (acc.getType() == null) {
            return Mono.error(new IllegalArgumentException("account type is required"));
        }

        if (acc.getBalance() != null && acc.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            return Mono.error(new IllegalArgumentException("opening balance cannot be negative"));
        }

        List<String> holders = Optional.ofNullable(acc.getHolders()).orElseGet(ArrayList::new);
        List<String> signers = Optional.ofNullable(acc.getAuthorizedSigners()).orElseGet(ArrayList::new);
        holders = holders.stream().filter(Objects::nonNull).distinct().toList();
        signers = signers.stream().filter(Objects::nonNull).distinct().toList();
        acc.setHolders(new ArrayList<>(holders));
        acc.setAuthorizedSigners(new ArrayList<>(signers));


        return switch (ct) {
            case PERSONAL -> validatePersonalOwnersAndSigners(acc.getCustomerId(), holders, signers)
                    .then(switch (acc.getType()) {
                        case SAVINGS  -> ensureNoExistingOfType(acc.getCustomerId(), AccountType.SAVINGS);
                        case CURRENT  -> ensureNoExistingOfType(acc.getCustomerId(), AccountType.CURRENT);
                        case FIXED_TERM -> Mono.<Void>empty();
                    });

            case BUSINESS -> {
                if (acc.getType() != AccountType.CURRENT) {
                    yield Mono.error(new IllegalStateException("Business customers can only open CURRENT accounts"));
                }
                yield validateBusinessOwnersAndSigners(acc.getCustomerId(), holders, signers);
            }


        };
    }

    private Mono<Void> validatePersonalOwnersAndSigners(String customerId,List<String> holders, List<String> signers) {
        if (holders.isEmpty() || !holders.contains(customerId)) {
            return Mono.error(new IllegalArgumentException("For PERSONAL, holders must include the customerId"));
        }
        boolean overlap = holders.stream().anyMatch(signers::contains);
        if (overlap) {
            return Mono.error(new IllegalArgumentException("A holder cannot be also an authorized signer"));
        }
        return Mono.empty();
    }

    private Mono<Void> validateBusinessOwnersAndSigners(String customerId,List<String> holders, List<String> signers) {
        if (holders.isEmpty()) {
            return Mono.error(new IllegalArgumentException("For BUSINESS, at least one holder is required"));
        }
        if (!holders.contains(customerId)) {
            return Mono.error(new IllegalArgumentException("For BUSINESS, holders must include the company customerId"));
        }

        return Mono.empty();
    }

    private Mono<Void> ensureNoExistingOfType(String customerId, AccountType type) {
        return accountRepo.findByCustomerId(customerId)
                .filter(a -> a.getType() == type)
                .hasElements()
                .flatMap(exists -> exists
                        ? Mono.error(new IllegalStateException(
                        "Customer already has a " + type + " account"))
                        : Mono.empty());
    }

    private Mono<AccountMovement> operate(String accountId,
                                          MovementType mt,
                                          BigDecimal signedAmount,
                                          String ref,
                                          Clock clock) {
        final Instant now = Instant.now(clock);
        final LocalDate today = LocalDateTime.ofInstant(now, ZoneId.systemDefault()).toLocalDate();

        return accountRepo.findById(accountId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("account not found")))
                .flatMap(acc ->
                        validateOperationRules(acc, mt, signedAmount, today, now)
                                .then(Mono.defer(() -> {
                                    final BigDecimal newBalance = acc.getBalance().add(signedAmount);
                                    if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                                        return Mono.error(new IllegalStateException("Insufficient funds"));
                                    }
                                    acc.setBalance(newBalance);
                                    return accountRepo.save(acc);
                                }))
                                .flatMap(saved -> {
                                    final AccountMovement mv = AccountMovement.builder()
                                            .accountId(accountId)
                                            .date(now)
                                            .type(mt)
                                            .amount(signedAmount.abs())
                                            .reference(ref)
                                            .build();
                                    return movementRepo.save(mv);
                                })
                );
    }

    private Mono<Void> validateOperationRules(Account acc,
                                              MovementType mt,
                                              BigDecimal signedAmount,
                                              LocalDate today,
                                              Instant now) {
        // FIXED_TERM -> only allowed operations on fixed day
        if (acc.getType() == AccountType.FIXED_TERM) {
            final Integer day = acc.getFixedDayAllowed();
            if (day == null || today.getDayOfMonth() != Math.min(day, today.lengthOfMonth())) {
                return Mono.error(new IllegalStateException(
                        "Fixed-term account allows operations only on day " + day));
            }
        }

        // SAVINGS -> monthly movement limit
        if (acc.getType() == AccountType.SAVINGS && acc.getMonthlyMovementLimit() != null) {
            final LocalDate first = today.with(TemporalAdjusters.firstDayOfMonth());
            final LocalDate last = today.with(TemporalAdjusters.lastDayOfMonth());
            final Instant from = first.atStartOfDay(ZoneId.systemDefault()).toInstant();
            final Instant to = last.plusDays(1)
                    .atStartOfDay(ZoneId.systemDefault()).toInstant()
                    .minusMillis(1);

            return movementRepo
                    .findByAccountIdAndDateBetweenOrderByDateDesc(acc.getId(), from, to)
                    .count()
                    .flatMap(cnt -> cnt >= acc.getMonthlyMovementLimit()
                            ? Mono.error(new IllegalStateException(
                            "Monthly movement limit exceeded for SAVINGS"))
                            : Mono.empty());
        }

        // CURRENT -> no monthly limit (negative balance guarded in operate)
        return Mono.empty();
    }
}
