package com.erp.mini.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdatePasswordRequest(
        @NotBlank(message = "현재 비밀번호는 필수입니다.")
        String currentPassword,

        @NotBlank(message = "새로운 비밀번호는 필수입니다.")
        String newPassword
) {
}