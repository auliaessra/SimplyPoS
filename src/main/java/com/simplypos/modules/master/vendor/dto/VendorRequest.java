package com.simplypos.modules.master.vendor.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VendorRequest {
    @NotBlank(message = "Kode vendor wajib diisi")
    private String code;

    @NotBlank(message = "Nama vendor wajib diisi")
    private String name;

    private String phone;
    private String email;
    private String address;
    private Boolean isActive = true;
    private Long storeId; // optional: override
}