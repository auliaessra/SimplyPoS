package com.simplypos.modules.reporting.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesChartPoint {
    private String label; // "01 Sep" atau "Sep 2026"
    private BigDecimal value;
}