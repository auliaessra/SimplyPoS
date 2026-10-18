package com.simplypos.modules.master.unit.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnitConversionResponse {
    private Long id;
    private Long fromUnitId;
    private String fromUnitCode;
    private String fromUnitName;
    private Long toUnitId;
    private String toUnitCode;
    private String toUnitName;
    private BigDecimal factor;
    private Boolean isActive;
    private String notes;
    private String expression; // "1 Liter = 1000 ML"
}