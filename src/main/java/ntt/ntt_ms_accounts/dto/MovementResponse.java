package ntt.ntt_ms_accounts.dto;
import java.time.Instant;

public record MovementResponse(
        String id,
        String accountId,
        Instant date,
        String type,
        String amount,
        String reference
) {}