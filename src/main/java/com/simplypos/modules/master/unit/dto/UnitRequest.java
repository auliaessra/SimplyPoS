package com.simplypos.modules.master.unit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UnitRequest {
    @NotBlank(message = "Kode unit wajib diisi")
    private String code;

    @NotBlank(message = "Nama unit wajib diisi")
    private String name;

    @Pattern(regexp = "VOLUME|WEIGHT|COUNT", message = "Kategori harus VOLUME, WEIGHT, atau COUNT")
    private String category = "VOLUME";

    private Boolean isBase = false;
    private Boolean isActive = true;
    private String description;
    private Integer sortOrder = 0;
}