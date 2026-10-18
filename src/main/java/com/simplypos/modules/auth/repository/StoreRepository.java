package com.simplypos.modules.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.simplypos.entities.Store;

public interface StoreRepository extends JpaRepository<Store, Long> {
    Optional<Store> findByCode(String code);

    boolean existsByCode(String code);

    List<Store> findAllByOrderByCodeAsc();

    List<Store> findByIsActiveTrueOrderByCodeAsc();

    @Query("SELECT COUNT(u) FROM User u WHERE u.store.id = :storeId")
    long countUsersByStore(Long storeId);
}