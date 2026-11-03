package com.kdt.cafesystem.menu.dto;

public record MenuRequestDto(
        Long categoryId,
        String name,
        int price,
        String description
) {
}
