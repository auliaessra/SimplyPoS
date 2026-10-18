package com.simplypos.modules.master.coa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CoaRequest {
    @NotBlank(message = "Kode akun wajib diisi")
    private String accountCode;

    @NotBlank(message = "Nama akun wajib diisi")
    private String accountName;

    @NotBlank(message = "Tipe akun wajib diisi")
    @Pattern(regexp = "ASSET|LIABILITY|EQUITY|REVENUE|EXPENSE", message = "Tipe akun harus salah satu dari: ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE")
    private String accountType;

    private Long parentId;
    private Boolean isActive = true;
}