package com.kdt.cafesystem.category.dto;

import jakarta.validation.constraints.NotNull;

public record CategoryRequestDto(
        @NotNull String name
) {
}
