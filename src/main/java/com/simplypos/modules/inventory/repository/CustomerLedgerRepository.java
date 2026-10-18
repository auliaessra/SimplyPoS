package com.simplypos.modules.inventory.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.simplypos.modules.inventory.entity.CustomerLedger;

public interface CustomerLedgerRepository extends JpaRepository<CustomerLedger, Long> {

    List<CustomerLedger> findByStoreIdAndCustomerIdOrderByIdAsc(Long storeId, Long customerId);

    List<CustomerLedger> findByStoreIdOrderByIdDesc(Long storeId);

    @Query("SELECT COALESCE(SUM(cl.amount),0) FROM CustomerLedger cl " +
            "WHERE cl.store.id = :storeId AND cl.customer.id = :customerId")
    BigDecimal sumByStoreAndCustomer(@Param("storeId") Long storeId,
            @Param("customerId") Long customerId);

    /**
     * Ringkasan AR per customer (hanya yang memiliki saldo > 0).
     * Jika storeId null → lintas cabang (untuk SUPER_ADMIN).
     */
    @Query("""
                SELECT cl.customer.id,
                       cl.customer.code,
                       cl.customer.name,
                       cl.customer.phone,
                       cl.store.id,
                       cl.store.name,
                       SUM(cl.amount),
                       COUNT(CASE WHEN cl.documentType = 'SALE' THEN 1 END)
                FROM CustomerLedger cl
                WHERE (:storeId IS NULL OR cl.store.id = :storeId)
                GROUP BY cl.customer.id, cl.customer.code, cl.customer.name,
                         cl.customer.phone, cl.store.id, cl.store.name
                HAVING SUM(cl.amount) > 0
                ORDER BY SUM(cl.amount) DESC
            """)
    List<Object[]> findCustomerArSummary(@Param("storeId") Long storeId);
}