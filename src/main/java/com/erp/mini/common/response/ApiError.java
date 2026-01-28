package com.erp.mini.common.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "공통 오류 응답 포맷")
public record ApiError(

        @Schema(description = "오류 코드", example = "USER_NOT_FOUND")
        String code,

        @Schema(description = "오류 메시지", example = "DB 제약 조건을 위반했습니다.")
        String message,

        @Schema(description = "Validation 오류")
        Map<String, Object> details
) {
    public static ApiError of(String code, String message) {
        return new ApiError(code, message, null);
    }

    public static ApiError of(String code, String message, Map<String, Object> details) {
        return new ApiError(code, message, details);
    }
}