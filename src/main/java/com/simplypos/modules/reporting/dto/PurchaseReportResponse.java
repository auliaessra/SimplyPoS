package com.simplypos.modules.reporting.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class PurchaseReportResponse {
    private LocalDate from;
    private LocalDate to;
    private Long storeId;
    private String storeName;

    private BigDecimal totalPurchase = BigDecimal.ZERO;
    private Long totalInvoices = 0L;
    private BigDecimal totalPaid = BigDecimal.ZERO;
    private BigDecimal totalOutstanding = BigDecimal.ZERO;

    private List<SalesChartPoint> dailyChart;
    private List<VendorSummary> topVendors;

    @Data
    public static class VendorSummary {
        private Long vendorId;
        private String vendorName;
        private BigDecimal totalAmount;
        private Long invoiceCount;
    }
}