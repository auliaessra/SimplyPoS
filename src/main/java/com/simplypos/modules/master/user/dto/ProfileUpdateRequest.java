package com.simplypos.modules.master.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProfileUpdateRequest {
    @NotBlank
    private String fullName;

    @Email
    private String email;

    private String phone;
}