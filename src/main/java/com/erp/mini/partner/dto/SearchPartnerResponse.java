package com.erp.mini.partner.dto;

import com.erp.mini.partner.domain.PartnerType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "거래처 검색 응답")
public record SearchPartnerResponse(
        @Schema(description = "거래처 식별자 ID", example = "1")
        Long id,

        @Schema(description = "거래처명", example = "기성식품")
        String name,

        @Schema(description = "거래처 코드", example = "SUP000001")
        String code,

        @Schema(description = "거래처 타입", example = "SUPPLIER")
        PartnerType type
) {
}