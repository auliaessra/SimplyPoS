package com.simplypos.modules.master.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordChangeRequest {
    @NotBlank(message = "Password lama wajib diisi")
    private String currentPassword;

    @NotBlank(message = "Password baru wajib diisi")
    @Size(min = 6, message = "Password minimal 6 karakter")
    private String newPassword;

    @NotBlank(message = "Konfirmasi password wajib diisi")
    private String confirmPassword;
}