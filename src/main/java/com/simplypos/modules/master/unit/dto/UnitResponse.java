package com.simplypos.modules.master.unit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnitResponse {
    private Long id;
    private String code;
    private String name;
    private String category;
    private Boolean isBase;
    private Boolean isActive;
    private String description;
    private Integer sortOrder;
}