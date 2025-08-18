package ntt.ntt_ms_accounts.repository;

import ntt.ntt_ms_accounts.models.Account;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountRepository extends ReactiveMongoRepository<Account, String>
{
    Mono<Account> findByAccountNumber(String accountNumber);
    Flux<Account> findByCustomerId(String customerId);

    @Override
    Flux<Account> findAll();
}