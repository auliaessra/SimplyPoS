package com.simplypos.modules.master.storeprice.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class StorePriceRequest {
    @NotNull
    private Long itemId;

    private Long storeId; // optional: kalau null, ambil dari StoreContext

    @NotNull
    @PositiveOrZero
    private BigDecimal buyPrice;

    @NotNull
    @PositiveOrZero
    private BigDecimal sellPrice;

    @PositiveOrZero
    private BigDecimal minStockMl;

    // stockQtyMl tidak di-set manual di form ini, hanya via transaksi
}