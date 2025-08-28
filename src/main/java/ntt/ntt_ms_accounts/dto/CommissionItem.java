package ntt.ntt_ms_accounts.dto;
import java.math.BigDecimal;

public record CommissionItem(
        String productType,     // ACCOUNT | CREDIT_CARD | LOAN (según tu alcance)
        String productId,
        String productNumber,   // p.ej., accountNumber
        String commissionType,  // tomado de reference o tipo interno
        BigDecimal totalAmount
) {
}
