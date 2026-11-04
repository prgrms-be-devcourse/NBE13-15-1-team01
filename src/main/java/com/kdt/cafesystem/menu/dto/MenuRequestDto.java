package com.kdt.cafesystem.menu.dto;

import jakarta.validation.constraints.*;

public record MenuRequestDto(
        @NotNull Long categoryId,
        @NotBlank @Size(max = 100) String name,
        @NotNull @Min(1)@Max(1000000) Integer price,
        @NotNull @Size(max = 300) String description
) {
}
