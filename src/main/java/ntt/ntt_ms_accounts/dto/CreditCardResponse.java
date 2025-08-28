package ntt.ntt_ms_accounts.dto;
import java.math.BigDecimal;
import java.time.Instant;

public record CreditCardResponse(
        String id,
        String cardNumber,
        String customerId,
        String type,     // PERSONAL | BUSINESS
        String status,   // ACTIVE | BLOCKED | CANCELED
        BigDecimal creditLimit,
        BigDecimal available,
        Integer closingDay,
        Integer dueDay,
        Instant createdAt,
        Instant updatedAt) {
}
