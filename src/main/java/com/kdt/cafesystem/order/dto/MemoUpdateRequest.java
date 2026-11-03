package com.kdt.cafesystem.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * A22 주문 메모 수정 요청. 공백·줄바꿈은 그대로 보존.
 */
public record MemoUpdateRequest(

        @NotNull(message = "메모는 필수입니다. 비우려면 빈 문자열을 보내세요.")
        @Size(max = 200, message = "메모는 200자 이하입니다.")
        String memo
) {
}
