package com.simplypos.modules.master.storeprice.dto;

import java.math.BigDecimal;

import lombok.Data;

/**
 * DTO untuk Store Price.
 *
 * PENTING: Beberapa field BigDecimal menggunakan custom getter yang
 * mengembalikan ZERO jika nilainya null, untuk menghindari NPE
 * saat template Thymeleaf melakukan compareTo() atau operasi aritmatika.
 */
@Data
public class StorePriceResponse {

    private Long id;
    private Long itemId;
    private String itemCode;
    private String itemName;
    private Long storeId;
    private String storeName;
    private BigDecimal buyPrice;
    private BigDecimal sellPrice;
    private BigDecimal stockQtyMl;
    private BigDecimal minStockMl;
    private String unitCode;
    private String unitName;

    public StorePriceResponse() {
    }

    public StorePriceResponse(Long id, Long itemId, String itemCode, String itemName,
            Long storeId, String storeName,
            BigDecimal buyPrice, BigDecimal sellPrice,
            BigDecimal stockQtyMl, BigDecimal minStockMl,
            String unitCode, String unitName) {
        this.id = id;
        this.itemId = itemId;
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.storeId = storeId;
        this.storeName = storeName;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.stockQtyMl = stockQtyMl;
        this.minStockMl = minStockMl;
        this.unitCode = unitCode;
        this.unitName = unitName;
    }

    // ==================== SAFE GETTERS ====================

    /** Selalu kembalikan nilai non-null */
    public BigDecimal getBuyPrice() {
        return buyPrice != null ? buyPrice : BigDecimal.ZERO;
    }

    public BigDecimal getSellPrice() {
        return sellPrice != null ? sellPrice : BigDecimal.ZERO;
    }

    public BigDecimal getStockQtyMl() {
        return stockQtyMl != null ? stockQtyMl : BigDecimal.ZERO;
    }

    public BigDecimal getMinStockMl() {
        return minStockMl != null ? minStockMl : BigDecimal.ZERO;
    }

    /** Kode unit dengan fallback "ML" */
    public String getUnitCode() {
        return (unitCode != null && !unitCode.isBlank()) ? unitCode : "ML";
    }

    /** Nama unit dengan fallback */
    public String getUnitName() {
        return (unitName != null && !unitName.isBlank()) ? unitName : "Mililiter";
    }

    // ==================== HELPER ====================

    /** Cek apakah stok di bawah minimal */
    public boolean isLowStock() {
        return getStockQtyMl().compareTo(getMinStockMl()) <= 0;
    }
}