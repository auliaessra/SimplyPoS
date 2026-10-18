package com.simplypos.modules.inventory.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.simplypos.config.StoreContext;
import com.simplypos.core.security.SecurityUtils;
import com.simplypos.entities.Store;
import com.simplypos.modules.auth.repository.StoreRepository;
import com.simplypos.modules.finance.service.GeneralLedgerService;
import com.simplypos.modules.inventory.dto.ItemJournalRequest;
import com.simplypos.modules.inventory.dto.ItemJournalResponse;
import com.simplypos.modules.inventory.entity.ItemJournal;
import com.simplypos.modules.inventory.entity.ItemJournalItem;
import com.simplypos.modules.inventory.repository.ItemJournalRepository;
import com.simplypos.modules.master.common.SequenceService;
import com.simplypos.modules.master.common.exception.BusinessException;
import com.simplypos.modules.master.item.entity.Item;
import com.simplypos.modules.master.item.repository.ItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemJournalService {

    private final ItemJournalRepository repo;
    private final ItemRepository itemRepo;
    private final StoreRepository storeRepo;
    private final StoreContext storeContext;
    private final SequenceService sequenceService;
    private final InventoryLedgerService inventoryLedgerService;
    private final GeneralLedgerService glService;

    // Akun penyesuaian stok
    private static final String COA_PERSEDIAAN = "1301";
    private static final String COA_BEBAN_PENYESUAIAN = "5901";
    private static final String COA_PENDAPATAN_PENYESUAIAN = "5902";

    public Page<ItemJournal> search(Long storeId, Pageable pageable) {
        if (storeId == null && SecurityUtils.isSuperAdmin()) {
            return repo.findAllByOrderByIdDesc(pageable);
        }
        Long effectiveStore = resolveStoreId(storeId);
        if (effectiveStore == null) {
            return repo.findAllByOrderByIdDesc(pageable);
        }
        return repo.findByStoreId(effectiveStore, pageable);
    }

    public ItemJournal findById(Long id) {
        return repo.findById(id).orElseThrow(() -> new BusinessException("Item Journal tidak ditemukan"));
    }

    @Transactional
    public ItemJournal create(ItemJournalRequest request) {
        Long storeId = resolveStoreId(request.getStoreId());
        if (storeId == null) {
            throw new BusinessException("error.store.context");
        }
        Store store = storeRepo.findById(storeId)
                .orElseThrow(() -> new BusinessException("Store tidak ditemukan"));

        ItemJournal ij = ItemJournal.builder()
                .store(store)
                .documentNo(sequenceService.nextNumber("IJ"))
                .journalDate(request.getJournalDate())
                .status("DRAFT")
                .reason(request.getReason())
                .notes(request.getNotes())
                .createdBy(SecurityUtils.getCurrentUsername().orElse("system"))
                .build();

        for (var line : request.getItems()) {
            Item item = itemRepo.findById(line.getItemId())
                    .orElseThrow(() -> new BusinessException("Item tidak ditemukan"));

            BigDecimal unitCost = line.getUnitCost() != null
                    ? line.getUnitCost()
                    : getItemCost(storeId, item.getId());

            BigDecimal totalCost = line.getQtyMlAdjustment().multiply(unitCost);

            ItemJournalItem iji = ItemJournalItem.builder()
                    .item(item)
                    .qtyMlAdjustment(line.getQtyMlAdjustment())
                    .unitCost(unitCost)
                    .totalCost(totalCost)
                    .notes(line.getNotes())
                    .build();
            ij.addItem(iji);
        }

        return repo.save(ij);
    }

    /**
     * POSTING Item Journal:
     * - Update stok via item_ledgers
     * - Auto-posting GL:
     * Jika qty positif (stok bertambah):
     * Debit : Persediaan (1301)
     * Credit : Pendapatan Penyesuaian (5902)
     * Jika qty negatif (stok berkurang):
     * Debit : Beban Penyesuaian (5901)
     * Credit : Persediaan (1301)
     */
    @Transactional
    public ItemJournal post(Long id) {
        ItemJournal ij = findById(id);

        if (!"DRAFT".equals(ij.getStatus())) {
            throw new BusinessException("Hanya Item Journal berstatus DRAFT yang dapat diposting");
        }
        if (ij.getItems().isEmpty()) {
            throw new BusinessException("Item Journal tidak memiliki item");
        }

        Store store = ij.getStore();

        BigDecimal totalAdd = BigDecimal.ZERO; // stok bertambah
        BigDecimal totalReduce = BigDecimal.ZERO; // stok berkurang

        for (ItemJournalItem item : ij.getItems()) {
            // Update stok via item_ledgers
            inventoryLedgerService.postMovement(
                    store, item.getItem(),
                    "ADJUSTMENT", ij.getDocumentNo(), ij.getId(),
                    ij.getJournalDate(),
                    item.getQtyMlAdjustment(),
                    item.getUnitCost(),
                    "Penyesuaian stok (" + ij.getReason() + ")");

            if (item.getQtyMlAdjustment().compareTo(BigDecimal.ZERO) > 0) {
                totalAdd = totalAdd.add(item.getTotalCost());
            } else {
                totalReduce = totalReduce.add(item.getTotalCost().abs());
            }
        }

        // Auto-posting GL
        List<GeneralLedgerService.GlLine> debits = new ArrayList<>();
        List<GeneralLedgerService.GlLine> credits = new ArrayList<>();

        // Bagian stok bertambah
        if (totalAdd.compareTo(BigDecimal.ZERO) > 0) {
            debits.add(new GeneralLedgerService.GlLine(
                    COA_PERSEDIAAN, totalAdd, "Penyesuaian stok + " + ij.getDocumentNo()));
            credits.add(new GeneralLedgerService.GlLine(
                    COA_PENDAPATAN_PENYESUAIAN, totalAdd, "Pendapatan penyesuaian stok"));
        }

        // Bagian stok berkurang
        if (totalReduce.compareTo(BigDecimal.ZERO) > 0) {
            debits.add(new GeneralLedgerService.GlLine(
                    COA_BEBAN_PENYESUAIAN, totalReduce, "Beban penyesuaian stok"));
            credits.add(new GeneralLedgerService.GlLine(
                    COA_PERSEDIAAN, totalReduce, "Pengurangan stok " + ij.getDocumentNo()));
        }

        if (!debits.isEmpty() || !credits.isEmpty()) {
            glService.postDoubleEntry(
                    store, "ITEM_JOURNAL", ij.getDocumentNo(), ij.getId(),
                    ij.getJournalDate(), debits, credits,
                    "Posting penyesuaian stok " + ij.getDocumentNo());
        }

        ij.setStatus("POSTED");
        ij.setPostedAt(LocalDateTime.now());
        return repo.save(ij);
    }

    @Transactional
    public ItemJournal cancel(Long id) {
        ItemJournal ij = findById(id);
        if (!"DRAFT".equals(ij.getStatus())) {
            throw new BusinessException("Hanya Item Journal berstatus DRAFT yang dapat dibatalkan");
        }
        ij.setStatus("CANCELLED");
        return repo.save(ij);
    }

    @Transactional
    public void delete(Long id) {
        ItemJournal ij = findById(id);
        if (!"DRAFT".equals(ij.getStatus())) {
            throw new BusinessException("Hanya Item Journal berstatus DRAFT yang dapat dihapus");
        }
        repo.delete(ij);
    }

    public ItemJournalResponse toResponse(ItemJournal ij) {
        ItemJournalResponse res = new ItemJournalResponse();
        res.setId(ij.getId());
        res.setDocumentNo(ij.getDocumentNo());
        res.setStoreId(ij.getStore().getId());
        res.setStoreName(ij.getStore().getName());
        res.setJournalDate(ij.getJournalDate());
        res.setStatus(ij.getStatus());
        res.setReason(ij.getReason());
        res.setNotes(ij.getNotes());
        res.setPostedAt(ij.getPostedAt());
        res.setCreatedBy(ij.getCreatedBy());
        res.setCreatedAt(ij.getCreatedAt());
        res.setItems(ij.getItems().stream().map(i -> {
            var d = new ItemJournalResponse.ItemLine();
            d.setItemId(i.getItem().getId());
            d.setItemCode(i.getItem().getCode());
            d.setItemName(i.getItem().getName());
            d.setQtyMlAdjustment(i.getQtyMlAdjustment());
            d.setUnitCost(i.getUnitCost());
            d.setTotalCost(i.getTotalCost());
            d.setNotes(i.getNotes());
            return d;
        }).toList());
        return res;
    }

    private BigDecimal getItemCost(Long storeId, Long itemId) {
        // Bisa di-inject StorePriceService untuk ambil HPP, atau fallback ke 0
        return BigDecimal.ZERO;
    }

    private Long resolveStoreId(Long override) {
        if (override != null)
            return override;
        Long ctx = storeContext.getCurrentStoreId();
        if (ctx != null)
            return ctx;
        if (SecurityUtils.isSuperAdmin())
            return null;
        throw new BusinessException("error.store.context");
    }
}