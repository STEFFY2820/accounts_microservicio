package ntt.ntt_ms_accounts.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import ntt.ntt_ms_accounts.models.CustomerType;

public record OpenAccountRequest(
        @NotBlank String customerId,
        CustomerType customerType,
        @NotNull  String type,
        @NotBlank String accountNumber,
        List<String> holders,
        List<String> authorizedSigners
)
{
}
