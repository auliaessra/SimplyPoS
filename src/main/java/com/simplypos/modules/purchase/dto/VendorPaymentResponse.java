package com.simplypos.modules.purchase.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class VendorPaymentResponse {
    private Long id;
    private String paymentNo;
    private Long storeId;
    private String storeName;
    private Long vendorId;
    private String vendorName;
    private Long purchaseInvoiceId;
    private String purchaseInvoiceNo;
    private LocalDate paymentDate;
    private BigDecimal amount;
    private String paymentMethod;
    private String bankAccount;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
}