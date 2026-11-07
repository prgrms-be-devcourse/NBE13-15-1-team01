package com.kdt.cafesystem.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequestDto(

        @NotBlank
        @Size(max = 30, message = "카테고리명은 앞뒤 공백 제거 후 1~30자 이어야 합니다.")
        String name
) {
}
