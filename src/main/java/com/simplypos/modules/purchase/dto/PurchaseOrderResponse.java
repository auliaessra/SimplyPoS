package com.simplypos.modules.purchase.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class PurchaseOrderResponse {
    private Long id;
    private String poNumber;
    private Long storeId;
    private String storeName;
    private Long vendorId;
    private String vendorName;
    private LocalDate orderDate;
    private LocalDate expectedDate;
    private String status;
    private BigDecimal totalAmount;
    private String notes;
    private LocalDateTime createdAt;
    private List<ItemLine> items;

    @Data
    public static class ItemLine {
        private Long itemId;
        private String itemCode;
        private String itemName;
        private BigDecimal qtyMl;
        private BigDecimal unitCost;
        private BigDecimal totalCost;
    }
}