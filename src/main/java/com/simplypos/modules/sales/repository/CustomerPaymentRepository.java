package com.simplypos.modules.sales.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.simplypos.modules.sales.entity.CustomerPayment;

public interface CustomerPaymentRepository extends JpaRepository<CustomerPayment, Long> {
    Page<CustomerPayment> findByStoreId(Long storeId, Pageable pageable);

    Page<CustomerPayment> findAllByOrderByIdDesc(Pageable pageable);
}