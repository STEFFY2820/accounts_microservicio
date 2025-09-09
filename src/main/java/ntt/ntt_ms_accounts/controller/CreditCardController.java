package ntt.ntt_ms_accounts.controller;

import io.swagger.v3.oas.annotations.Operation;
import ntt.ntt_ms_accounts.mapper.CreditCardMapper;
import ntt.ntt_ms_accounts.service.CreditCardService;
import ntt.ntt_ms_accounts.dto.CreateCreditCard;
import ntt.ntt_ms_accounts.dto.CreditCardResponse;
import ntt.ntt_ms_accounts.dto.AmountRequest;
import ntt.ntt_ms_accounts.models.CreditCard;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/credit-cards")
@RequiredArgsConstructor
public class CreditCardController {

    private final CreditCardService cardService;
    private final CreditCardMapper mapper;

    @Operation(summary = "Create Credit Card")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Mono<CreditCardResponse> create(@RequestBody @Valid CreateCreditCard req) {
        CreditCard model = mapper.toModel(req);
        return cardService.create(model)
                .map(mapper::toResponse);
    }

    @Operation(summary = "Get Credit Card by id")
    @GetMapping("/{id}")
    public Mono<CreditCardResponse> get(@PathVariable String id) {
        return cardService.get(id).map(mapper::toResponse);
    }

    @Operation(summary = "List credit cards by customer")
    @GetMapping("/customer/{customerId}")
    public Flux<CreditCardResponse> listByCustomer(@PathVariable String customerId) {
        return cardService.listByCustomer(customerId)
                .map(mapper::toResponse);
    }

    @Operation(summary = "Charge a credit card")
    @PostMapping("/{id}/charge")
    public Mono<CreditCardResponse> charge(@PathVariable("id") String cardId,
                                           @RequestBody @Valid AmountRequest body) {
        return cardService.charge(cardId, body.amount(), body.reference())
                .map(mapper::toResponse);
    }

    @Operation(summary = "Register a payment to a credit card")
    @PostMapping("/{id}/payment")
    public Mono<CreditCardResponse> payment(@PathVariable("id") String cardId,
                                            @RequestBody @Valid AmountRequest body) {
        return cardService.payment(cardId, body.amount())
                .map(mapper::toResponse);
    }
}
