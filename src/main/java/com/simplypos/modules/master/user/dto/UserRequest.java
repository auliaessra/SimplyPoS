package com.simplypos.modules.master.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String fullName;

    @Email
    private String email;

    private String phone;

    @NotBlank
    @Pattern(regexp = "SUPER_ADMIN|ADMIN|CASHIER|STAFF", message = "Role tidak valid")
    private String role;

    private Long storeId;

    private String status = "ACTIVE";

    // Password hanya wajib untuk create
    private String password;
}