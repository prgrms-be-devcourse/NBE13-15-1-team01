package com.kdt.cafesystem.global.exception;

import lombok.Getter;

/**
 * 404·409 같은 업무 규칙 위반을 Service에서 던질 때 사용.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
