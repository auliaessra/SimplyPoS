package com.simplypos.modules.inventory.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.simplypos.modules.inventory.entity.VendorLedger;

public interface VendorLedgerRepository extends JpaRepository<VendorLedger, Long> {

    List<VendorLedger> findByStoreIdAndVendorIdOrderByIdAsc(Long storeId, Long vendorId);

    List<VendorLedger> findByStoreIdOrderByIdDesc(Long storeId);

    @Query("SELECT COALESCE(SUM(vl.amount),0) FROM VendorLedger vl " +
            "WHERE vl.store.id = :storeId AND vl.vendor.id = :vendorId")
    BigDecimal sumByStoreAndVendor(@Param("storeId") Long storeId,
            @Param("vendorId") Long vendorId);
}