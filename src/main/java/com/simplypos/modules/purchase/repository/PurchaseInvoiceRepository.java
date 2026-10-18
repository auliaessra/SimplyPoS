package com.simplypos.modules.purchase.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.simplypos.modules.purchase.entity.PurchaseInvoice;

public interface PurchaseInvoiceRepository extends JpaRepository<PurchaseInvoice, Long> {

    Page<PurchaseInvoice> findByStoreId(Long storeId, Pageable pageable);

    Page<PurchaseInvoice> findAllByOrderByIdDesc(Pageable pageable);

    Optional<PurchaseInvoice> findByInvoiceNo(String invoiceNo);

    boolean existsByStoreIdAndInvoiceNo(Long storeId, String invoiceNo);

    /**
     * Cari PI yang masih outstanding (POSTED atau PARTIAL_PAID) berdasarkan vendor.
     * Bisa filter by store juga untuk isolasi multi-cabang.
     */
    @Query("SELECT pi FROM PurchaseInvoice pi " +
            "WHERE pi.vendor.id = :vendorId " +
            "  AND pi.status IN ('POSTED', 'PARTIAL_PAID') " +
            "  AND pi.paidAmount < pi.totalAmount " +
            "  AND (:storeId IS NULL OR pi.store.id = :storeId) " +
            "ORDER BY pi.invoiceDate ASC, pi.id ASC")
    List<PurchaseInvoice> findOpenByVendor(
            @Param("vendorId") Long vendorId,
            @Param("storeId") Long storeId);
}