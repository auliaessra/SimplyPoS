package com.simplypos.modules.purchase.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PurchaseInvoiceRequest {
    private Long storeId;
    @NotNull
    private Long vendorId;
    private String vendorInvoiceNo;
    @NotNull
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private BigDecimal taxAmount;
    private String notes;
    @NotEmpty
    private List<ItemLine> items;

    @Data
    public static class ItemLine {
        @NotNull
        private Long itemId;
        @NotNull
        @Positive
        private BigDecimal qtyMl;
        @NotNull
        @Positive
        private BigDecimal unitCost;
    }
}