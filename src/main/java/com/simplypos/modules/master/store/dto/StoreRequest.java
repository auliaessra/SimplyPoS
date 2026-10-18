package com.simplypos.modules.master.store.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StoreRequest {
    @NotBlank(message = "Kode cabang wajib diisi")
    private String code;
    @NotBlank(message = "Nama cabang wajib diisi")
    private String name;
    private String address;
    private String phone;
    private String email;
    private String managerName;
    private Boolean isActive = true;
}