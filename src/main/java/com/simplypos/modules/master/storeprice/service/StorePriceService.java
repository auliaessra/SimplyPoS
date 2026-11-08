package com.simplypos.modules.master.storeprice.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.simplypos.config.StoreContext;
import com.simplypos.core.security.SecurityUtils;
import com.simplypos.entities.Store;
import com.simplypos.modules.auth.repository.StoreRepository;
import com.simplypos.modules.master.common.exception.BusinessException;
import com.simplypos.modules.master.item.entity.Item;
import com.simplypos.modules.master.item.repository.ItemRepository;
import com.simplypos.modules.master.storeprice.dto.StorePriceRequest;
import com.simplypos.modules.master.storeprice.dto.StorePriceResponse;
import com.simplypos.modules.master.storeprice.entity.ItemStorePrice;
import com.simplypos.modules.master.storeprice.repository.ItemStorePriceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StorePriceService {

    private final ItemStorePriceRepository repo;
    private final ItemRepository itemRepository;
    private final StoreRepository storeRepository;
    private final StoreContext storeContext;

    public List<StorePriceResponse> findByStore(Long storeId) {
        if (storeId == null && SecurityUtils.isSuperAdmin()) {
            return repo.findAllByOrderByStoreIdAscItemIdAsc()
                    .stream().map(this::toResponse).toList();
        }
        if (storeId == null) {
            storeId = storeContext.getCurrentStoreId();
        }
        if (storeId == null) {
            throw new BusinessException("error.store.context");
        }
        return repo.findByStoreId(storeId).stream().map(this::toResponse).toList();
    }

    public StorePriceResponse findById(Long id) {
        return toResponse(repo.findById(id)
                .orElseThrow(() -> new BusinessException("Harga item tidak ditemukan")));
    }

    /**
     * Upsert: buat baru atau update jika kombinasi item+store sudah ada.
     * (Tidak dipakai jika pakai edit terpisah, tapi tetap dipertahankan).
     */
    @Transactional
    public StorePriceResponse upsert(StorePriceRequest request) {
        Long storeId = resolveStoreId(request.getStoreId());

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new BusinessException("Item tidak ditemukan"));
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException("Store tidak ditemukan"));

        ItemStorePrice entity = repo.findByItemIdAndStoreId(item.getId(), storeId)
                .orElseGet(() -> ItemStorePrice.builder()
                        .item(item)
                        .store(store)
                        .stockQtyMl(BigDecimal.ZERO)
                        .build());

        entity.setBuyPrice(request.getBuyPrice());
        entity.setSellPrice(request.getSellPrice());
        entity.setMinStockMl(request.getMinStockMl() == null ? BigDecimal.ZERO : request.getMinStockMl());

        return toResponse(repo.save(entity));
    }

    /**
     * Update harga beli / jual / min stock item yang sudah ada.
     * Tidak boleh ubah item & store (harus sama).
     */
    @Transactional
    public StorePriceResponse update(Long id, StorePriceRequest request) {
        ItemStorePrice entity = repo.findById(id)
                .orElseThrow(() -> new BusinessException("Harga item tidak ditemukan"));

        // Validasi harga tidak boleh negatif
        if (request.getBuyPrice() == null || request.getBuyPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Harga beli tidak boleh negatif");
        }
        if (request.getSellPrice() == null || request.getSellPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Harga jual tidak boleh negatif");
        }

        // Validasi harga jual tidak boleh lebih rendah dari harga beli (opsional)
        if (request.getSellPrice().compareTo(request.getBuyPrice()) < 0) {
            throw new BusinessException(
                    "Harga jual tidak boleh lebih rendah dari harga beli");
        }

        entity.setBuyPrice(request.getBuyPrice());
        entity.setSellPrice(request.getSellPrice());
        entity.setMinStockMl(request.getMinStockMl() == null
                ? BigDecimal.ZERO
                : request.getMinStockMl());

        return toResponse(repo.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        ItemStorePrice entity = repo.findById(id)
                .orElseThrow(() -> new BusinessException("Harga item tidak ditemukan"));
        repo.delete(entity);
    }

    private Long resolveStoreId(Long override) {
        if (override != null)
            return override;
        Long ctx = storeContext.getCurrentStoreId();
        if (ctx != null)
            return ctx;
        if (SecurityUtils.isSuperAdmin()) {
            throw new BusinessException("SUPER_ADMIN wajib memilih storeId");
        }
        throw new BusinessException("error.store.context");
    }

    private StorePriceResponse toResponse(ItemStorePrice e) {
        // Normalize null values → ZERO
        BigDecimal buyPrice = e.getBuyPrice() != null ? e.getBuyPrice() : BigDecimal.ZERO;
        BigDecimal sellPrice = e.getSellPrice() != null ? e.getSellPrice() : BigDecimal.ZERO;
        BigDecimal stockQty = e.getStockQtyMl() != null ? e.getStockQtyMl() : BigDecimal.ZERO;
        BigDecimal minStock = e.getMinStockMl() != null ? e.getMinStockMl() : BigDecimal.ZERO;

        // Unit dari item
        String unitCode = e.getItem().getUnit() != null
                ? e.getItem().getUnit().getCode()
                : "ML";
        String unitName = e.getItem().getUnit() != null
                ? e.getItem().getUnit().getName()
                : "Mililiter";

        return new StorePriceResponse(
                e.getId(),
                e.getItem().getId(),
                e.getItem().getCode(),
                e.getItem().getName(),
                e.getStore().getId(),
                e.getStore().getName(),
                buyPrice,
                sellPrice,
                stockQty,
                minStock,
                unitCode,
                unitName,
                e.getItem().getBarcode());
    }
}