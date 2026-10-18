package com.simplypos.modules.finance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class BalanceSheetReport {
    private LocalDate asOf;
    private Long storeId;
    private String storeName;

    private List<ReportLine> assets;
    private List<ReportLine> liabilities;
    private List<ReportLine> equities;

    private BigDecimal totalAssets = BigDecimal.ZERO;
    private BigDecimal totalLiabilities = BigDecimal.ZERO;
    private BigDecimal totalEquities = BigDecimal.ZERO;
    private BigDecimal netProfit = BigDecimal.ZERO; // dari laba rugi tahun berjalan

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