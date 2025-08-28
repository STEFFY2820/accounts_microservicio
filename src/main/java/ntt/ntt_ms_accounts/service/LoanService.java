package ntt.ntt_ms_accounts.service;

import ntt.ntt_ms_accounts.client.CustomerClient;
import ntt.ntt_ms_accounts.models.Loan;
import ntt.ntt_ms_accounts.models.LoanStatus;
import ntt.ntt_ms_accounts.models.LoanType;
import ntt.ntt_ms_accounts.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class LoanService {
    private final LoanRepository loanRepo;
    private final CustomerClient customerClient;

    public Mono<Loan> create(Loan loan) {
        // Defaults
        if (loan.getStatus() == null) loan.setStatus(LoanStatus.ACTIVE);
        if (loan.getDisbursementDate() == null) loan.setDisbursementDate(Instant.now());
        if (loan.getRemaining() == null) loan.setRemaining(loan.getPrincipal());

        return customerClient.getCustomerType(loan.getCustomerId())
                .flatMap(ct -> {
                    if (loan.getType() == LoanType.PERSONAL && ct != CustomerClient.CustomerType.PERSONAL) {
                        return Mono.error(new IllegalStateException("Personal loan must belong to a personal customer"));
                    }
                    if (loan.getType() == LoanType.BUSINESS && ct != CustomerClient.CustomerType.BUSINESS) {
                        return Mono.error(new IllegalStateException("Business loan must belong to a business customer"));
                    }
                    return Mono.empty();
                })
                .then(
                        loan.getType() == LoanType.PERSONAL
                                ? loanRepo.countByCustomerIdAndType(loan.getCustomerId(), LoanType.PERSONAL)
                                .flatMap(cnt -> cnt > 0
                                        ? Mono.error(new IllegalStateException("Customer already has a PERSONAL loan"))
                                        : loanRepo.save(loan))
                                : loanRepo.save(loan) // BUSINESS: N permitidos
                );
    }

    public Mono<Loan> get(String id) { return loanRepo.findById(id); }

    public Flux<Loan> listByCustomer(String customerId) { return loanRepo.findByCustomerId(customerId); }

    public Mono<Loan> payment(String id, BigDecimal amount) {
        return loanRepo.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("loan not found")))
                .flatMap(ln -> {
                    BigDecimal newRemaining = ln.getRemaining().subtract(amount);
                    ln.setRemaining(newRemaining.max(BigDecimal.ZERO));
                    if (ln.getRemaining().compareTo(BigDecimal.ZERO) == 0) ln.setStatus(LoanStatus.CLOSED);
                    return loanRepo.save(ln);
                });
    }
}
