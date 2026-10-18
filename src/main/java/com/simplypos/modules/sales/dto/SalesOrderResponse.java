package com.simplypos.modules.sales.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class SalesOrderResponse {
    private Long id;
    private String orderNo;
    private LocalDateTime orderDate;
    private Long storeId;
    private String storeName;
    private Long customerId;
    private String customerName;
    private String paymentMethod;
    private String status;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal changeAmount;
    private String notes;
    private String createdBy;
    private LocalDateTime createdAt;
    private List<ItemLine> items;

    @Data
    public static class ItemLine {
        private Long itemId;
        private String itemCode;
        private String itemName;
        private BigDecimal qtyMl;
        private BigDecimal unitPrice;
        private BigDecimal discountPercent;
        private BigDecimal totalPrice;
        private BigDecimal unitCost;
        private BigDecimal totalCost;
    }
}