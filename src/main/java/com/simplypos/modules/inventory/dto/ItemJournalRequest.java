package com.simplypos.modules.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ItemJournalRequest {
    private Long storeId;
    @NotNull
    private LocalDate journalDate;
    @NotNull
    private String reason; // OPNAME, DAMAGE, LOSS, FOUND, OTHER
    private String notes;
    @NotEmpty
    private List<ItemLine> items;

    @Data
    public static class ItemLine {
        @NotNull
        private Long itemId;
        @NotNull
        private BigDecimal qtyMlAdjustment; // +/- ML
        private BigDecimal unitCost;
        private String notes;
    }
}