package com.kdt.cafesystem.menu.dto;

import jakarta.validation.constraints.*;

public record MenuRequestDto(
        @NotNull Long categoryId,
        @NotBlank @Size(max = 100, message = "메뉴명은 앞뒤 공백 제거 후 1~100자 이어야 합니다.") String name,
        @NotNull @Min(1) @Max(value = 1_000_000 , message = "가격은 정수 1~1,000,000원 이어야 합니다.") Integer price,
        @NotNull @Size(max = 300, message = "설명은 앞뒤 공백 제거 후 300자 이하입니다. ") String description
) {
}
