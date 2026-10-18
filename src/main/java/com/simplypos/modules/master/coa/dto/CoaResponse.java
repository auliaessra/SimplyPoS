package com.simplypos.modules.master.coa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoaResponse {
    private Long id;
    private String accountCode;
    private String accountName;
    private String accountType;
    private Long parentId;
    private String parentName;
    private Boolean isActive;
}