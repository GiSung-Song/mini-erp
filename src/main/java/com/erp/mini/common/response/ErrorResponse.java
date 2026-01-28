package com.erp.mini.common.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ErrorResponse", description = "공통 오류 응답 포맷 (success=false")
public record ErrorResponse(
        @Schema(description = "성공 여부", example = "false")
        boolean success,

        @Schema(description = "응답 데이터(오류 시 보통 null)", nullable = true)
        Object data,

        @Schema(description = "오류 정보")
        ApiError error
) {
}