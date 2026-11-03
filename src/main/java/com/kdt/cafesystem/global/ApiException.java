package com.kdt.cafesystem.global;

public class ApiException extends RuntimeException {
    private final ErrorCode code;
    public ApiException(ErrorCode code) { super(code.name()); this.code = code; }
    public ErrorCode getCode() { return code; }
}
