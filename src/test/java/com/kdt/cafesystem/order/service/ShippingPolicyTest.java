package com.kdt.cafesystem.order.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 시스템 시계를 바꾸지 않고 Clock.fixed로 14:00 경계를 검증한다.
 */
class ShippingPolicyTest {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private final ShippingPolicy policy = new ShippingPolicy();

    private LocalDateTime seoul(int y, int m, int d, int hh, int mm, int ss) {
        Clock fixed = Clock.fixed(ZonedDateTime.of(y, m, d, hh, mm, ss, 0, SEOUL).toInstant(), SEOUL);
        return LocalDateTime.now(fixed);
    }

    @Test
    @DisplayName("13:59:59 접수는 당일 배송, 14:00:00 접수는 다음 날 배송")
    void shippingDateBoundary() {
        assertThat(policy.shippingDateOf(seoul(2026, 11, 5, 13, 59, 59)))
                .isEqualTo(LocalDate.of(2026, 11, 5));
        assertThat(policy.shippingDateOf(seoul(2026, 11, 5, 14, 0, 0)))
                .isEqualTo(LocalDate.of(2026, 11, 6));
    }

    @Test
    @DisplayName("배송일 13:59:59까지는 취소 가능, 14:00:00부터는 취소 불가")
    void cancellableBoundary() {
        LocalDate shippingDate = LocalDate.of(2026, 11, 5);
        assertThat(policy.isCancellable(shippingDate, seoul(2026, 11, 5, 13, 59, 59))).isTrue();
        assertThat(policy.isCancellable(shippingDate, seoul(2026, 11, 5, 14, 0, 0))).isFalse();
    }
}
