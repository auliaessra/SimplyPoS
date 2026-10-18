package com.simplypos.modules.sales.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.simplypos.modules.sales.entity.SalesOrder;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    Page<SalesOrder> findByStoreId(Long storeId, Pageable pageable);

    Page<SalesOrder> findAllByOrderByIdDesc(Pageable pageable);

    Optional<SalesOrder> findByOrderNo(String orderNo);

    /**
     * Cari SO CREDIT yang masih outstanding (belum lunas) untuk customer tertentu.
     * Filter opsional by store.
     */
    @Query("""
                SELECT so FROM SalesOrder so
                WHERE so.paymentMethod = 'CREDIT'
                  AND so.status = 'COMPLETED'
                  AND so.customer.id = :customerId
                  AND (:storeId IS NULL OR so.store.id = :storeId)
                  AND so.totalAmount > COALESCE(
                      (SELECT COALESCE(SUM(-cl.amount), 0)
                       FROM CustomerLedger cl
                       WHERE cl.documentType = 'RECEIPT'
                         AND cl.documentId = so.id),
                      0)
                ORDER BY so.orderDate ASC, so.id ASC
            """)
    List<SalesOrder> findOpenByCustomer(@Param("customerId") Long customerId,
            @Param("storeId") Long storeId);
}