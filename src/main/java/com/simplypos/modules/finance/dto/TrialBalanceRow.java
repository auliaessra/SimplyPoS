package com.simplypos.modules.finance.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrialBalanceRow {
    private Long coaId;
    private String accountCode;
    private String accountName;
    private String accountType;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;
    private BigDecimal balance; // debit - credit
}