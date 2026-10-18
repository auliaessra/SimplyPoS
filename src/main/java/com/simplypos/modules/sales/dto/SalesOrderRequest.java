package com.simplypos.modules.sales.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class SalesOrderRequest {
    private Long storeId;
    private Long customerId;
    private LocalDate orderDate;

    @NotNull
    private String paymentMethod; // CASH, QRIS, CREDIT
    private BigDecimal taxAmount;
    private BigDecimal paidAmount;
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
        private BigDecimal discountPercent = BigDecimal.ZERO;
    }
}