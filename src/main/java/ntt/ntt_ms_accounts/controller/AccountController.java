package ntt.ntt_ms_accounts.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ntt.ntt_ms_accounts.dto.AccountResponse;
import ntt.ntt_ms_accounts.dto.OpenAccountRequest;
import ntt.ntt_ms_accounts.dto.DepositRequest;
import ntt.ntt_ms_accounts.dto.MovementResponse;
import ntt.ntt_ms_accounts.dto.BalanceResponse;
import ntt.ntt_ms_accounts.service.AccountService;
import ntt.ntt_ms_accounts.mapper.AccountMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Instant;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper mapper;

    @PostMapping("/tst")
    public Mono<AccountResponse> createTest(@RequestBody @Valid OpenAccountRequest req) {
        return accountService.createFromRequest(req).map(mapper::toResponse);
    }

    @Operation(summary = "Open new account")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Mono<AccountResponse> create(@RequestBody @Valid OpenAccountRequest req) {
        return accountService.create(mapper.toModel(req))
                .map(mapper::toResponse);
    }

    @Operation(summary = "Get account by id")
    @GetMapping("/{id}")
    public Mono<AccountResponse> get(@PathVariable String id) {
        return accountService.get(id).map(mapper::toResponse);
    }

    @Operation(summary = "Get Accounts")
    @GetMapping("/list")
    public Flux<AccountResponse> getAccountsAll(){
        return accountService.getAccounts().map(mapper::toResponse);
    }

    @Operation(summary = "List accounts by customer")
    @GetMapping("/customerAccount/{customerId}")
    public Flux<AccountResponse> getAccountsByCustomerId(@PathVariable String customerId) {
        return accountService.listByCustomer(customerId).map(mapper::toResponse);
    }

    @Operation(summary = "Delete Account")
    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable String id) {
        return accountService.delete(id);
    }

    @Operation(summary = "Deposit into account")
    @PostMapping("/{id}/deposit")
    public Mono<MovementResponse> deposit(
            @PathVariable("id") String accountId,
            @RequestBody @Valid DepositRequest req
    ) {
        return accountService.deposit(
                accountId,
                req.amount(),
                req.reference(),
                java.time.Clock.systemDefaultZone()).map(mapper::toResponse);

    }

    @Operation(summary = "Get account balance")
    @GetMapping("/{id}/balance")
    public Mono<BalanceResponse> balance(@PathVariable String id) {
        return accountService.get(id).map(mapper::toBalance);
    }

    @Operation(summary = "Withdraw funds from an account")
    @PostMapping("/{id}/withdraw")
    public Mono<MovementResponse> withdraw(
            @PathVariable("id") String accountId,
            @RequestBody @Valid DepositRequest req
    ) {
        return accountService.withdraw(
                accountId,
                req.amount(),
                req.reference(),
                java.time.Clock.systemDefaultZone()).map(mapper::toResponse);
    }

    @Operation(summary = "Movements from an account")
    @GetMapping("/{id}/movements")
    public Flux<MovementResponse> movements(
            @PathVariable String id,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to
    ) {
        return accountService.movements(id, from, to)
                .map(mapper::toResponse);
    }

}
