package ntt.ntt_ms_accounts.dto;
import java.math.BigDecimal;

public record ProductDailyAverage(
        String productType,   // ACCOUNT | LOAN | CREDIT_CARD
        String productId,
        String productNumber, // accountNumber o cardNumber; para loan puedes dejar null o un code
        BigDecimal dailyAverage) {
}
