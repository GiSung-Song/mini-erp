package com.erp.mini.partner.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdatePartnerRequest(
        @Schema(description = "이메일", example = "test@email.com")
        String email,

        @Schema(description = "전화번호", example = "01012345678")
        String phone
) {
}
