package com.simplypos.modules.reporting.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.simplypos.config.StoreContext;
import com.simplypos.core.security.SecurityUtils;
import com.simplypos.modules.master.common.exception.BusinessException;
import com.simplypos.modules.reporting.dto.AgingReportResponse;
import com.simplypos.modules.reporting.dto.InventoryReportResponse;
import com.simplypos.modules.reporting.dto.PurchaseReportResponse;
import com.simplypos.modules.reporting.dto.SalesChartPoint;
import com.simplypos.modules.reporting.dto.SalesReportResponse;
import com.simplypos.modules.reporting.dto.TopProductResponse;
import com.simplypos.modules.reporting.repository.AgingQueryRepository;
import com.simplypos.modules.reporting.repository.DashboardQueryRepository;
import com.simplypos.modules.reporting.utils.QueryResultUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final DashboardQueryRepository dashRepo;
    private final AgingQueryRepository agingRepo;
    private final StoreContext storeContext;

    @PersistenceContext
    private EntityManager em;

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

    // ==================== SALES REPORT ====================
    public SalesReportResponse salesReport(Long storeId, LocalDate from, LocalDate to) {
        Long s = effectiveStore(storeId);

        SalesReportResponse r = new SalesReportResponse();
        r.setFrom(from);
        r.setTo(to);
        r.setStoreId(s);

        r.setTotalRevenue(dashRepo.sumSales(s, from, to));
        r.setTotalCogs(dashRepo.sumCogs(s, from, to));
        r.setTotalProfit(r.getTotalRevenue().subtract(r.getTotalCogs()));
        r.setTotalTransactions(dashRepo.countSales(s, from, to));

        // Daily chart
        List<SalesChartPoint> daily = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM");

        Map<LocalDate, BigDecimal> map = new HashMap<>();
        for (Object[] row : dashRepo.salesChart(s, from, to, true)) {
            LocalDate d = QueryResultUtils.toLocalDate(row[0]);
            BigDecimal v = toBigDecimal(row[1]);
            if (d != null)
                map.put(d, v);
        }
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            daily.add(new SalesChartPoint(d.format(fmt), map.getOrDefault(d, BigDecimal.ZERO)));
        }
        r.setDailyChart(daily);

        // Top products
        r.setTopProducts(dashRepo.topProducts(s, from, to, 10).stream()
                .map(x -> new TopProductResponse(
                        ((Number) x[0]).longValue(),
                        (String) x[1],
                        (String) x[2],
                        toBigDecimal(x[3]),
                        toBigDecimal(x[4])))
                .toList());

        return r;
    }

    // ==================== PURCHASE REPORT ====================
    public PurchaseReportResponse purchaseReport(Long storeId, LocalDate from, LocalDate to) {
        Long s = effectiveStore(storeId);

        PurchaseReportResponse r = new PurchaseReportResponse();
        r.setFrom(from);
        r.setTo(to);
        r.setStoreId(s);
        r.setTotalPurchase(dashRepo.sumPurchases(s, from, to));

        // Daily chart
        List<SalesChartPoint> daily = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM");

        Map<LocalDate, BigDecimal> map = new HashMap<>();
        for (Object[] row : dashRepo.purchaseChart(s, from, to, true)) {
            LocalDate d = QueryResultUtils.toLocalDate(row[0]);
            BigDecimal v = toBigDecimal(row[1]);
            if (d != null)
                map.put(d, v);
        }
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            daily.add(new SalesChartPoint(d.format(fmt), map.getOrDefault(d, BigDecimal.ZERO)));
        }
        r.setDailyChart(daily);

        // Top vendors
        r.setTopVendors(dashRepo.topVendors(s, from, to, 10).stream()
                .map(x -> {
                    var v = new PurchaseReportResponse.VendorSummary();
                    v.setVendorId(((Number) x[0]).longValue());
                    v.setVendorName((String) x[1]);
                    v.setTotalAmount(toBigDecimal(x[2]));
                    v.setInvoiceCount(((Number) x[3]).longValue());
                    return v;
                }).toList());

        // Total paid
        BigDecimal paid = toBigDecimal(em.createNativeQuery("""
                    SELECT COALESCE(SUM(paid_amount),0)
                    FROM purchase_invoices
                    WHERE status IN ('POSTED','PARTIAL_PAID','PAID')
                    AND invoice_date BETWEEN :from AND :to
                    AND (CAST(:storeId AS bigint) IS NULL OR store_id = CAST(:storeId AS bigint))
                """)
                .setParameter("storeId", s)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult());
        r.setTotalPaid(paid);

        return r;
    }

    // ==================== INVENTORY REPORT ====================
    @SuppressWarnings("unchecked")
    public InventoryReportResponse inventoryReport(Long storeId) {
        Long s = effectiveStore(storeId);

        String sql = """
                    SELECT i.id, i.code, i.name,
                        st.name,
                        isp.stock_qty_ml, isp.min_stock_ml,
                        isp.buy_price,
                        isp.stock_qty_ml * isp.buy_price
                    FROM item_store_prices isp
                    JOIN items i ON i.id = isp.item_id
                    JOIN stores st ON st.id = isp.store_id
                    WHERE (CAST(:storeId AS bigint) IS NULL OR isp.store_id = CAST(:storeId AS bigint))
                    ORDER BY i.code, st.name
                """;

        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("storeId", s)
                .getResultList();

        InventoryReportResponse r = new InventoryReportResponse();
        List<InventoryReportResponse.InventoryItem> items = new ArrayList<>();
        List<InventoryReportResponse.InventoryItem> lowStock = new ArrayList<>();
        BigDecimal totalValue = BigDecimal.ZERO;

        for (Object[] row : rows) {
            InventoryReportResponse.InventoryItem item = new InventoryReportResponse.InventoryItem();
            item.setItemId(((Number) row[0]).longValue());
            item.setItemCode((String) row[1]);
            item.setItemName((String) row[2]);
            item.setStoreName((String) row[3]);
            item.setStockQtyMl(toBigDecimal(row[4]));
            item.setMinStockMl(toBigDecimal(row[5]));
            item.setUnitCost(toBigDecimal(row[6]));
            item.setTotalValue(toBigDecimal(row[7]));
            item.setLowStock(item.getStockQtyMl().compareTo(item.getMinStockMl()) <= 0);

            items.add(item);
            totalValue = totalValue.add(item.getTotalValue());
            if (item.isLowStock())
                lowStock.add(item);
        }

        r.setItems(items);
        r.setLowStockItems(lowStock);
        r.setTotalValue(totalValue);
        r.setTotalItems((long) items.size());
        r.setLowStockCount((long) lowStock.size());
        return r;
    }

    // ==================== AGING AR ====================
    public AgingReportResponse agingAr(Long storeId, LocalDate asOf) {
        Long s = effectiveStore(storeId);
        AgingReportResponse r = new AgingReportResponse();

        List<AgingReportResponse.AgingRow> rows = new ArrayList<>();
        BigDecimal[] totals = new BigDecimal[] {
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO };

        for (Object[] row : agingRepo.agingAr(s, asOf)) {
            var ar = new AgingReportResponse.AgingRow();
            ar.setPartyId(((Number) row[0]).longValue());
            ar.setPartyCode((String) row[1]);
            ar.setPartyName((String) row[2]);
            ar.setCurrent(toBigDecimal(row[3]));
            ar.setDays1to30(toBigDecimal(row[4]));
            ar.setDays31to60(toBigDecimal(row[5]));
            ar.setDays61to90(toBigDecimal(row[6]));
            ar.setOver90(toBigDecimal(row[7]));
            ar.setTotal(toBigDecimal(row[8]));
            rows.add(ar);

            totals[0] = totals[0].add(ar.getCurrent());
            totals[1] = totals[1].add(ar.getDays1to30());
            totals[2] = totals[2].add(ar.getDays31to60());
            totals[3] = totals[3].add(ar.getDays61to90());
            totals[4] = totals[4].add(ar.getOver90());
            totals[5] = totals[5].add(ar.getTotal());
        }

        r.setRows(rows);
        r.setTotalCurrent(totals[0]);
        r.setTotal1to30(totals[1]);
        r.setTotal31to60(totals[2]);
        r.setTotal61to90(totals[3]);
        r.setTotalOver90(totals[4]);
        r.setGrandTotal(totals[5]);
        return r;
    }

    // ==================== AGING AP ====================
    public AgingReportResponse agingAp(Long storeId, LocalDate asOf) {
        Long s = effectiveStore(storeId);
        AgingReportResponse r = new AgingReportResponse();

        List<AgingReportResponse.AgingRow> rows = new ArrayList<>();
        BigDecimal[] totals = new BigDecimal[] {
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO };

        for (Object[] row : agingRepo.agingAp(s, asOf)) {
            var ar = new AgingReportResponse.AgingRow();
            ar.setPartyId(((Number) row[0]).longValue());
            ar.setPartyCode((String) row[1]);
            ar.setPartyName((String) row[2]);
            ar.setCurrent(toBigDecimal(row[3]));
            ar.setDays1to30(toBigDecimal(row[4]));
            ar.setDays31to60(toBigDecimal(row[5]));
            ar.setDays61to90(toBigDecimal(row[6]));
            ar.setOver90(toBigDecimal(row[7]));
            ar.setTotal(toBigDecimal(row[8]));
            rows.add(ar);

            totals[0] = totals[0].add(ar.getCurrent());
            totals[1] = totals[1].add(ar.getDays1to30());
            totals[2] = totals[2].add(ar.getDays31to60());
            totals[3] = totals[3].add(ar.getDays61to90());
            totals[4] = totals[4].add(ar.getOver90());
            totals[5] = totals[5].add(ar.getTotal());
        }

        r.setRows(rows);
        r.setTotalCurrent(totals[0]);
        r.setTotal1to30(totals[1]);
        r.setTotal31to60(totals[2]);
        r.setTotal61to90(totals[3]);
        r.setTotalOver90(totals[4]);
        r.setGrandTotal(totals[5]);
        return r;
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