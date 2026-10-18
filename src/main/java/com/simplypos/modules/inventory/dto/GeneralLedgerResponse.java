package com.simplypos.modules.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneralLedgerResponse {
    private Long id;
    private Long storeId;
    private String storeName;
    private String accountCode;
    private String accountName;
    private String accountType;
    private String documentType;
    private String documentNo;
    private LocalDate transactionDate;
    private BigDecimal debit;
    private BigDecimal credit;
    private String description;
}