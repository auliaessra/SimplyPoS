package com.simplypos.modules.sales.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CustomerPaymentRequest {
    private Long storeId;

    @NotNull(message = "Sales Order wajib dipilih")
    private Long salesOrderId;

    @NotNull(message = "Tanggal pembayaran wajib diisi")
    private LocalDate paymentDate;

    @NotNull(message = "Jumlah wajib diisi")
    @Positive(message = "Jumlah harus lebih besar dari 0")
    private BigDecimal amount;

    private String paymentMethod = "CASH";
    private String bankAccount;
    private String notes;
}