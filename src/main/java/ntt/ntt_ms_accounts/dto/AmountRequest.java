package ntt.ntt_ms_accounts.dto;
import jakarta.validation.constraints.NotBlank;

public record AmountRequest(@NotBlank String amount, String reference) {
}
