package com.erp.mini.stock.dto;

import com.erp.mini.common.response.PageResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품별 재고 조회 응답")
public record ItemStockResponse(
        @Schema(description = "상품 정보")
        ItemInfoDto itemInfo,

        @Schema(description = "상품별 재고 현황")
        PageResponse<ItemStockInfoDto> itemStockInfos
) {
}