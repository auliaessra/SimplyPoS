package com.simplypos.modules.inventory.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerArSummaryResponse {
    private Long customerId;
    private String customerCode;
    private String customerName;
    private String customerPhone;
    private Long storeId;
    private String storeName;
    private BigDecimal totalAr;
    private Long invoiceCount;
}