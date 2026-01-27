package com.erp.mini.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "사용자 등록 요청")
public record AddUserRequest(
        @Schema(description = "사용자 이름", example = "송기성")
        @NotBlank(message = "이름은 필수입니다.")
        String name,

        @Schema(description = "사용자 사번", example = "EMP001")
        @NotBlank(message = "사번은 필수입니다.")
        String employeeNumber,

        @Schema(description = "비밀번호", example = "password")
        @NotBlank(message = "비밀번호는 필수입니다.")
        String password
) {
}
