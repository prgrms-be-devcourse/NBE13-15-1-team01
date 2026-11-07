package com.kdt.cafesystem.order.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.springframework.stereotype.Component;

/**
 * 서울 시각 14:00 마감 규칙. 배송 묶음(팀원 3)에서도 재사용 가능.
 */
@Component
public class ShippingPolicy {

    public static final LocalTime CUTOFF = LocalTime.of(14, 0);

    /** 13:59:59까지 접수 → 당일, 14:00:00부터 → 다음 날 */
    public LocalDate shippingDateOf(LocalDateTime createdAt) {
        return createdAt.toLocalTime().isBefore(CUTOFF)
                ? createdAt.toLocalDate()
                : createdAt.toLocalDate().plusDays(1);
    }

    /** 저장된 배송일의 14:00:00 전까지만 취소 가능 */
    public boolean isCancellable(LocalDate shippingDate, LocalDateTime now) {
        return now.isBefore(shippingDate.atTime(CUTOFF));
    }
}
