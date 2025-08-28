package ntt.ntt_ms_accounts.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateLoanRequest(
        @NotBlank String customerId,
        @NotNull String type,              // "PERSONAL" | "BUSINESS"
        @Positive BigDecimal principal,    // monto otorgado
        @Positive BigDecimal interestRateAnnual, // ej. 0.25 para 25%
        @Positive Integer termMonths) {}