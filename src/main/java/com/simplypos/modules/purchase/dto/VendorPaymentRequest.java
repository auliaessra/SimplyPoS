package com.simplypos.modules.purchase.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class VendorPaymentRequest {
    private Long storeId;

    @NotNull(message = "Vendor wajib dipilih")
    private Long vendorId;

    @NotNull(message = "Purchase Invoice wajib dipilih")
    private Long purchaseInvoiceId;

    @NotNull(message = "Tanggal pembayaran wajib diisi")
    private LocalDate paymentDate;

    @NotNull(message = "Jumlah pembayaran wajib diisi")
    @Positive(message = "Jumlah pembayaran harus lebih besar dari 0")
    private BigDecimal amount;

    private String paymentMethod = "CASH";
    private String bankAccount;
    private String notes;
}