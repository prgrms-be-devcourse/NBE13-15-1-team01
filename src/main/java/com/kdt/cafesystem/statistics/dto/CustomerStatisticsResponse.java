package com.kdt.cafesystem.statistics.dto;

import java.time.LocalDate;

// A24 접수일별 고객 통계. A17 주문 통계와 같은 숫자를 이메일별로 계산한 결과.
public record CustomerStatisticsResponse(
        LocalDate date,
        String email,
        long orderCount,
        long placedOrderCount,
        long cancelledOrderCount,
        long totalQuantity,
        long totalAmount
) {
}