package com.kdt.cafesystem.menu.dto;

public record MenuResponseDto(
        Long id,
        Long categoryId,
        String name,
        int price,
        String description,
        boolean available
) {
}
