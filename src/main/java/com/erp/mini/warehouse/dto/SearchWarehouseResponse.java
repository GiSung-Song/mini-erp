package com.erp.mini.warehouse.dto;

import com.erp.mini.warehouse.domain.WarehouseStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "창고 검색 응답")
public record SearchWarehouseResponse(
        @Schema(description = "창고 식별자 ID", example = "1")
        Long id,

        @Schema(description = "창고명", example = "서울 1창고")
        String name,

        @Schema(description = "창고 코드", example = "WH000001")
        String code,

        @Schema(description = "창고 위치", example = "서울시 정읍구 강원동")
        String location,

        @Schema(description = "창고 상태", example = "ACTIVE")
        WarehouseStatus status
) {
}
