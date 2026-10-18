package com.simplypos.modules.purchase.service;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.simplypos.config.StoreContext;
import com.simplypos.core.security.SecurityUtils;
import com.simplypos.entities.Store;
import com.simplypos.modules.auth.repository.StoreRepository;
import com.simplypos.modules.master.common.SequenceService;
import com.simplypos.modules.master.common.exception.BusinessException;
import com.simplypos.modules.master.item.entity.Item;
import com.simplypos.modules.master.item.repository.ItemRepository;
import com.simplypos.modules.master.vendor.entity.Vendor;
import com.simplypos.modules.master.vendor.repository.VendorRepository;
import com.simplypos.modules.purchase.dto.PurchaseOrderRequest;
import com.simplypos.modules.purchase.dto.PurchaseOrderResponse;
import com.simplypos.modules.purchase.entity.PurchaseOrder;
import com.simplypos.modules.purchase.entity.PurchaseOrderItem;
import com.simplypos.modules.purchase.repository.PurchaseOrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository repo;
    private final VendorRepository vendorRepo;
    private final ItemRepository itemRepo;
    private final StoreRepository storeRepo;
    private final StoreContext storeContext;
    private final SequenceService sequenceService;

    public Page<PurchaseOrder> search(Long storeId, Pageable pageable) {
        if (storeId == null && SecurityUtils.isSuperAdmin()) {
            return repo.findAllByOrderByIdDesc(pageable);
        }
        if (storeId == null)
            storeId = storeContext.getCurrentStoreId();
        if (storeId == null)
            throw new BusinessException("Store context tidak tersedia");
        return repo.findByStoreId(storeId, pageable);
    }

    public PurchaseOrder findById(Long id) {
        return repo.findById(id).orElseThrow(() -> new BusinessException("Purchase Order tidak ditemukan"));
    }

    @Transactional
    public PurchaseOrder create(PurchaseOrderRequest request) {
        Long storeId = resolveStoreId(request.getStoreId());
        Store store = storeRepo.findById(storeId)
                .orElseThrow(() -> new BusinessException("Store tidak ditemukan"));
        Vendor vendor = vendorRepo.findById(request.getVendorId())
                .orElseThrow(() -> new BusinessException("Vendor tidak ditemukan"));

        if (!vendor.getStore().getId().equals(storeId)) {
            throw new BusinessException("Vendor tidak berada di cabang yang dipilih");
        }

        PurchaseOrder po = PurchaseOrder.builder()
                .store(store)
                .vendor(vendor)
                .poNumber(sequenceService.nextNumber("PO"))
                .orderDate(request.getOrderDate())
                .expectedDate(request.getExpectedDate())
                .status("DRAFT")
                .notes(request.getNotes())
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (var line : request.getItems()) {
            Item item = itemRepo.findById(line.getItemId())
                    .orElseThrow(() -> new BusinessException("Item tidak ditemukan: " + line.getItemId()));
            BigDecimal totalCost = line.getQtyMl().multiply(line.getUnitCost());

            PurchaseOrderItem poi = PurchaseOrderItem.builder()
                    .item(item)
                    .qtyMl(line.getQtyMl())
                    .unitCost(line.getUnitCost())
                    .totalCost(totalCost)
                    .build();
            po.addItem(poi);
            total = total.add(totalCost);
        }
        po.setTotalAmount(total);

        return repo.save(po);
    }

    @Transactional
    public PurchaseOrder confirm(Long id) {
        PurchaseOrder po = findById(id);
        if (!"DRAFT".equals(po.getStatus())) {
            throw new BusinessException("Hanya PO berstatus DRAFT yang dapat dikonfirmasi");
        }
        po.setStatus("CONFIRMED");
        return repo.save(po);
    }

    @Transactional
    public PurchaseOrder cancel(Long id) {
        PurchaseOrder po = findById(id);
        if ("RECEIVED".equals(po.getStatus())) {
            throw new BusinessException("PO yang sudah diterima tidak dapat dibatalkan");
        }
        po.setStatus("CANCELLED");
        return repo.save(po);
    }

    @Transactional
    public void delete(Long id) {
        PurchaseOrder po = findById(id);
        if (!"DRAFT".equals(po.getStatus())) {
            throw new BusinessException("Hanya PO berstatus DRAFT yang dapat dihapus");
        }
        repo.delete(po);
    }

    public PurchaseOrderResponse toResponse(PurchaseOrder po) {
        PurchaseOrderResponse res = new PurchaseOrderResponse();
        res.setId(po.getId());
        res.setPoNumber(po.getPoNumber());
        res.setStoreId(po.getStore().getId());
        res.setStoreName(po.getStore().getName());
        res.setVendorId(po.getVendor().getId());
        res.setVendorName(po.getVendor().getName());
        res.setOrderDate(po.getOrderDate());
        res.setExpectedDate(po.getExpectedDate());
        res.setStatus(po.getStatus());
        res.setTotalAmount(po.getTotalAmount());
        res.setNotes(po.getNotes());
        res.setCreatedAt(po.getCreatedAt());
        res.setItems(po.getItems().stream().map(i -> {
            var d = new PurchaseOrderResponse.ItemLine();
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
}