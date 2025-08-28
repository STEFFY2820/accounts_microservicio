package ntt.ntt_ms_accounts.dto;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CommissionReport(
        Instant from,
        Instant to,
        List<CommissionItem> items,
        BigDecimal grandTotal
) { }
