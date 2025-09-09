package ntt.ntt_ms_accounts.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ntt.ntt_ms_accounts.models.CustomerSubType;
import ntt.ntt_ms_accounts.models.CustomerType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerClient {


    private final WebClient customersWebClient;

    public record CustomerDTO(
            String id,
            CustomerType type,        // PERSONAL | BUSINESS
            CustomerSubType subType,  // STANDARD | VIP | PYME | ...
            String email,
            String phone,
            String address) {}

    public Mono<CustomerDTO> getCustomer(String customerId) {
        return customersWebClient.get()
                .uri("/api/customers/{id}", customerId)
                .retrieve()
                .bodyToMono(CustomerDTO.class)
                .doOnNext(c -> log.debug("Customer fetched: {} - {} / {}", c.id(), c.type(), c.subType()));
    }

}
