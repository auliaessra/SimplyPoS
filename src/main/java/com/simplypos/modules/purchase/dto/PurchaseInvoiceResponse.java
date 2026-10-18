package com.simplypos.modules.purchase.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class PurchaseInvoiceResponse {
    private Long id;
    private String invoiceNo;
    private String vendorInvoiceNo;
    private Long storeId;
    private String storeName;
    private Long vendorId;
    private String vendorName;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private String status;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private String notes;
    private LocalDateTime postedAt;
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