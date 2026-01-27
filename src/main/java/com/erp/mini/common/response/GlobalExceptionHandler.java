package com.erp.mini.common.response;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<CustomResponse<Void>> handleBusinessException(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        return ResponseEntity.status(errorCode.status)
                .body(CustomResponse.fail(ApiError.of(errorCode.name(), ex.getMessage())));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> details = new HashMap<>();
        Map<String, String> fields = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fields.put(fe.getField(), fe.getDefaultMessage());
        }
        details.put("fields", fields);

        ErrorCode code = ErrorCode.INVALID_REQUEST;
        return ResponseEntity.status(code.status)
                .body(CustomResponse.fail(ApiError.of(code.name(), "입력값 검증에 실패했습니다.", details)));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CustomResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        ErrorCode code = ErrorCode.INVALID_REQUEST;
        return ResponseEntity.status(code.status)
                .body(CustomResponse.fail(ApiError.of(code.name(), "QueryParam / PathVariable 오류")));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CustomResponse<Void>> handleNotReadable(HttpMessageNotReadableException ex) {
        ErrorCode code = ErrorCode.INVALID_REQUEST;
        return ResponseEntity.status(code.status)
                .body(CustomResponse.fail(ApiError.of(code.name(), "요청 본문을 해석할 수 없습니다.")));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<CustomResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        ErrorCode code = ErrorCode.CONFLICT;
        return ResponseEntity.status(code.status)
                .body(CustomResponse.fail(ApiError.of(code.name(), "DB 제약 조건을 위반했습니다.")));
    }

    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<CustomResponse<Void>> handleMissingCookie(MissingRequestCookieException ex) {
        ErrorCode code = ErrorCode.INVALID_REQUEST;
        return ResponseEntity.status(code.status)
                .body(CustomResponse.fail(ApiError.of(code.name(), "필수 쿠키가 누락되었습니다.")));
    }
}
