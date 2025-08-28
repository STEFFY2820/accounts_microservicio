package ntt.ntt_ms_accounts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateCreditCard(
        @NotBlank String cardNumber,
        @NotBlank String customerId,
        @NotNull String type,   // "PERSONAL" | "BUSINESS"
        BigDecimal creditLimit,
        Integer closingDay,
        Integer dueDay
) {}
