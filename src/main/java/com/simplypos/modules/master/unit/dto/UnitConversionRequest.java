package com.simplypos.modules.master.unit.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UnitConversionRequest {
    @NotNull
    private Long fromUnitId;
    @NotNull
    private Long toUnitId;
    @NotNull
    @Positive
    private BigDecimal factor;
    private Boolean isActive = true;
    private String notes;
}