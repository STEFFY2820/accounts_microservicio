package ntt.ntt_ms_accounts.repository;


import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.time.Instant;

import ntt.ntt_ms_accounts.models.AccountMovement;

public interface AccountMovementRepository extends ReactiveMongoRepository<AccountMovement, String> {
    Flux<AccountMovement> findByAccountIdOrderByDateDesc(String accountId);
    Flux<AccountMovement> findByAccountIdAndDateBetweenOrderByDateDesc(String accountId, Instant from, Instant to);
}
