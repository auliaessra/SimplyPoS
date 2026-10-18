package com.simplypos.modules.inventory.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.simplypos.modules.inventory.entity.ItemLedger;

public interface ItemLedgerRepository extends JpaRepository<ItemLedger, Long> {

    List<ItemLedger> findByStoreIdAndItemIdOrderByIdDesc(Long storeId, Long itemId);

    Page<ItemLedger> findByStoreIdOrderByIdDesc(Long storeId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(il.qtyMl),0) FROM ItemLedger il " +
            "WHERE il.store.id = :storeId AND il.item.id = :itemId")
    java.math.BigDecimal sumQtyByStoreAndItem(@Param("storeId") Long storeId,
            @Param("itemId") Long itemId);

    // Query untuk filter riwayat mutasi
    @Query("SELECT il FROM ItemLedger il WHERE " +
            "(:storeId IS NULL OR il.store.id = :storeId) AND " +
            "(:itemId IS NULL OR il.item.id = :itemId) AND " +
            "il.transactionDate BETWEEN :from AND :to " +
            "ORDER BY il.id DESC")
    Page<ItemLedger> search(@Param("storeId") Long storeId,
            @Param("itemId") Long itemId,
            @Param("from") java.time.LocalDate from,
            @Param("to") java.time.LocalDate to,
            Pageable pageable);
}