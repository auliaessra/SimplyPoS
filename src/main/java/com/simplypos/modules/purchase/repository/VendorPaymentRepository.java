package com.simplypos.modules.purchase.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.simplypos.modules.purchase.entity.VendorPayment;

public interface VendorPaymentRepository extends JpaRepository<VendorPayment, Long> {

    Page<VendorPayment> findByStoreId(Long storeId, Pageable pageable);

    Page<VendorPayment> findAllByOrderByIdDesc(Pageable pageable);
}