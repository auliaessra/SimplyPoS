package com.simplypos.modules.reporting.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class DashboardQueryRepository {

    @PersistenceContext
    private EntityManager em;

    /** Safe cast: Object → BigDecimal */
    private BigDecimal toBd(Object v) {
        if (v == null)
            return BigDecimal.ZERO;
        if (v instanceof BigDecimal bd)
            return bd;
        if (v instanceof Number n)
            return BigDecimal.valueOf(n.doubleValue());
        return BigDecimal.ZERO;
    }

    /** Safe cast: Object → Long */
    private Long toLong(Object v) {
        if (v == null)
            return 0L;
        if (v instanceof Number n)
            return n.longValue();
        return 0L;
    }

    /**
     * CATATAN PENTING:
     * Gunakan CAST(:storeId AS bigint) pada setiap parameter nullable
     * agar PostgreSQL dapat menentukan tipe data parameter secara eksplisit.
     * Tanpa CAST, PostgreSQL akan error:
     * "could not determine data type of parameter $N"
     * ketika parameter bernilai NULL (mis. SUPER_ADMIN lintas cabang).
     */

    public BigDecimal sumSales(Long storeId, LocalDate from, LocalDate to) {
        String sql = """
                    SELECT COALESCE(SUM(total_amount), 0)
                    FROM sales_orders
                    WHERE status = 'COMPLETED'
                      AND order_date::date BETWEEN :from AND :to
                      AND (CAST(:storeId AS bigint) IS NULL OR store_id = CAST(:storeId AS bigint))
                """;
        return toBd(em.createNativeQuery(sql)
                .setParameter("storeId", storeId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult());
    }

    public Long countSales(Long storeId, LocalDate from, LocalDate to) {
        String sql = """
                    SELECT COUNT(*)
                    FROM sales_orders
                    WHERE status = 'COMPLETED'
                      AND order_date::date BETWEEN :from AND :to
                      AND (CAST(:storeId AS bigint) IS NULL OR store_id = CAST(:storeId AS bigint))
                """;
        return toLong(em.createNativeQuery(sql)
                .setParameter("storeId", storeId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult());
    }

    public BigDecimal sumPurchases(Long storeId, LocalDate from, LocalDate to) {
        String sql = """
                    SELECT COALESCE(SUM(total_amount), 0)
                    FROM purchase_invoices
                    WHERE status IN ('POSTED','PARTIAL_PAID','PAID')
                      AND invoice_date BETWEEN :from AND :to
                      AND (CAST(:storeId AS bigint) IS NULL OR store_id = CAST(:storeId AS bigint))
                """;
        return toBd(em.createNativeQuery(sql)
                .setParameter("storeId", storeId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult());
    }

    public BigDecimal sumCogs(Long storeId, LocalDate from, LocalDate to) {
        String sql = """
                    SELECT COALESCE(SUM(soi.total_cost), 0)
                    FROM sales_order_items soi
                    JOIN sales_orders so ON so.id = soi.sales_order_id
                    WHERE so.status = 'COMPLETED'
                      AND so.order_date::date BETWEEN :from AND :to
                      AND (CAST(:storeId AS bigint) IS NULL OR so.store_id = CAST(:storeId AS bigint))
                """;
        return toBd(em.createNativeQuery(sql)
                .setParameter("storeId", storeId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult());
    }

    public BigDecimal totalAr(Long storeId) {
        String sql = """
                    SELECT COALESCE(SUM(amount), 0)
                    FROM customer_ledgers
                    WHERE (CAST(:storeId AS bigint) IS NULL OR store_id = CAST(:storeId AS bigint))
                """;
        return toBd(em.createNativeQuery(sql)
                .setParameter("storeId", storeId)
                .getSingleResult());
    }

    public BigDecimal totalAp(Long storeId) {
        String sql = """
                    SELECT COALESCE(SUM(amount), 0)
                    FROM vendor_ledgers
                    WHERE (CAST(:storeId AS bigint) IS NULL OR store_id = CAST(:storeId AS bigint))
                """;
        return toBd(em.createNativeQuery(sql)
                .setParameter("storeId", storeId)
                .getSingleResult());
    }

    public BigDecimal totalInventoryValue(Long storeId) {
        String sql = """
                    SELECT COALESCE(SUM(isp.stock_qty_ml * isp.buy_price), 0)
                    FROM item_store_prices isp
                    WHERE (CAST(:storeId AS bigint) IS NULL OR isp.store_id = CAST(:storeId AS bigint))
                """;
        return toBd(em.createNativeQuery(sql)
                .setParameter("storeId", storeId)
                .getSingleResult());
    }

    public Long lowStockCount(Long storeId) {
        String sql = """
                    SELECT COUNT(*)
                    FROM item_store_prices
                    WHERE stock_qty_ml <= min_stock_ml
                      AND (CAST(:storeId AS bigint) IS NULL OR store_id = CAST(:storeId AS bigint))
                """;
        return toLong(em.createNativeQuery(sql)
                .setParameter("storeId", storeId)
                .getSingleResult());
    }

    public List<Object[]> salesChart(Long storeId, LocalDate from, LocalDate to, boolean daily) {
        String groupExpr = daily
                ? "so.order_date::date"
                : "DATE_TRUNC('month', so.order_date)";

        String sql = "SELECT " + groupExpr + " AS d, COALESCE(SUM(so.total_amount), 0) " +
                "FROM sales_orders so " +
                "WHERE so.status = 'COMPLETED' " +
                "AND so.order_date::date BETWEEN :from AND :to " +
                "AND (CAST(:storeId AS bigint) IS NULL OR so.store_id = CAST(:storeId AS bigint)) " +
                "GROUP BY d ORDER BY d";
        return em.createNativeQuery(sql)
                .setParameter("storeId", storeId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
    }

    public List<Object[]> topProducts(Long storeId, LocalDate from, LocalDate to, int limit) {
        String sql = """
                    SELECT i.id, i.code, i.name,
                           COALESCE(SUM(soi.qty_ml), 0),
                           COALESCE(SUM(soi.total_price), 0)
                    FROM sales_order_items soi
                    JOIN sales_orders so ON so.id = soi.sales_order_id
                    JOIN items i ON i.id = soi.item_id
                    WHERE so.status = 'COMPLETED'
                      AND so.order_date::date BETWEEN :from AND :to
                      AND (CAST(:storeId AS bigint) IS NULL OR so.store_id = CAST(:storeId AS bigint))
                    GROUP BY i.id, i.code, i.name
                    ORDER BY 5 DESC LIMIT :lim
                """;
        return em.createNativeQuery(sql)
                .setParameter("storeId", storeId)
                .setParameter("from", from)
                .setParameter("to", to)
                .setParameter("lim", limit)
                .getResultList();
    }

    public List<Object[]> topVendors(Long storeId, LocalDate from, LocalDate to, int limit) {
        String sql = """
                    SELECT v.id, v.name,
                           COALESCE(SUM(pi.total_amount), 0), COUNT(pi.id)
                    FROM purchase_invoices pi
                    JOIN vendors v ON v.id = pi.vendor_id
                    WHERE pi.status IN ('POSTED','PARTIAL_PAID','PAID')
                      AND pi.invoice_date BETWEEN :from AND :to
                      AND (CAST(:storeId AS bigint) IS NULL OR pi.store_id = CAST(:storeId AS bigint))
                    GROUP BY v.id, v.name
                    ORDER BY 3 DESC LIMIT :lim
                """;
        return em.createNativeQuery(sql)
                .setParameter("storeId", storeId)
                .setParameter("from", from)
                .setParameter("to", to)
                .setParameter("lim", limit)
                .getResultList();
    }

    public List<Object[]> purchaseChart(Long storeId, LocalDate from, LocalDate to, boolean daily) {
        String groupExpr = daily
                ? "invoice_date"
                : "DATE_TRUNC('month', invoice_date)";

        String sql = "SELECT " + groupExpr + " AS d, COALESCE(SUM(total_amount), 0) " +
                "FROM purchase_invoices " +
                "WHERE status IN ('POSTED','PARTIAL_PAID','PAID') " +
                "AND invoice_date BETWEEN :from AND :to " +
                "AND (CAST(:storeId AS bigint) IS NULL OR store_id = CAST(:storeId AS bigint)) " +
                "GROUP BY d ORDER BY d";
        return em.createNativeQuery(sql)
                .setParameter("storeId", storeId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
    }
}