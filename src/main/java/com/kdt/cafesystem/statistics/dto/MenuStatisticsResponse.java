package com.kdt.cafesystem.statistics.dto;

/* A18 접수일별 메뉴 통계, PLACED 주문 항목 중 메뉴 id, 주문 당시 메뉴명, 가격이 같은 것끼리 합산한 결과. */
public record MenuStatisticsResponse(
        Long menuId,
        String menuName,
        int unitPrice,
        long totalQuantity,
        long totalAmount
) {
}