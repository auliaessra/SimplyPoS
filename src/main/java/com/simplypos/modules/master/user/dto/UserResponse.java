package com.simplypos.modules.master.user.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private Long storeId;
    private String storeName;
    private String status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}