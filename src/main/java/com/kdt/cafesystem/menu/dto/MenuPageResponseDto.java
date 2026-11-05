package com.kdt.cafesystem.menu.dto;

import java.util.List;

public record MenuPageResponseDto(
        List<MenuResponseDto> content,
        int number,
        int size,
        long totalElements,
        int totalPages) {
}
