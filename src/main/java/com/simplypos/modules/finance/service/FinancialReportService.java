package com.simplypos.modules.finance.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.simplypos.config.StoreContext;
import com.simplypos.core.security.SecurityUtils;
import com.simplypos.modules.finance.dto.BalanceSheetReport;
import com.simplypos.modules.finance.dto.ProfitLossReport;
import com.simplypos.modules.finance.dto.TrialBalanceRow;
import com.simplypos.modules.finance.repository.GeneralLedgerRepository;
import com.simplypos.modules.master.common.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FinancialReportService {

    private final GeneralLedgerRepository glRepo;
    private final StoreContext storeContext;

    // ===================== TRIAL BALANCE =====================
    public List<TrialBalanceRow> trialBalance(Long storeId, LocalDate from, LocalDate to) {
        Long effectiveStore = resolveStoreId(storeId);
        return glRepo.trialBalance(effectiveStore, from, to).stream()
                .map(row -> {
                    BigDecimal debit = (BigDecimal) row[4];
                    BigDecimal credit = (BigDecimal) row[5];
                    return new TrialBalanceRow(
                            ((Number) row[0]).longValue(),
                            (String) row[1],
                            (String) row[2],
                            (String) row[3],
                            debit, credit,
                            debit.subtract(credit));
                }).toList();
    }

    // ===================== PROFIT & LOSS =====================
    public ProfitLossReport profitLoss(Long storeId, LocalDate from, LocalDate to) {
        Long effectiveStore = resolveStoreId(storeId);

        ProfitLossReport report = new ProfitLossReport();
        report.setFrom(from);
        report.setTo(to);
        report.setStoreId(effectiveStore);

        List<Object[]> rows = glRepo.trialBalance(effectiveStore, from, to);

        List<ProfitLossReport.ReportLine> revenues = new ArrayList<>();
        List<ProfitLossReport.ReportLine> expenses = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (Object[] row : rows) {
            String accountType = (String) row[3];
            BigDecimal debit = (BigDecimal) row[4];
            BigDecimal credit = (BigDecimal) row[5];

            if ("REVENUE".equals(accountType)) {
                BigDecimal amount = credit.subtract(debit);
                if (amount.compareTo(BigDecimal.ZERO) != 0) {
                    revenues.add(new ProfitLossReport.ReportLine(
                            (String) row[1], (String) row[2], amount));
                    totalRevenue = totalRevenue.add(amount);
                }
            } else if ("EXPENSE".equals(accountType)) {
                BigDecimal amount = debit.subtract(credit);
                if (amount.compareTo(BigDecimal.ZERO) != 0) {
                    expenses.add(new ProfitLossReport.ReportLine(
                            (String) row[1], (String) row[2], amount));
                    totalExpense = totalExpense.add(amount);
                }
            }
        }

        report.setRevenues(revenues);
        report.setExpenses(expenses);
        report.setTotalRevenue(totalRevenue);
        report.setTotalExpense(totalExpense);
        report.setNetProfit(totalRevenue.subtract(totalExpense));

        return report;
    }

    // ===================== BALANCE SHEET =====================
    public BalanceSheetReport balanceSheet(Long storeId, LocalDate asOf) {
        Long effectiveStore = resolveStoreId(storeId);

        BalanceSheetReport report = new BalanceSheetReport();
        report.setAsOf(asOf);
        report.setStoreId(effectiveStore);

        // Balance Sheet = saldo kumulatif dari awal sampai asOf
        LocalDate fromEpoch = LocalDate.of(2000, 1, 1);

        List<Object[]> rows = glRepo.trialBalance(effectiveStore, fromEpoch, asOf);

        List<BalanceSheetReport.ReportLine> assets = new ArrayList<>();
        List<BalanceSheetReport.ReportLine> liabilities = new ArrayList<>();
        List<BalanceSheetReport.ReportLine> equities = new ArrayList<>();

        BigDecimal totalAssets = BigDecimal.ZERO;
        BigDecimal totalLiabilities = BigDecimal.ZERO;
        BigDecimal totalEquities = BigDecimal.ZERO;
        BigDecimal netProfit = BigDecimal.ZERO;

        for (Object[] row : rows) {
            String accountType = (String) row[3];
            BigDecimal debit = (BigDecimal) row[4];
            BigDecimal credit = (BigDecimal) row[5];

            switch (accountType) {
                case "ASSET" -> {
                    BigDecimal amount = debit.subtract(credit);
                    if (amount.compareTo(BigDecimal.ZERO) != 0) {
                        assets.add(new BalanceSheetReport.ReportLine(
                                (String) row[1], (String) row[2], amount));
                        totalAssets = totalAssets.add(amount);
                    }
                }
                case "LIABILITY" -> {
                    BigDecimal amount = credit.subtract(debit);
                    if (amount.compareTo(BigDecimal.ZERO) != 0) {
                        liabilities.add(new BalanceSheetReport.ReportLine(
                                (String) row[1], (String) row[2], amount));
                        totalLiabilities = totalLiabilities.add(amount);
                    }
                }
                case "EQUITY" -> {
                    BigDecimal amount = credit.subtract(debit);
                    if (amount.compareTo(BigDecimal.ZERO) != 0) {
                        equities.add(new BalanceSheetReport.ReportLine(
                                (String) row[1], (String) row[2], amount));
                        totalEquities = totalEquities.add(amount);
                    }
                }
                case "REVENUE" -> netProfit = netProfit.add(credit.subtract(debit));
                case "EXPENSE" -> netProfit = netProfit.subtract(debit.subtract(credit));
            }
        }

        report.setAssets(assets);
        report.setLiabilities(liabilities);
        report.setEquities(equities);
        report.setTotalAssets(totalAssets);
        report.setTotalLiabilities(totalLiabilities);
        report.setTotalEquities(totalEquities);
        report.setNetProfit(netProfit);

        return report;
    }

    private Long resolveStoreId(Long override) {
        if (override != null)
            return override;
        Long ctx = storeContext.getCurrentStoreId();
        if (ctx != null)
            return ctx;
        if (SecurityUtils.isSuperAdmin())
            return null;
        throw new BusinessException("error.store.context");
    }
}