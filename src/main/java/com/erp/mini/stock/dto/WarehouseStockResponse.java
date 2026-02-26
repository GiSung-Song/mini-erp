package com.erp.mini.stock.dto;

import com.erp.mini.common.response.PageResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "창고별 재고 조회 응답")
public record WarehouseStockResponse(
        @Schema(description = "창고 정보")
        WarehouseInfoDto warehouseInfo,

        @Schema(description = "창고별 재고 현황")
        PageResponse<WarehouseStockInfoDto> warehouseStockInfos
) {
}
