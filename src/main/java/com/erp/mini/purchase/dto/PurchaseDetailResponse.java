package com.erp.mini.purchase.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "구매 상세 조회 응답")
public record PurchaseDetailResponse(

        @Schema(description = "구매 상세 헤더")
        PurchaseHeaderDto header,

        @Schema(description = "구매 상세 라인")
        List<PurchaseLineDto> lines
) {
}