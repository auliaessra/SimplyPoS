package com.simplypos.modules.master.customer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomerRequest {
    @NotBlank
    private String code;
    @NotBlank
    private String name;
    private String phone;
    private String email;
    private String address;
    private Boolean isActive = true;
    private Long storeId;
}