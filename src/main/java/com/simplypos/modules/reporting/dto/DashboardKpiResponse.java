package com.simplypos.modules.reporting.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class DashboardKpiResponse {
    // Master data
    private Long totalItems;
    private Long totalCustomers;
    private Long totalVendors;
    private Long totalStores;
    private Long totalUsers;

    // Hari ini
    private BigDecimal todaySales;
    private Long todaySalesCount;
    private BigDecimal todayPurchases;

    // Bulan ini
    private BigDecimal monthSales;
    private BigDecimal monthPurchases;
    private BigDecimal monthProfit;

    // Outstanding
    private BigDecimal totalAr;
    private BigDecimal totalAp;

    // Inventory
    private BigDecimal totalInventoryValue;
    private Long lowStockCount;
}