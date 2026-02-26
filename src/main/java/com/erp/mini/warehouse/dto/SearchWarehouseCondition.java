package com.erp.mini.warehouse.dto;

import com.erp.mini.warehouse.domain.WarehouseStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "창고 검색")
public record SearchWarehouseCondition(

        @Schema(description = "키워드", example = "서울")
        String keyword,

        @Schema(description = "창고 상태", example = "INACTIVE")
        WarehouseStatus status
) {
}