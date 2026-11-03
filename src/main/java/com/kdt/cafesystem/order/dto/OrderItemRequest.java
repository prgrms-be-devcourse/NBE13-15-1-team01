package com.kdt.cafesystem.order.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 주문 항목 요청. 수량 1~20 (schema.sql 기준).
 */
public record OrderItemRequest(

        @NotNull(message = "메뉴 ID는 필수입니다.")
        @Positive(message = "메뉴 ID는 양수입니다.")
        Long menuId,

        @NotNull(message = "수량은 필수입니다.")
        @Min(value = 1, message = "수량은 1~20입니다.")
        @Max(value = 20, message = "수량은 1~20입니다.")
        Integer quantity
) {
}
