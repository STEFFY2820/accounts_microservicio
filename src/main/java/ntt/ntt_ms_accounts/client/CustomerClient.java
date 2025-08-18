package ntt.ntt_ms_accounts.client;


import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CustomerClient {
    public enum CustomerType { PERSONAL, BUSINESS }
    public Mono<CustomerType> getCustomerType(String customerId) {
        // TODO: reemplazar por WebClient al Customers MS
        return Mono.just(CustomerType.PERSONAL); // valor por defecto para avanzar
    }
}
