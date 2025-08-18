package ntt.ntt_ms_accounts.repository;

import ntt.ntt_ms_accounts.models.CreditCard;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface CreditCardRepository extends ReactiveMongoRepository<CreditCard, String>{
    Mono<CreditCard> findByCardNumber(String cardNumber);
    Flux<CreditCard> findByCustomerId(String customerId);

}
