package ntt.ntt_ms_accounts.controller;


import ntt.ntt_ms_accounts.models.Loan;
import ntt.ntt_ms_accounts.service.LoanService;



import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RestController
@RequestMapping("/loans")
@RequiredArgsConstructor
public class LoanController {
    private final LoanService loanService;

    @PostMapping
    public Mono<Loan> create(@RequestBody Loan loan) {
        return loanService.create(loan);
    }

    @GetMapping("/{id}")
    public Mono<Loan> get(@PathVariable String id) {
        return loanService.get(id);
    }

    @GetMapping("/customer/{customerId}")
    public Flux<Loan> listByCustomer(@PathVariable String customerId) {
        return loanService.listByCustomer(customerId);
    }

    @PostMapping("/{id}/payment")
    public Mono<Loan> payment(@PathVariable String id,
                              @RequestParam BigDecimal amount) {
        return loanService.payment(id, amount);
    }
}
