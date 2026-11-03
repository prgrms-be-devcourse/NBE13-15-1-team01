package com.kdt.cafesystem.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.kdt.cafesystem.menu.entity.Menu;
import com.kdt.cafesystem.menu.repository.MenuRepo;
import com.kdt.cafesystem.order.dto.OrderCreateRequest;
import com.kdt.cafesystem.order.dto.OrderItemRequest;
import com.kdt.cafesystem.order.dto.OrderResponse;
import com.kdt.cafesystem.order.entity.Order;
import com.kdt.cafesystem.order.repository.OrderRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 주문 시각 처리 검증 (DB 없이 모의 Repository 사용).
 * - 응답 시각은 명세 형식 "yyyy-MM-ddTHH:mm:ss+09:00"
 * - 소수점 초는 버려서 MySQL 반올림으로 14:00:00이 되는 일을 막는다
 */
class OrderServiceTimeTest {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private OrderService serviceAt(ZonedDateTime seoulNow) {
        OrderRepository orderRepository = mock(OrderRepository.class);
        MenuRepo menuRepo = mock(MenuRepo.class);
        given(menuRepo.findById(1L)).willReturn(Optional.of(new Menu("Columbia Nariñó", 5000, "", null)));
        given(orderRepository.findFirstByEmailAndShippingDateAndStatus(any(), any(), any())).willReturn(Optional.empty());
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

        Clock fixed = Clock.fixed(seoulNow.toInstant(), SEOUL);
        return new OrderService(orderRepository, menuRepo, new ShippingPolicy(), fixed);
    }

    private OrderCreateRequest request() {
        return new OrderCreateRequest("buyer@example.com", "서울시 실습로 10", "01234", null,
                List.of(new OrderItemRequest(1L, 2)));
    }

    @Test
    @DisplayName("13:59:59.700 접수: 초 단위로 절사해 당일 배송, createdAt은 +09:00 형식, cancelledAt은 null")
    void createdAtTruncatedAndFormatted() {
        OrderService service = serviceAt(ZonedDateTime.of(2026, 11, 5, 13, 59, 59, 700_000_000, SEOUL));

        OrderResponse response = service.create(request());

        assertThat(response.createdAt()).isEqualTo("2026-11-05T13:59:59+09:00");
        assertThat(response.shippingDate()).isEqualTo(LocalDate.of(2026, 11, 5));
        assertThat(response.cancelledAt()).isNull();
        assertThat(response.memo()).isEmpty();
    }

    @Test
    @DisplayName("14:00:00 정각 접수: 다음 날 배송, createdAt은 소수점 없이 +09:00 형식")
    void createdAtAtCutoff() {
        OrderService service = serviceAt(ZonedDateTime.of(2026, 11, 5, 14, 0, 0, 0, SEOUL));

        OrderResponse response = service.create(request());

        assertThat(response.createdAt()).isEqualTo("2026-11-05T14:00:00+09:00");
        assertThat(response.shippingDate()).isEqualTo(LocalDate.of(2026, 11, 6));
    }
}
