package com.simplypos.modules.master.customer.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.simplypos.modules.master.customer.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByStoreId(Long storeId);

    Optional<Customer> findByStoreIdAndCode(Long storeId, String code);

    boolean existsByStoreIdAndCode(Long storeId, String code);

    // Untuk SUPER_ADMIN (lintas cabang)
    List<Customer> findAllByOrderByStoreIdAscCodeAsc();
}