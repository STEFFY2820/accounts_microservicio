package ntt.ntt_ms_accounts.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record AmountRequest(
        @NotNull @Positive BigDecimal amount,
        String reference // opcional (solo para charge)
) {}