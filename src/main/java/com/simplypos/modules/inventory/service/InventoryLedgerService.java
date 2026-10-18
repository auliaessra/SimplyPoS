package com.simplypos.modules.inventory.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.simplypos.entities.Store;
import com.simplypos.modules.inventory.entity.ItemLedger;
import com.simplypos.modules.inventory.repository.ItemLedgerRepository;
import com.simplypos.modules.master.item.entity.Item;
import com.simplypos.modules.master.storeprice.entity.ItemStorePrice;
import com.simplypos.modules.master.storeprice.repository.ItemStorePriceRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service untuk mencatat pergerakan stok (item_ledgers)
 * sekaligus update stok real-time di item_store_prices.
 */
@Service
@RequiredArgsConstructor
public class InventoryLedgerService {

    private final ItemLedgerRepository ledgerRepo;
    private final ItemStorePriceRepository priceRepo;

    @Transactional
    public ItemLedger postMovement(Store store, Item item, String documentType,
            String documentNo, Long documentId,
            LocalDate transactionDate,
            BigDecimal qtyMl, BigDecimal unitCost,
            String description) {

        BigDecimal totalCost = qtyMl.multiply(unitCost);

        // Update stok real-time di item_store_prices
        ItemStorePrice isp = priceRepo.findByItemIdAndStoreId(item.getId(), store.getId())
                .orElseGet(() -> ItemStorePrice.builder()
                        .item(item)
                        .store(store)
                        .buyPrice(unitCost)
                        .sellPrice(BigDecimal.ZERO)
                        .stockQtyMl(BigDecimal.ZERO)
                        .minStockMl(BigDecimal.ZERO)
                        .build());

        BigDecimal newStock = isp.getStockQtyMl().add(qtyMl);
        if (newStock.compareTo(BigDecimal.ZERO) < 0) {
            throw new com.simplypos.modules.master.common.exception.BusinessException(
                    "Stok tidak boleh negatif untuk item " + item.getCode());
        }
        isp.setStockQtyMl(newStock);
        priceRepo.save(isp);

        ItemLedger ledger = ItemLedger.builder()
                .store(store)
                .item(item)
                .documentType(documentType)
                .documentNo(documentNo)
                .documentId(documentId)
                .transactionDate(transactionDate)
                .qtyMl(qtyMl)
                .unitCost(unitCost)
                .totalCost(totalCost)
                .balanceQtyMl(newStock)
                .description(description)
                .build();

        return ledgerRepo.save(ledger);
    }
}