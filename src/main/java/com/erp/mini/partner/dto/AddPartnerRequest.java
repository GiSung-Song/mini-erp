package com.erp.mini.partner.dto;

import com.erp.mini.partner.domain.PartnerType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "거래처 등록 요청")
public record AddPartnerRequest(
        @Schema(description = "거래처명", example = "기성 식품")
        @NotBlank(message = "거래처명은 필수입니다.")
        String name,

        @Schema(description = "거래처 타입", example = "SUPPLIER")
        @NotNull(message = "거래처 타입은 필수입니다.")
        PartnerType type,

        @Schema(description = "이메일", example = "test@email.com")
        String email,

        @Schema(description = "전화번호", example = "01012345678")
        String phone
) {
}
