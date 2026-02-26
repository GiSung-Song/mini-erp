package com.erp.mini.inventory.dto;

import com.erp.mini.inventory.domain.RefType;
import com.erp.mini.inventory.domain.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "상세 거래 이력 조회")
public record ItxDetailResponse(
        @Schema(description = "재고 이력 식별자 ID")
        Long itxId,

        @Schema(description = "상품 코드")
        String itemCode,

        @Schema(description = "상품명")
        String itemName,

        @Schema(description = "창고 코드")
        String warehouseCode,

        @Schema(description = "창고명")
        String warehouseName,

        @Schema(description = "거래 종류")
        TransactionType type,

        @Schema(description = "수량")
        long qtyDelta,

        @Schema(description = "거래 시각(이력 생성 시각)")
        LocalDateTime createdAt,

        @Schema(description = "거래자(이력 작성자)")
        String createdBy,

        @Schema(description = "관련 발주 식별자 ID")
        Long refId,

        @Schema(description = "거래 타입(입고/출고)")
        RefType refType,

        @Schema(description = "사유")
        String reason,

        @Schema(description = "파트너 코드")
        String partnerCode,

        @Schema(description = "파트너명")
        String partnerName
) {
}
