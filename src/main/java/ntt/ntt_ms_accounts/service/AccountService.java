package ntt.ntt_ms_accounts.service;

import lombok.RequiredArgsConstructor;
import ntt.ntt_ms_accounts.client.CustomerClient;
import ntt.ntt_ms_accounts.dto.OpenAccountRequest;
import ntt.ntt_ms_accounts.models.Account;
import ntt.ntt_ms_accounts.models.AccountMovement;
import ntt.ntt_ms_accounts.models.AccountStatus;
import ntt.ntt_ms_accounts.models.MovementType;
import ntt.ntt_ms_accounts.models.AccountType;
import ntt.ntt_ms_accounts.repository.AccountMovementRepository;
import ntt.ntt_ms_accounts.repository.AccountRepository;
import ntt.ntt_ms_accounts.mapper.AccountMapper;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepo;
    private final AccountMovementRepository movementRepo;
    private final CustomerClient customerClient;
    private final AccountMapper mapper;


    public Mono<Account> createFromRequest(OpenAccountRequest req) {
        Account acc = mapper.toModel(req);

        CustomerClient.CustomerType ct =
                CustomerClient.CustomerType.valueOf(req.customerType().name());

        return validateAccountCreation(ct, acc)
                .then(
                        accountRepo.findByAccountNumber(acc.getAccountNumber())
                                .flatMap(a -> Mono.<Account>error(
                                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "accountNumber already exists")))
                                .switchIfEmpty(Mono.defer(() -> {
                                    acc.setCreatedAt(java.time.Instant.now());
                                    switch (acc.getType()) {
                                        case SAVINGS -> {
                                            acc.setMaintenanceFee(java.math.BigDecimal.ZERO);
                                            if (acc.getMonthlyMovementLimit()==null || acc.getMonthlyMovementLimit()<=0)
                                                acc.setMonthlyMovementLimit(10);
                                            acc.setFixedDayAllowed(null);
                                        }
                                        case CURRENT -> {
                                            if (acc.getMaintenanceFee()==null
                                                    || acc.getMaintenanceFee().compareTo(java.math.BigDecimal.ZERO) <= 0)
                                                acc.setMaintenanceFee(new java.math.BigDecimal("5.00"));
                                            acc.setMonthlyMovementLimit(0);
                                            acc.setFixedDayAllowed(null);
                                        }
                                        case FIXED_TERM -> {
                                            acc.setMaintenanceFee(java.math.BigDecimal.ZERO);
                                            acc.setMonthlyMovementLimit(1);
                                            if (acc.getFixedDayAllowed()==null) acc.setFixedDayAllowed(25);
                                        }
                                    }
                                    if (acc.getBalance()==null) acc.setBalance(java.math.BigDecimal.ZERO);
                                    if (acc.getStatus()==null)  acc.setStatus(AccountStatus.ACTIVE);
                                    return accountRepo.save(acc);
                                }))
                                .cast(Account.class)
                );
    }


    // Create account
    public Mono<Account> create(Account acc) {
        log.info("Creating account with number {}", acc.getAccountNumber());
        log.debug("Account details: {}", acc);

        return customerClient.getCustomerType(acc.getCustomerId())
                .flatMap(ct -> validateAccountCreation(ct, acc))
                .then(
                        accountRepo.findByAccountNumber(acc.getAccountNumber())
                                .flatMap(a -> Mono.<Account>error(
                                        new IllegalStateException("accountNumber already exists")))
                                .switchIfEmpty(Mono.defer(() -> {
                                    // defaults by type
                                    acc.setCreatedAt(Instant.now());
                                    switch (acc.getType()) {
                                        case SAVINGS -> {
                                            acc.setMaintenanceFee(BigDecimal.ZERO);
                                            if (acc.getMonthlyMovementLimit() == null
                                                    || acc.getMonthlyMovementLimit() <= 0) {
                                                acc.setMonthlyMovementLimit(10); // default
                                            }
                                            acc.setFixedDayAllowed(null);
                                        }
                                        case CURRENT -> {
                                            if (acc.getMaintenanceFee() == null
                                                    || acc.getMaintenanceFee().compareTo(BigDecimal.ZERO) <= 0) {
                                                acc.setMaintenanceFee(new BigDecimal("5.00"));
                                            }
                                            acc.setMonthlyMovementLimit(0);
                                            acc.setFixedDayAllowed(null);
                                        }
                                        case FIXED_TERM -> {
                                            acc.setMaintenanceFee(BigDecimal.ZERO);
                                            acc.setMonthlyMovementLimit(1);
                                            if (acc.getFixedDayAllowed() == null) {
                                                acc.setFixedDayAllowed(25); // default day
                                            }
                                        }
                                    }

                                    if (acc.getBalance() == null) {
                                        acc.setBalance(BigDecimal.ZERO);
                                    }
                                    if (acc.getStatus() == null) {
                                        acc.setStatus(AccountStatus.ACTIVE);
                                    }
                                    return accountRepo.save(acc);
                                }))
                                .cast(Account.class)
                );
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

    private Mono<Void> validateAccountCreation(CustomerClient.CustomerType ct, Account acc) {
        List<String> holders = Optional.ofNullable(acc.getHolders()).orElseGet(ArrayList::new);
        List<String> signers = Optional.ofNullable(acc.getAuthorizedSigners()).orElseGet(ArrayList::new);

        if (ct == CustomerClient.CustomerType.PERSONAL) {
            System.out.println("Reconoció PERSONAL");
            Mono<Void> partyRule = validatePersonalOwnersAndSigners(holders, signers);

            if (acc.getType() == AccountType.SAVINGS) {
                return partyRule.then(ensureNoExistingOfType(acc.getCustomerId(), AccountType.SAVINGS));
            }
            if (acc.getType() == AccountType.CURRENT) {
                return partyRule.then(ensureNoExistingOfType(acc.getCustomerId(), AccountType.CURRENT));
            }

            if (acc.getType() == AccountType.FIXED_TERM) {
                return partyRule;
            }
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "unsupported account type for personal"));
        }
        if (ct == CustomerClient.CustomerType.BUSINESS) {
            System.out.println("Reconoció Bussiness");
            if (acc.getType() != AccountType.CURRENT) {
                return Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Business customers can only open CURRENT accounts"));
            }
            return validateBusinessOwnersAndSigners(holders, signers);
            }
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "unsupported customer type"));
        }

    private Mono<Void> validatePersonalOwnersAndSigners(List<String> holders, List<String> signers) {
        if (holders.size() != 1) {
            return Mono.error(new IllegalStateException("personal accounts require exactly one holder"));
        }
        if (!signers.isEmpty()) {
            return Mono.error(new IllegalStateException("personal accounts cannot have authorized signers"));
        }
        return Mono.empty();
    }

    private Mono<Void> validateBusinessOwnersAndSigners(List<String> holders, List<String> signers) {
        if (holders == null || holders.isEmpty()) {
            return Mono.error(new IllegalStateException("business accounts require at least one holder"));
        }
        // signers can be empty
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
