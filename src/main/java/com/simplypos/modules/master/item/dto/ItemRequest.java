package com.simplypos.modules.master.item.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ItemRequest {
    @NotBlank(message = "Kode item wajib diisi")
    private String code;

    @NotBlank(message = "Nama item wajib diisi")
    private String name;

    /**
     * ID unit dari master units.
     * Jika null, akan default ke ML (base unit).
     */
    private Long unitId;

    private String description;
    private Boolean isActive = true;

    private String barcode;
}