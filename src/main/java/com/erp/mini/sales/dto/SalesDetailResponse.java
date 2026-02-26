package com.erp.mini.sales.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "판매 상세 조회 응답")
public record SalesDetailResponse(

        @Schema(description = "판매 상세 헤더")
        SalesHeaderDto header,

        @Schema(description = "판매 상세 라인")
        List<SalesLineDto> lines
) {
}