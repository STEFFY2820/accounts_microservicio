package ntt.ntt_ms_accounts.service;

import jakarta.validation.constraints.NotNull;
import ntt.ntt_ms_accounts.models.CreditCard;
import ntt.ntt_ms_accounts.models.CardStatus;
import ntt.ntt_ms_accounts.repository.CreditCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CreditCardService {

    private final CreditCardRepository cardRepo;

    public Mono<CreditCard> create(CreditCard c) {
        if (c.getStatus() == null) c.setStatus(CardStatus.ACTIVE);
        if (c.getAvailable() == null) c.setAvailable(c.getCreditLimit());
        c.setCreatedAt(Instant.now());
        return cardRepo.findByCardNumber(c.getCardNumber())
                .flatMap(x -> Mono.<CreditCard>error(new IllegalStateException("cardNumber already exists")))
                .switchIfEmpty(cardRepo.save(c))
                .cast(CreditCard.class);
    }

    public Mono<CreditCard> get(String id) { return cardRepo.findById(id); }

    public Flux<CreditCard> listByCustomer(String customerId) { return cardRepo.findByCustomerId(customerId); }

    public Mono<CreditCard> charge(String id, @NotNull BigDecimal amount, String ref) {
        return cardRepo.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("card not found")))
                .flatMap(card -> {
                    if (card.getAvailable().compareTo(amount) < 0) {
                        return Mono.error(new IllegalStateException("Insufficient available credit"));
                    }
                    card.setAvailable(card.getAvailable().subtract(amount));
                    return cardRepo.save(card);
                });
    }

    public Mono<CreditCard> payment(String id, @NotNull BigDecimal amount) {
        return cardRepo.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("card not found")))
                .flatMap(card -> {
                    BigDecimal newAvailable = card.getAvailable().add(amount);
                    if (newAvailable.compareTo(card.getCreditLimit()) > 0) {
                        newAvailable = card.getCreditLimit();
                    }
                    card.setAvailable(newAvailable);
                    return cardRepo.save(card);
                });
    }
}
