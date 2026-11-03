package com.kdt.cafesystem.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 공통 오류 코드. 팀원 1과 공동 관리 — 카테고리·메뉴 코드는 팀원 1이 추가.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),

    // 404
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "메뉴를 찾을 수 없습니다."),

    // 409
    MENU_UNAVAILABLE(HttpStatus.CONFLICT, "판매 중지된 메뉴가 포함되어 있습니다."),
    ADDRESS_MISMATCH(HttpStatus.CONFLICT, "같은 배송일의 기존 주문과 배송지가 다릅니다."),
    ORDER_NOT_CANCELLABLE(HttpStatus.CONFLICT, "취소 가능 시간이 지났습니다.");

    private final HttpStatus status;
    private final String message;
}
