package com.simplypos.modules.sales.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CustomerPaymentResponse {
    private Long id;
    private String paymentNo;
    private Long storeId;
    private String storeName;
    private Long customerId;
    private String customerName;
    private Long salesOrderId;
    private String salesOrderNo;
    private LocalDate paymentDate;
    private BigDecimal amount;
    private String paymentMethod;
    private String bankAccount;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
}