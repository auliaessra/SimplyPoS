package com.simplypos.modules.inventory.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.simplypos.modules.inventory.entity.ItemJournal;

public interface ItemJournalRepository extends JpaRepository<ItemJournal, Long> {
    Page<ItemJournal> findByStoreId(Long storeId, Pageable pageable);

    Page<ItemJournal> findAllByOrderByIdDesc(Pageable pageable);

    Optional<ItemJournal> findByDocumentNo(String documentNo);
}