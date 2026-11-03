package com.kdt.cafesystem.global;

public record ErrorResponse(String code, String message) {
    public static ErrorResponse of(ErrorCode code) {
        return new ErrorResponse(code.name(), code.name());
    }
}
