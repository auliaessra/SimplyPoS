package com.simplypos.modules.purchase.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.simplypos.config.StoreContext;
import com.simplypos.core.security.SecurityUtils;
import com.simplypos.entities.Store;
import com.simplypos.modules.auth.repository.StoreRepository;
import com.simplypos.modules.finance.service.CoaCode;
import com.simplypos.modules.finance.service.GeneralLedgerService;
import com.simplypos.modules.inventory.service.InventoryLedgerService;
import com.simplypos.modules.inventory.service.VendorLedgerService;
import com.simplypos.modules.master.common.SequenceService;
import com.simplypos.modules.master.common.exception.BusinessException;
import com.simplypos.modules.master.item.entity.Item;
import com.simplypos.modules.master.item.repository.ItemRepository;
import com.simplypos.modules.master.vendor.entity.Vendor;
import com.simplypos.modules.master.vendor.repository.VendorRepository;
import com.simplypos.modules.purchase.dto.PurchaseInvoiceRequest;
import com.simplypos.modules.purchase.dto.PurchaseInvoiceResponse;
import com.simplypos.modules.purchase.entity.PurchaseInvoice;
import com.simplypos.modules.purchase.entity.PurchaseInvoiceItem;
import com.simplypos.modules.purchase.repository.PurchaseInvoiceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurchaseInvoiceService {

    private final PurchaseInvoiceRepository repo;
    private final VendorRepository vendorRepo;
    private final ItemRepository itemRepo;
    private final StoreRepository storeRepo;
    private final StoreContext storeContext;
    private final SequenceService sequenceService;
    private final InventoryLedgerService inventoryLedgerService;
    private final VendorLedgerService vendorLedgerService;
    private final GeneralLedgerService glService;

    public Page<PurchaseInvoice> search(Long storeId, Pageable pageable) {
        if (storeId == null && SecurityUtils.isSuperAdmin()) {
            return repo.findAllByOrderByIdDesc(pageable);
        }
        if (storeId == null)
            storeId = storeContext.getCurrentStoreId();
        if (storeId == null)
            throw new BusinessException("Store context tidak tersedia");
        return repo.findByStoreId(storeId, pageable);
    }

    public PurchaseInvoice findById(Long id) {
        return repo.findById(id).orElseThrow(() -> new BusinessException("Purchase Invoice tidak ditemukan"));
    }

    @Transactional
    public PurchaseInvoice create(PurchaseInvoiceRequest request) {
        Long storeId = resolveStoreId(request.getStoreId());
        Store store = storeRepo.findById(storeId)
                .orElseThrow(() -> new BusinessException("Store tidak ditemukan"));
        Vendor vendor = vendorRepo.findById(request.getVendorId())
                .orElseThrow(() -> new BusinessException("Vendor tidak ditemukan"));

        if (!vendor.getStore().getId().equals(storeId)) {
            throw new BusinessException("Vendor tidak berada di cabang yang dipilih");
        }

        String invoiceNo = sequenceService.nextNumber("PI");

        PurchaseInvoice pi = PurchaseInvoice.builder()
                .store(store)
                .vendor(vendor)
                .invoiceNo(invoiceNo)
                .vendorInvoiceNo(request.getVendorInvoiceNo())
                .invoiceDate(request.getInvoiceDate())
                .dueDate(request.getDueDate())
                .status("DRAFT")
                .notes(request.getNotes())
                .taxAmount(request.getTaxAmount() == null ? BigDecimal.ZERO : request.getTaxAmount())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        for (var line : request.getItems()) {
            Item item = itemRepo.findById(line.getItemId())
                    .orElseThrow(() -> new BusinessException("Item tidak ditemukan: " + line.getItemId()));
            BigDecimal totalCost = line.getQtyMl().multiply(line.getUnitCost());

            PurchaseInvoiceItem pii = PurchaseInvoiceItem.builder()
                    .item(item)
                    .qtyMl(line.getQtyMl())
                    .unitCost(line.getUnitCost())
                    .totalCost(totalCost)
                    .build();
            pi.addItem(pii);
            subtotal = subtotal.add(totalCost);
        }
        pi.setSubtotal(subtotal);
        pi.setTotalAmount(subtotal.add(pi.getTaxAmount()));

        return repo.save(pi);
    }

    /**
     * POSTING Purchase Invoice:
     * - Tambah stok via item_ledgers
     * - Tambah AP via vendor_ledgers
     * - Auto-posting GL:
     * Debit : Persediaan (1301) = subtotal
     * Debit : PPN Masukan = tax (jika ada)
     * Credit : Hutang Usaha = total
     */
    @Transactional
    public PurchaseInvoice post(Long id) {
        PurchaseInvoice pi = findById(id);

        if (!"DRAFT".equals(pi.getStatus())) {
            throw new BusinessException("Hanya PI berstatus DRAFT yang dapat diposting");
        }
        if (pi.getItems().isEmpty()) {
            throw new BusinessException("PI tidak memiliki item");
        }

        Store store = pi.getStore();
        Vendor vendor = pi.getVendor();

        // 1. Item Ledger – tambah stok
        for (PurchaseInvoiceItem item : pi.getItems()) {
            inventoryLedgerService.postMovement(
                    store, item.getItem(),
                    "PURCHASE", pi.getInvoiceNo(), pi.getId(),
                    pi.getInvoiceDate(),
                    item.getQtyMl(), item.getUnitCost(),
                    "Pembelian dari vendor " + vendor.getName());
        }

        // 2. Vendor Ledger – tambah AP (positif)
        vendorLedgerService.post(
                store, vendor,
                "INVOICE", pi.getInvoiceNo(), pi.getId(),
                pi.getInvoiceDate(),
                pi.getTotalAmount(),
                "PI dari vendor " + vendor.getName());

        // 3. General Ledger – auto-posting
        List<GeneralLedgerService.GlLine> debits = new java.util.ArrayList<>();
        debits.add(new GeneralLedgerService.GlLine(
                CoaCode.PERSEDIAAN, pi.getSubtotal(), "Persediaan barang dagang"));

        if (pi.getTaxAmount().compareTo(BigDecimal.ZERO) > 0) {
            // PPN Masukan (anggap menggunakan kode 1302, fallback ke 1301)
            debits.add(new GeneralLedgerService.GlLine(
                    "1302", pi.getTaxAmount(), "PPN Masukan"));
        }

        List<GeneralLedgerService.GlLine> credits = List.of(
                new GeneralLedgerService.GlLine(
                        CoaCode.HUTANG_USAHA, pi.getTotalAmount(), "Hutang usaha ke " + vendor.getName()));

        glService.postDoubleEntry(
                store, "PURCHASE_INVOICE", pi.getInvoiceNo(), pi.getId(),
                pi.getInvoiceDate(), debits, credits,
                "Posting PI " + pi.getInvoiceNo());

        // 4. Update status
        pi.setStatus("POSTED");
        pi.setPostedAt(LocalDateTime.now());
        return repo.save(pi);
    }

    @Transactional
    public void delete(Long id) {
        PurchaseInvoice pi = findById(id);
        if (!"DRAFT".equals(pi.getStatus())) {
            throw new BusinessException("Hanya PI berstatus DRAFT yang dapat dihapus");
        }
        repo.delete(pi);
    }

    public PurchaseInvoiceResponse toResponse(PurchaseInvoice pi) {
        PurchaseInvoiceResponse res = new PurchaseInvoiceResponse();
        res.setId(pi.getId());
        res.setInvoiceNo(pi.getInvoiceNo());
        res.setVendorInvoiceNo(pi.getVendorInvoiceNo());
        res.setStoreId(pi.getStore().getId());
        res.setStoreName(pi.getStore().getName());
        res.setVendorId(pi.getVendor().getId());
        res.setVendorName(pi.getVendor().getName());
        res.setInvoiceDate(pi.getInvoiceDate());
        res.setDueDate(pi.getDueDate());
        res.setStatus(pi.getStatus());
        res.setSubtotal(pi.getSubtotal());
        res.setTaxAmount(pi.getTaxAmount());
        res.setTotalAmount(pi.getTotalAmount());
        res.setPaidAmount(pi.getPaidAmount());
        res.setNotes(pi.getNotes());
        res.setPostedAt(pi.getPostedAt());
        res.setCreatedAt(pi.getCreatedAt());
        res.setItems(pi.getItems().stream().map(i -> {
            var d = new PurchaseInvoiceResponse.ItemLine();
            d.setItemId(i.getItem().getId());
            d.setItemCode(i.getItem().getCode());
            d.setItemName(i.getItem().getName());
            d.setQtyMl(i.getQtyMl());
            d.setUnitCost(i.getUnitCost());
            d.setTotalCost(i.getTotalCost());
            return d;
        }).toList());
        return res;
    }

    private Long resolveStoreId(Long override) {
        // 1. Override eksplisit (dari form/query param)
        if (override != null)
            return override;

        // 2. StoreContext (diisi filter dari header atau user.store_id)
        Long ctx = storeContext.getCurrentStoreId();
        if (ctx != null)
            return ctx;

        // 3. Jika masih kosong:
        // - SUPER_ADMIN → null (akses lintas cabang)
        // - Role lain → error (karena mereka harus terikat cabang)
        if (SecurityUtils.isSuperAdmin()) {
            return null;
        }
        throw new BusinessException("error.store.context");
    }

    /**
     * Cari Purchase Invoice yang masih outstanding untuk vendor tertentu.
     * Digunakan untuk dropdown di form Vendor Payment.
     */
    public List<PurchaseInvoiceResponse> findOpenByVendor(Long vendorId, Long storeId) {
        if (storeId == null && !SecurityUtils.isSuperAdmin()) {
            storeId = storeContext.getCurrentStoreId();
        }
        return repo.findOpenByVendor(vendorId, storeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }
}