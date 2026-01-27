package com.erp.mini.common.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공통 응답 포맷")
public record CustomResponse<T>(

        @Schema(description = "성공 여부", example = "true")
        boolean success,

        @Schema(description = "응답 데이터 (실패 시 null)", nullable = true)
        T data,

        @Schema(description = "오류 응답 포맷 (성공 시 null)", nullable = true)
        ApiError error
) {
    public static <T> CustomResponse<T> ok(T data) {
        return new CustomResponse<T>(true, data, null);
    }

    public static CustomResponse<Void> ok() {
        return new CustomResponse<>(true, null, null);
    }

    public static CustomResponse<Void> fail(ApiError error) {
        return new CustomResponse<>(false, null, error);
    }
}
