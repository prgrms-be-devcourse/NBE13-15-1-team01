package com.kdt.cafesystem.menu.dto;

import jakarta.validation.constraints.NotNull;

public record MenuAvailabilityRequestDto(
        @NotNull Boolean available
) {
}
