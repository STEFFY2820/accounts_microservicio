package ntt.ntt_ms_accounts.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record AccountResponse(
        String id,
        String customerId,
        String type,     // enum a string
        String balance,  // BigDecimal a string
        Instant createdAt,
        String accountNumber,
        String status,// enum a string
        List<String> holders,
        List<String> authorizedSigners,
        BigDecimal maintenanceFee,
        Integer monthlyMovementLimit,
        Integer fixedDayAllowed
) {}