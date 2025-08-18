package ntt.ntt_ms_accounts.controller;

import io.swagger.v3.oas.annotations.Operation;
import ntt.ntt_ms_accounts.models.CreditCard;
import ntt.ntt_ms_accounts.service.CreditCardService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RestController
@RequestMapping("/credit-cards")
@RequiredArgsConstructor
public class CreditCardController {

    private final CreditCardService cardService;

    @Operation(summary = "Create Credit Card")
    @PostMapping
    public Mono<CreditCard> create(@RequestBody CreditCard card) {
        return cardService.create(card);
    }

    @GetMapping("/{id}")
    public Mono<CreditCard> get(@PathVariable String id) {
        return cardService.get(id);
    }

    @GetMapping("/customer/{customerId}")
    public Flux<CreditCard> listByCustomer(@PathVariable String customerId) {
        return cardService.listByCustomer(customerId);
    }

    @PostMapping("/{id}/charge")
    public Mono<CreditCard> charge(@PathVariable String id,
                                   @RequestParam BigDecimal amount,
                                   @RequestParam(required = false) String reference) {
        return cardService.charge(id, amount, reference);
    }

    @PostMapping("/{id}/payment")
    public Mono<CreditCard> payment(@PathVariable String id,
                                    @RequestParam BigDecimal amount) {
        return cardService.payment(id, amount);
    }
}
