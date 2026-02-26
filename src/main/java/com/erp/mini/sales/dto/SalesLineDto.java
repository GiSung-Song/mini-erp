package com.erp.mini.sales.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "판매 상세 라인")
public record SalesLineDto(

        @Schema(description = "판매 라인 식별자 ID")
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

        @Schema(description = "판매 가격")
        BigDecimal unitPrice,

        @Schema(description = "총 가격")
        BigDecimal totalCost
) {
}
