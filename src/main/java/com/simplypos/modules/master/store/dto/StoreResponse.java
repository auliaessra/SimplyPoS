package com.simplypos.modules.master.store.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreResponse {
    private Long id;
    private String code;
    private String name;
    private String address;
    private String phone;
    private String email;
    private String managerName;
    private Boolean isActive;
    private Long userCount;
    private LocalDateTime createdAt;
}