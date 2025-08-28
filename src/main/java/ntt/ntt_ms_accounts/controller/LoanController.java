package ntt.ntt_ms_accounts.controller;

import jakarta.validation.Valid;
import ntt.ntt_ms_accounts.dto.LoanResponse;
import ntt.ntt_ms_accounts.dto.CreateLoanRequest;
import ntt.ntt_ms_accounts.dto.AmountRequest;
import ntt.ntt_ms_accounts.service.LoanService;
import ntt.ntt_ms_accounts.mapper.LoanMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;
    private final LoanMapper mapper;

    @Operation(summary = "Create Loan")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Mono<LoanResponse> create(@RequestBody @Valid CreateLoanRequest req) {
        return loanService.create(mapper.toModel(req))
                .map(mapper::toResponse);
    }

    @Operation(summary = "Get Loan by ID")
    @GetMapping("/{id}")
    public Mono<LoanResponse> get(@PathVariable String id) {
        return loanService.get(id).map(mapper::toResponse);
    }

    @Operation(summary = "List Loans by Customer")
    @GetMapping("/customer/{customerId}")
    public Flux<LoanResponse> listByCustomer(@PathVariable String customerId) {
        return loanService.listByCustomer(customerId).map(mapper::toResponse);
    }

    @Operation(summary = "Register a Payment to a Loan")
    @PostMapping("/{id}/payment")
    public Mono<LoanResponse> payment(@PathVariable String id,
                                      @RequestBody @Valid AmountRequest body) {
        return loanService.payment(id, body.amount())
                .map(mapper::toResponse);
    }
}
