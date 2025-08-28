package ntt.ntt_ms_accounts.dto;

import java.time.Instant;

public record AccountResponse(
        String id,
        String customerId,
        String type,     // enum a string
        String balance,  // BigDecimal a string
        Instant createdAt,
        String accountNumber,
        String status    // enum a string
) {}