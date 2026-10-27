package com.simplypos.modules.master.item.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.simplypos.modules.master.item.entity.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Optional<Item> findByCode(String code);

    boolean existsByCode(String code);

    @Query("""
                SELECT i FROM Item i
                WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(i.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<Item> search(@Param("keyword") String keyword, Pageable pageable);

    Page<Item> findByIsActiveTrue(Pageable pageable);

    Optional<Item> findByBarcode(String barcode);    // ← BARU
    boolean existsByBarcode(String barcode);         // ← BARU
}