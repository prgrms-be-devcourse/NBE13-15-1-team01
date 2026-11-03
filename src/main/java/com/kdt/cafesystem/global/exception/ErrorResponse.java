package com.kdt.cafesystem.global.exception;

import java.util.Map;

/**
 * 공통 오류 응답 형식. 필드 이름은 명세서에 맞게 팀원 1과 확정할 것.
 * 예) {"code":"VALIDATION_ERROR","message":"...","fieldErrors":{"email":"..."}}
 */
public record ErrorResponse(String code, String message, Map<String, String> fieldErrors) {

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.name(), errorCode.getMessage(), Map.of());
    }

    public static ErrorResponse of(ErrorCode errorCode, Map<String, String> fieldErrors) {
        return new ErrorResponse(errorCode.name(), errorCode.getMessage(), fieldErrors);
    }
}
