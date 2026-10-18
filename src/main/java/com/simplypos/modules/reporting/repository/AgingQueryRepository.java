package com.simplypos.modules.reporting.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class AgingQueryRepository {

    @PersistenceContext
    private EntityManager em;

    /**
     * Aging AR — dikelompokkan berdasarkan umur piutang (dari order_date).
     * Gunakan CAST(:storeId AS bigint) agar PostgreSQL dapat menentukan tipe data
     * parameter nullable dengan benar.
     */
    public List<Object[]> agingAr(Long storeId, LocalDate asOf) {
        String sql = """
                    SELECT c.id, c.code, c.name,
                      COALESCE(SUM(CASE WHEN (CAST(:asOf AS date) - so.order_date::date) <= 0
                                        THEN so.total_amount ELSE 0 END), 0) AS current,
                      COALESCE(SUM(CASE WHEN (CAST(:asOf AS date) - so.order_date::date) BETWEEN 1 AND 30
                                        THEN so.total_amount ELSE 0 END), 0) AS d1_30,
                      COALESCE(SUM(CASE WHEN (CAST(:asOf AS date) - so.order_date::date) BETWEEN 31 AND 60
                                        THEN so.total_amount ELSE 0 END), 0) AS d31_60,
                      COALESCE(SUM(CASE WHEN (CAST(:asOf AS date) - so.order_date::date) BETWEEN 61 AND 90
                                        THEN so.total_amount ELSE 0 END), 0) AS d61_90,
                      COALESCE(SUM(CASE WHEN (CAST(:asOf AS date) - so.order_date::date) > 90
                                        THEN so.total_amount ELSE 0 END), 0) AS d90p,
                      COALESCE(SUM(so.total_amount), 0) AS total
                    FROM sales_orders so
                    JOIN customers c ON c.id = so.customer_id
                    WHERE so.payment_method = 'CREDIT'
                      AND so.status = 'COMPLETED'
                      AND (CAST(:storeId AS bigint) IS NULL OR so.store_id = CAST(:storeId AS bigint))
                    GROUP BY c.id, c.code, c.name
                    HAVING COALESCE(SUM(so.total_amount), 0) > 0
                    ORDER BY total DESC
                """;

        return em.createNativeQuery(sql)
                .setParameter("storeId", storeId)
                .setParameter("asOf", asOf)
                .getResultList();
    }

    /**
     * Aging AP — dikelompokkan berdasarkan umur hutang (dari invoice_date).
     */
    public List<Object[]> agingAp(Long storeId, LocalDate asOf) {
        String sql = """
                    SELECT v.id, v.code, v.name,
                      COALESCE(SUM(CASE WHEN (CAST(:asOf AS date) - pi.invoice_date) <= 0
                                        THEN pi.total_amount - pi.paid_amount ELSE 0 END), 0) AS current,
                      COALESCE(SUM(CASE WHEN (CAST(:asOf AS date) - pi.invoice_date) BETWEEN 1 AND 30
                                        THEN pi.total_amount - pi.paid_amount ELSE 0 END), 0) AS d1_30,
                      COALESCE(SUM(CASE WHEN (CAST(:asOf AS date) - pi.invoice_date) BETWEEN 31 AND 60
                                        THEN pi.total_amount - pi.paid_amount ELSE 0 END), 0) AS d31_60,
                      COALESCE(SUM(CASE WHEN (CAST(:asOf AS date) - pi.invoice_date) BETWEEN 61 AND 90
                                        THEN pi.total_amount - pi.paid_amount ELSE 0 END), 0) AS d61_90,
                      COALESCE(SUM(CASE WHEN (CAST(:asOf AS date) - pi.invoice_date) > 90
                                        THEN pi.total_amount - pi.paid_amount ELSE 0 END), 0) AS d90p,
                      COALESCE(SUM(pi.total_amount - pi.paid_amount), 0) AS total
                    FROM purchase_invoices pi
                    JOIN vendors v ON v.id = pi.vendor_id
                    WHERE pi.status IN ('POSTED','PARTIAL_PAID')
                      AND (CAST(:storeId AS bigint) IS NULL OR pi.store_id = CAST(:storeId AS bigint))
                    GROUP BY v.id, v.code, v.name
                    HAVING COALESCE(SUM(pi.total_amount - pi.paid_amount), 0) > 0
                    ORDER BY total DESC
                """;

        return em.createNativeQuery(sql)
                .setParameter("storeId", storeId)
                .setParameter("asOf", asOf)
                .getResultList();
    }
}