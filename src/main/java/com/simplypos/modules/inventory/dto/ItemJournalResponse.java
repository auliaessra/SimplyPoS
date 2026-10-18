package com.simplypos.modules.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class ItemJournalResponse {
    private Long id;
    private String documentNo;
    private Long storeId;
    private String storeName;
    private LocalDate journalDate;
    private String status;
    private String reason;
    private String notes;
    private LocalDateTime postedAt;
    private String createdBy;
    private LocalDateTime createdAt;
    private List<ItemLine> items;

    @Data
    public static class ItemLine {
        private Long itemId;
        private String itemCode;
        private String itemName;
        private BigDecimal qtyMlAdjustment;
        private BigDecimal unitCost;
        private BigDecimal totalCost;
        private String notes;
    }
}