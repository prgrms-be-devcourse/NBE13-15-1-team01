package com.kdt.cafesystem.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequestDto(
        @NotBlank@Size(max=30) String name
) {
}
