package com.erp.mini.user.dto;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(

        @NotBlank(message = "이름은 필수입니다.")
        String name,

        @NotBlank(message = "사번은 필수입니다.")
        String employeeNumber
) {
}