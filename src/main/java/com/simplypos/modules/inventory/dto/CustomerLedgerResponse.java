package com.simplypos.modules.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerLedgerResponse {
    private Long id;
    private Long storeId;
    private String storeName;
    private Long customerId;
    private String customerCode;
    private String customerName;
    private String documentType;
    private String documentNo;
    private Long documentId;
    private LocalDate transactionDate;
    private BigDecimal amount;
    private BigDecimal balance;
    private String description;
}