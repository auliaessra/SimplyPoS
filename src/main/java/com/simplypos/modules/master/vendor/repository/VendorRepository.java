package com.simplypos.modules.master.vendor.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.simplypos.modules.master.vendor.entity.Vendor;

public interface VendorRepository extends JpaRepository<Vendor, Long> {
    List<Vendor> findByStoreId(Long storeId);

    Optional<Vendor> findByStoreIdAndCode(Long storeId, String code);

    boolean existsByStoreIdAndCode(Long storeId, String code);

    // Untuk SUPER_ADMIN (lintas cabang)
    List<Vendor> findAllByOrderByStoreIdAscCodeAsc();
}