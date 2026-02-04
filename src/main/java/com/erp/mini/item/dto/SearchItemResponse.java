package com.erp.mini.item.dto;

import com.erp.mini.item.domain.ItemStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 검색 응답")
public record SearchItemResponse(
        @Schema(description = "상품 식별자 ID", example = "1")
        Long id,

        @Schema(description = "상품명", example = "설탕600g")
        String name,

        @Schema(description = "상품 코드", example = "C10001")
        String code,

        @Schema(description = "상품 상태", example = "ACTIVE")
        ItemStatus status
) {
}