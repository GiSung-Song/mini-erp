package com.erp.mini.item.dto;

import com.erp.mini.item.domain.ItemStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "상품 상세 조회 응답")
public record ItemDetailResponse(

        @Schema(description = "상품 식별자 ID", example = "1")
        Long id,

        @Schema(description = "상품명", example = "소금600g")
        String name,

        @Schema(description = "상품 코드", example = "C10001")
        String code,

        @Schema(description = "상품 상태", example = "ACTIVE")
        ItemStatus status,

        @Schema(description = "재고 목록")
        List<WarehouseStock> stocks
) {
    @Schema(description = "창고별 재고 응답")
    public record WarehouseStock(

            @Schema(description = "창고 식별자 ID", example = "1")
            Long warehouseId,

            @Schema(description = "창고 이름", example = "서울 창고")
            String warehouseName,

            @Schema(description = "재고 수량", example = "100")
            int quantity
    ) {}
}
