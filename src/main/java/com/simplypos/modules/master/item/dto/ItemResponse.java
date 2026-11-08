package com.simplypos.modules.master.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemResponse {
    private Long id;
    private String code;
    private String name;
    private Long unitId;
    private String unitCode;
    private String unitName;
    private String description;
    private Boolean isActive;

    private String barcode;
}