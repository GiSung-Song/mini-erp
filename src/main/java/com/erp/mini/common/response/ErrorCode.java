package com.erp.mini.common.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "올바르지 않은 입력값입니다."),
    CONFLICT(HttpStatus.CONFLICT, "DB 무결정 제약 조건을 위반했습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "비로그인 상태입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "해당 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "해당 리소스를 찾을 수 없습니다.")
    ;

    public final HttpStatus status;
    public final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
