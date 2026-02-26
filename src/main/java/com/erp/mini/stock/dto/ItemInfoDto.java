package com.erp.mini.stock.dto;

import com.erp.mini.item.domain.ItemStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 정보")
public record ItemInfoDto(

        @Schema(description = "상품 식별자 ID")
        Long itemId,

        @Schema(description = "상품명")
        String itemName,

        @Schema(description = "상품 코드")
        String itemCode,

        @Schema(description = "상품 상태")
        ItemStatus itemStatus
) {
}
