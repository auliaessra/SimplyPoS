package com.simplypos.modules.reporting.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopProductResponse {
    private Long itemId;
    private String itemCode;
    private String itemName;
    private BigDecimal qtyMl;
    private BigDecimal revenue;
}