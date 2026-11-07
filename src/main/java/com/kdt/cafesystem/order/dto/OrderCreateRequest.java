package com.kdt.cafesystem.order.dto;

import com.kdt.cafesystem.order.dto.validation.UniqueMenuIds;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * A11 주문 접수 요청.
 * - email, memo : 공백 제거 안 함(원본 그대로 검사·보존)
 * - address, postcode : 앞뒤 공백 제거 후 검사
 * - items : 서로 다른 메뉴 1~10개, 메뉴 ID 중복 불가 (fieldErrors.items)
 */
public record OrderCreateRequest(

        @NotNull(message = "이메일은 필수입니다.")
        @Size(min = 1, max = 100, message = "이메일은 1~100자입니다.")
        @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)+$",
                message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @NotBlank(message = "주소는 필수입니다.")
        @Size(max = 200, message = "주소는 1~200자입니다.")
        String address,

        @NotNull(message = "우편번호는 필수입니다.")
        @Pattern(regexp = "^\\d{5}$", message = "우편번호는 숫자 5자리입니다.")
        String postcode,

        @Size(max = 200, message = "메모는 200자 이하입니다.")
        String memo,

        @NotEmpty(message = "서로 다른 메뉴를 1~10개 선택해 주세요.")
        @Size(max = 10, message = "서로 다른 메뉴를 1~10개 선택해 주세요.")
        @UniqueMenuIds
        List<@Valid @NotNull OrderItemRequest> items
) {
    public OrderCreateRequest {
        address = address == null ? null : address.strip();
        postcode = postcode == null ? null : postcode.strip();
    }
}
