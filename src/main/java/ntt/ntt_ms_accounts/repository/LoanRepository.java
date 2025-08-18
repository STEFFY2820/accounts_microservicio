package ntt.ntt_ms_accounts.repository;

import ntt.ntt_ms_accounts.models.Loan;
import ntt.ntt_ms_accounts.models.LoanType;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface LoanRepository  extends ReactiveMongoRepository<Loan, String>  {
    Flux<Loan> findByCustomerId(String customerId);
    Mono<Long> countByCustomerIdAndType(String customerId, LoanType type);

}
