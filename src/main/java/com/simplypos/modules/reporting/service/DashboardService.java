package com.simplypos.modules.reporting.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.simplypos.config.StoreContext;
import com.simplypos.core.security.SecurityUtils;
import com.simplypos.modules.master.common.exception.BusinessException;
import com.simplypos.modules.reporting.dto.DashboardKpiResponse;
import com.simplypos.modules.reporting.dto.SalesChartPoint;
import com.simplypos.modules.reporting.dto.TopProductResponse;
import com.simplypos.modules.reporting.repository.DashboardQueryRepository;
import com.simplypos.modules.reporting.utils.QueryResultUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardQueryRepository repo;
    private final StoreContext storeContext;

    private Long effectiveStore(Long override) {
        if (override != null)
            return override;
        Long ctx = storeContext.getCurrentStoreId();
        if (ctx != null)
            return ctx;
        if (SecurityUtils.isSuperAdmin())
            return null;
        throw new BusinessException("error.store.context");
    }

    public DashboardKpiResponse kpi(Long storeId) {
        Long s = effectiveStore(storeId);
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

        DashboardKpiResponse k = new DashboardKpiResponse();
        k.setTodaySales(repo.sumSales(s, today, today));
        k.setTodaySalesCount(repo.countSales(s, today, today));
        k.setTodayPurchases(repo.sumPurchases(s, today, today));

        BigDecimal monthSales = repo.sumSales(s, monthStart, monthEnd);
        BigDecimal monthCogs = repo.sumCogs(s, monthStart, monthEnd);
        k.setMonthSales(monthSales);
        k.setMonthPurchases(repo.sumPurchases(s, monthStart, monthEnd));
        k.setMonthProfit(monthSales.subtract(monthCogs));

        k.setTotalAr(repo.totalAr(s));
        k.setTotalAp(repo.totalAp(s));
        k.setTotalInventoryValue(repo.totalInventoryValue(s));
        k.setLowStockCount(repo.lowStockCount(s));
        return k;
    }

    /**
     * Sales trend harian (default 30 hari).
     */
    public List<SalesChartPoint> salesTrend(Long storeId, int days) {
        Long s = effectiveStore(storeId);
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(days - 1);

        List<Object[]> rows = repo.salesChart(s, from, to, true);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM");

        // Build map dengan helper aman
        Map<LocalDate, BigDecimal> map = new HashMap<>();
        for (Object[] r : rows) {
            LocalDate d = QueryResultUtils.toLocalDate(r[0]);
            BigDecimal v = toBigDecimal(r[1]);
            if (d != null)
                map.put(d, v);
        }

        List<SalesChartPoint> result = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate d = from.plusDays(i);
            result.add(new SalesChartPoint(
                    d.format(fmt),
                    map.getOrDefault(d, BigDecimal.ZERO)));
        }
        return result;
    }

    /**
     * Sales trend bulanan (default 12 bulan).
     */
    public List<SalesChartPoint> salesTrendMonthly(Long storeId, int months) {
        Long s = effectiveStore(storeId);
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusMonths(months - 1).withDayOfMonth(1);

        List<Object[]> rows = repo.salesChart(s, from, to, false);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM yyyy");

        Map<YearMonth, BigDecimal> map = new HashMap<>();
        for (Object[] r : rows) {
            YearMonth ym = QueryResultUtils.toYearMonth(r[0]);
            BigDecimal v = toBigDecimal(r[1]);
            if (ym != null)
                map.put(ym, v);
        }

        List<SalesChartPoint> result = new ArrayList<>();
        YearMonth start = YearMonth.from(from);
        for (int i = 0; i < months; i++) {
            YearMonth ym = start.plusMonths(i);
            result.add(new SalesChartPoint(
                    ym.format(fmt),
                    map.getOrDefault(ym, BigDecimal.ZERO)));
        }
        return result;
    }

    public List<TopProductResponse> topProducts(Long storeId, int days, int limit) {
        Long s = effectiveStore(storeId);
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(days - 1);

        return repo.topProducts(s, from, to, limit).stream()
                .map(r -> new TopProductResponse(
                        ((Number) r[0]).longValue(),
                        (String) r[1],
                        (String) r[2],
                        toBigDecimal(r[3]),
                        toBigDecimal(r[4])))
                .toList();
    }

    /**
     * Purchase trend harian.
     */
    public List<SalesChartPoint> purchaseTrend(Long storeId, int days) {
        Long s = effectiveStore(storeId);
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(days - 1);

        List<Object[]> rows = repo.purchaseChart(s, from, to, true);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM");

        Map<LocalDate, BigDecimal> map = new HashMap<>();
        for (Object[] r : rows) {
            LocalDate d = QueryResultUtils.toLocalDate(r[0]);
            BigDecimal v = toBigDecimal(r[1]);
            if (d != null)
                map.put(d, v);
        }

        List<SalesChartPoint> result = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate d = from.plusDays(i);
            result.add(new SalesChartPoint(
                    d.format(fmt),
                    map.getOrDefault(d, BigDecimal.ZERO)));
        }
        return result;
    }

    /** Helper: aman konversi Number → BigDecimal */
    private BigDecimal toBigDecimal(Object value) {
        if (value == null)
            return BigDecimal.ZERO;
        if (value instanceof BigDecimal bd)
            return bd;
        if (value instanceof Number n)
            return BigDecimal.valueOf(n.doubleValue());
        return BigDecimal.ZERO;
    }
}