package com.simplypos.modules.reporting.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class AgingReportResponse {
    private BigDecimal totalCurrent = BigDecimal.ZERO;
    private BigDecimal total1to30 = BigDecimal.ZERO;
    private BigDecimal total31to60 = BigDecimal.ZERO;
    private BigDecimal total61to90 = BigDecimal.ZERO;
    private BigDecimal totalOver90 = BigDecimal.ZERO;
    private BigDecimal grandTotal = BigDecimal.ZERO;

    private List<AgingRow> rows;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgingRow {
        private Long partyId;
        private String partyCode;
        private String partyName;
        private BigDecimal current;
        private BigDecimal days1to30;
        private BigDecimal days31to60;
        private BigDecimal days61to90;
        private BigDecimal over90;
        private BigDecimal total;
    }
}