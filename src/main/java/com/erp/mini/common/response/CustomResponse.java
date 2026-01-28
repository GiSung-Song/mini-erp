package com.erp.mini.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@Schema(description = "공통 응답 포맷")
public record CustomResponse<T>(

        @Schema(description = "성공 여부", example = "true")
        boolean success,

        @Schema(description = "응답 데이터 (실패 시 null)", nullable = true)
        T data,

        @Schema(description = "오류 응답 포맷 (성공 시 null)", nullable = true)
        ApiError error
) {
    public static <T> CustomResponse<T> okBody(T data) {
        return new CustomResponse<T>(true, data, null);
    }

    public static CustomResponse<Void> okBody() {
        return new CustomResponse<>(true, null, null);
    }

    public static <T> CustomResponse<T> failBody(ApiError error) {
        return new CustomResponse<>(false, null, error);
    }

    // 200 OK + data
    public static <T> ResponseEntity<CustomResponse<T>> ok(T data) {
        return ResponseEntity.ok(okBody(data));
    }

    // 200 OK
    public static <T> ResponseEntity<CustomResponse<Void>> ok() {
        return ResponseEntity.ok(okBody());
    }

    // 201 CREATED + data
    public static <T> ResponseEntity<CustomResponse<T>> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(okBody(data));
    }

    // 201 CREATED
    public static <T> ResponseEntity<CustomResponse<Void>> created() {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(okBody());
    }

    public static <T> ResponseEntity<CustomResponse<T>> fail(HttpStatus status, ApiError error) {
        return ResponseEntity.status(status).body(failBody(error));
    }

    public static <T> ResponseEntity<CustomResponse<T>> fail(ErrorCode code, String message) {
        return fail(code.status, ApiError.of(code.name(), message));
    }

    public static <T> ResponseEntity<CustomResponse<T>> fail(ErrorCode code) {
        return fail(code.status, ApiError.of(code.name(), code.message));
    }

    public static <T> ResponseEntity<CustomResponse<T>> fail(ErrorCode code, String message, Map<String, Object> details) {
        return fail(code.status, ApiError.of(code.name(), message, details));
    }
}
