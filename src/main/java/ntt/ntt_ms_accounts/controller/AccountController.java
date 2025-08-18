package ntt.ntt_ms_accounts.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ntt.ntt_ms_accounts.models.Account;
import ntt.ntt_ms_accounts.models.AccountMovement;
import ntt.ntt_ms_accounts.dto.*;
import ntt.ntt_ms_accounts.service.AccountService;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Instant;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "Open new account")
    @PostMapping
    public Mono<Account> create(@RequestBody Account account) {
        return accountService.create(account);
    }

    @Operation(summary = "Get account by id")
    @GetMapping("/{id}")
    public Mono<Account> get(@PathVariable String id) {
        return accountService.get(id);
    }

    @Operation(summary = "Get Accounts")
    @GetMapping("list")
    public Flux<Account> getAccountsAll(){
        return accountService.getAccounts();
    }

    @Operation(summary = "List accounts by customer")
    @GetMapping("/customerAccount/{customerId}")
    public Flux<Account> getAccountsByCustomerId(@PathVariable String customerId) {
        return accountService.listByCustomer(customerId);
    }

    @Operation(summary = "Delete Account")
    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable String id) {
        return accountService.delete(id);
    }

    @Operation(summary = "Deposit into account")
    @PostMapping("/{id}/deposit")
    public Mono<AccountMovement> deposit(
            @PathVariable("id") String accountId,
            @RequestBody @Valid DepositRequest req
    ) {
        return accountService.deposit(
                accountId,
                req.amount(),
                req.reference(),
                java.time.Clock.systemDefaultZone()
        );
    }

    @Operation(summary = "Get account balance")
    @GetMapping("/{id}/balance")
    public Mono<BalanceResponse> balance(@PathVariable String id) {
        return accountService.balance(id);
    }

    @Operation(summary = "Withdraw funds from an account")
    @PostMapping("/{id}/withdraw")
    public Mono<AccountMovement> withdraw(
            @PathVariable("id") String accountId,
            @RequestBody @Valid DepositRequest req
    ) {
        return accountService.withdraw(
                accountId,
                req.amount(),
                req.reference(),
                java.time.Clock.systemDefaultZone()
        );
    }

    @Operation(summary = "Movements from an account")
    @GetMapping("/{id}/movements")
    public Flux<MovementResponse> movements(
            @PathVariable String id,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to
    ) {
        return accountService.movements(id, from, to);
    }

}
