package com.simplypos.modules.reporting.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class InventoryReportResponse {
    private BigDecimal totalValue = BigDecimal.ZERO;
    private Long totalItems = 0L;
    private Long lowStockCount = 0L;
    private List<InventoryItem> items;
    private List<InventoryItem> lowStockItems;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryItem {
        private Long itemId;
        private String itemCode;
        private String itemName;
        private String storeName;
        private BigDecimal stockQtyMl;
        private BigDecimal minStockMl;
        private BigDecimal unitCost;
        private BigDecimal totalValue;
        private boolean lowStock;
    }
}