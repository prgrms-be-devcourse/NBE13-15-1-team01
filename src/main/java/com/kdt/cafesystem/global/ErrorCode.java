package com.kdt.cafesystem.global;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND),
    CATEGORY_NAME_DUPLICATED(HttpStatus.CONFLICT),
    CATEGORY_IN_USE(HttpStatus.CONFLICT);

    private final HttpStatus status;
    ErrorCode(HttpStatus status) { this.status = status; }
    public HttpStatus getStatus() { return status; }
}
