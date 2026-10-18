package com.simplypos.modules.master.vendor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorResponse {
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