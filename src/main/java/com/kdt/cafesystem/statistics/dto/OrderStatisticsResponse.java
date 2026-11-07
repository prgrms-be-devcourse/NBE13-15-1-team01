package com.kdt.cafesystem.statistics.dto;

import java.time.LocalDate;

/* A17 접수일별 주문 통계, 건수는 취소 포함 전체이고, 수량과 금액은 PLACED 주문만 집계. */
public record OrderStatisticsResponse(
        LocalDate date,
        long orderCount,
        long placedOrderCount,
        long cancelledOrderCount,
        long totalQuantity,
        long totalAmount
) {
}
