package ntt.ntt_ms_accounts.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import ntt.ntt_ms_accounts.dto.CommissionReport;
import ntt.ntt_ms_accounts.dto.CustomerDailyAverageReport;
import ntt.ntt_ms_accounts.service.ReportService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.time.Instant;
import java.time.ZoneId;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reports;
    private static final ZoneId ZONE = ZoneId.systemDefault();

    @Operation(summary="Daily average balances (current month) for all products of a customer")
    @GetMapping("/customers/{customerId}/daily-averages/current-month")
    public Mono<CustomerDailyAverageReport> customerDailyAverages(@PathVariable String customerId) {
        return reports.dailyAveragesForCustomerCurrentMonth(customerId, ZONE);
    }

    @Operation(summary="Commission totals by product within a period")
    @GetMapping("/commissions")
    public Mono<CommissionReport> commissions(@RequestParam Instant from, @RequestParam Instant to) {
        return reports.commissionsByProduct(from, to);
    }
}
