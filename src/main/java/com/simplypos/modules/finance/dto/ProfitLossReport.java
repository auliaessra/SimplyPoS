package com.simplypos.modules.finance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class ProfitLossReport {
    private LocalDate from;
    private LocalDate to;
    private Long storeId;
    private String storeName;

    private List<ReportLine> revenues;
    private List<ReportLine> expenses;

    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private BigDecimal totalExpense = BigDecimal.ZERO;
    private BigDecimal netProfit = BigDecimal.ZERO;

    @Data
    public static class ReportLine {
        private String accountCode;
        private String accountName;
        private BigDecimal amount;

        public ReportLine(String accountCode, String accountName, BigDecimal amount) {
            this.accountCode = accountCode;
            this.accountName = accountName;
            this.amount = amount;
        }
    }
}