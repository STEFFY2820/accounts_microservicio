package ntt.ntt_ms_accounts.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record LoanResponse(
        String id,
        String customerId,
        String type,            // mapeado desde enum LoanType
        String status,          // mapeado desde enum LoanStatus
        BigDecimal principal,
        BigDecimal remaining,
        BigDecimal interestRateAnnual,
        Integer termMonths,
        Instant disbursementDate,
        Instant nextDueDate) {
}
