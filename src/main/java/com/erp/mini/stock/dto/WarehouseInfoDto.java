package com.erp.mini.stock.dto;

import com.erp.mini.warehouse.domain.WarehouseStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "창고 정보")
public record WarehouseInfoDto(

        @Schema(description = "창고 식별자 ID")
        Long warehouseId,

        @Schema(description = "창고명")
        String warehouseName,

        @Schema(description = "창고 코드")
        String warehouseCode,

        @Schema(description = "창고 위치")
        String location,

        @Schema(description = "창고 상태")
        WarehouseStatus warehouseStatus
) {
}
