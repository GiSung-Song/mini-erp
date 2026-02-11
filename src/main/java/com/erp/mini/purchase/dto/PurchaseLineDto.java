package com.erp.mini.purchase.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "구매 상세 라인")
public record PurchaseLineDto(

        @Schema(description = "구매 라인 식별자 ID")
        Long id,

        @Schema(description = "상품 코드")
        String itemCode,

        @Schema(description = "상품명")
        String itemName,

        @Schema(description = "창고 코드")
        String warehouseCode,

        @Schema(description = "창고명")
        String warehouseName,

        @Schema(description = "창고 주소")
        String warehouseLocation,

        @Schema(description = "수량")
        long qty,

        @Schema(description = "구매 가격")
        BigDecimal unitCost,

        @Schema(description = "총 가격")
        BigDecimal totalCost
) {
}
