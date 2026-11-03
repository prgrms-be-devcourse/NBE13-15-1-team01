package com.kdt.cafesystem.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record MenuRequestDto(
        @NotNull Long categoryId,
        @NotBlank @Size(max = 100) String name,
        @NotNull @PositiveOrZero Integer price,
        @NotNull @Size(max = 300) String description
) {
}
