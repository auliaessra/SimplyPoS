package com.simplypos.modules.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private String type = "Bearer";
    private String username;
    private String fullName;
    private String role;
    private Long storeId;
    private String storeCode;
    private String storeName;

    public LoginResponse(String token, String type, String username, String fullName, String role, Long storeId,
            String storeCode, String storeName) {
        this.token = token;
        this.type = type;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.storeId = storeId;
        this.storeCode = storeCode;
        this.storeName = storeName;
    }

}