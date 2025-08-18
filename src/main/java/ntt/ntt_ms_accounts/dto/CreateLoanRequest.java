package ntt.ntt_ms_accounts.dto;

public record CreateLoanRequest(String customerId, String type, String principal,
                                String interestRateAnnual, Integer termMonths) {}