package com.simplypos.modules.master.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {
    private Long id;
    private Long storeId;
    private String storeName;
    private String code;
    private String name;
    private String phone;
    private String email;
    private String address;
    private Boolean isActive;
}