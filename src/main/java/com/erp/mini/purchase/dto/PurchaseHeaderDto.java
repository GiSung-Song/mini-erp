package com.erp.mini.purchase.dto;

import com.erp.mini.purchase.domain.PurchaseStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "구매 상세 헤더")
public record PurchaseHeaderDto(
        @Schema(description = "구매 식별자 ID")
        Long id,

        @Schema(description = "거래처 코드")
        String partnerCode,

        @Schema(description = "거래처명")
        String partnerName,

        @Schema(description = "구매 상태")
        PurchaseStatus status,

        @Schema(description = "생성 날짜")
        LocalDateTime createdAt,

        @Schema(description = "마지막 변경 날짜")
        LocalDateTime updatedAt,

        @Schema(description = "생성자")
        String createdBy,

        @Schema(description = "수정자")
        String updatedBy
) {
}
