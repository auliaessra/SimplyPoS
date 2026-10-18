package com.simplypos.modules.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemLedgerResponse {
    private Long id;
    private Long storeId;
    private String storeName;
    private Long itemId;
    private String itemCode;
    private String itemName;
    private String documentType;
    private String documentNo;
    private LocalDate transactionDate;
    private BigDecimal qtyMl;
    private BigDecimal unitCost;
    private BigDecimal totalCost;
    private BigDecimal balanceQtyMl;
    private String description;
}