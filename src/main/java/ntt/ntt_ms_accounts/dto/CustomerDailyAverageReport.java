package ntt.ntt_ms_accounts.dto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


public record CustomerDailyAverageReport(
        String customerId,
        String month,               // "2025-08"
        LocalDate fromDate,         // primer día del mes (inclusive)
        LocalDate toDate,           // hoy (inclusive)
        int daysComputed,           // cantidad de días usados
        List<ProductDailyAverage> products,
        BigDecimal totalDailyAverage) {
}
