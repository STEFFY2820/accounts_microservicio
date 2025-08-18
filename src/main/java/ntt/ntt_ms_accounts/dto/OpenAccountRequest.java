package ntt.ntt_ms_accounts.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;


public record OpenAccountRequest(
        @NotBlank String customerId,
        @NotNull  String type,
        @NotBlank String accountNumber,
        List<String> holders,
        List<String> authorizedSigners) {
}
