package com.simplypos.modules.reporting.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class SalesReportResponse {
    private LocalDate from;
    private LocalDate to;
    private Long storeId;
    private String storeName;

    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private BigDecimal totalCogs = BigDecimal.ZERO;
    private BigDecimal totalProfit = BigDecimal.ZERO;
    private Long totalTransactions = 0L;

    private List<SalesChartPoint> dailyChart;
    private List<TopProductResponse> topProducts;
}