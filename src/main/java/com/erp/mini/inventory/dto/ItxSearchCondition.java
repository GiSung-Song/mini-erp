package com.erp.mini.inventory.dto;

import com.erp.mini.inventory.domain.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "거래 이력 검색 조건")
public record ItxSearchCondition(
        @Schema(description = "상품 식별자 ID", example = "1")
        Long itemId,

        @Schema(description = "창고 식별자 ID", example = "2")
        Long warehouseId,

        @Schema(description = "검색 시작 날짜", example = "20221112")
        LocalDate startDate,

        @Schema(description = "검색 종료 날짜", example = "20221113")
        LocalDate endDate,

        @Schema(description = "거래 유형", example = "INBOUND")
        TransactionType type
) {
}
