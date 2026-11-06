package com.kdt.cafesystem.order.service;

import com.kdt.cafesystem.global.exception.BusinessException;
import com.kdt.cafesystem.global.exception.ErrorCode;
import com.kdt.cafesystem.menu.entity.Menu;
import com.kdt.cafesystem.menu.repository.MenuRepo;
import com.kdt.cafesystem.order.dto.OrderItemResponse;
import com.kdt.cafesystem.order.dto.OrderResponse;
import com.kdt.cafesystem.order.entity.Order;
import com.kdt.cafesystem.order.entity.OrderItem;
import com.kdt.cafesystem.order.entity.OrderStatus;
import com.kdt.cafesystem.order.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.*;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;


class OrderServiceRuleTest {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final LocalDate SHIPPING = LocalDate.of(2026, 11, 5);

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final MenuRepo menuRepo = mock(MenuRepo.class);

    private OrderService serviceAt(int y, int m, int d, int hh, int mm, int ss) {
        Clock fixed = Clock.fixed(ZonedDateTime.of(y, m, d, hh, mm, ss, 0, SEOUL).toInstant(), SEOUL);
        return new OrderService(orderRepository, menuRepo, new ShippingPolicy(), fixed);
    }

    private Menu menu(long id, int price) {
        Menu menu = new Menu("Columbia", price, "", null);
        ReflectionTestUtils.setField(menu, "id", id);
        return menu;
    }

    /** 단가 5000 × 2개 = 10000, 배송일 11/5 인 주문(id=7) */
    private Order existingOrder() {
        Order order = Order.create("buyer@example.com", "서울시 실습로 10", "01234", "문 앞",
                LocalDateTime.of(2026, 11, 4, 10, 0, 0), SHIPPING);
        order.addItem(OrderItem.of(menu(1L, 5000), 2));
        ReflectionTestUtils.setField(order, "id", 7L);
        given(orderRepository.findById(7L)).willReturn(Optional.of(order));
        return order;
    }

    @Test
    @DisplayName("취소: 배송일 13:59:59는 성공")
    void cancelBeforeCutoff() {
        existingOrder();

        OrderResponse response = serviceAt(2026, 11, 5, 13, 59, 59).cancel(7L);

        assertThat(response.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(response.cancelledAt()).isEqualTo("2026-11-05T13:59:59+09:00");
    }

    @Test
    @DisplayName("취소: 배송일 14:00:00은 ORDER_NOT_CANCELLABLE, 주문은 접수 상태 유지")
    void cancelAtCutoff() {
        Order order = existingOrder();

        assertThatThrownBy(() -> serviceAt(2026, 11, 5, 14, 0, 0).cancel(7L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ORDER_NOT_CANCELLABLE);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PLACED);
        assertThat(order.getCancelledAt()).isNull();
    }

    @Test
    @DisplayName("재취소: 마감 이후에도 예외 없이 최초 취소 시각과 기존 값 유지")
    void cancelAgainAfterCutoff() {
        existingOrder();
        OrderResponse first = serviceAt(2026, 11, 5, 9, 0, 0).cancel(7L);

        OrderResponse second = serviceAt(2026, 11, 6, 15, 30, 0).cancel(7L);

        assertThat(second.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(second.cancelledAt()).isEqualTo("2026-11-05T09:00:00+09:00");
        assertThat(second).isEqualTo(first);
    }

    @Test
    @DisplayName("재주문: 현재 가격으로 새 주문 계산, 원본 주문의 단가·항목 금액·총액은 보존")
    void reorderUsesCurrentPriceAndKeepsOrigin() {
        Order origin = existingOrder();
        given(menuRepo.findById(1L)).willReturn(Optional.of(menu(1L, 6000)));   // 5000 → 6000 인상
        given(orderRepository.findFirstByEmailAndShippingDateAndStatus(any(), any(), any()))
                .willReturn(Optional.empty());
        given(orderRepository.save(any(Order.class))).willAnswer(i -> i.getArgument(0));

        OrderResponse reordered = serviceAt(2026, 11, 5, 15, 0, 0).reorder(7L);

        // 새 주문: 현재 가격, 14시 이후 접수라 다음 날 배송
        OrderItemResponse newItem = reordered.items().get(0);
        assertThat(newItem.unitPrice()).isEqualTo(6000);
        assertThat(newItem.lineAmount()).isEqualTo(12000);
        assertThat(reordered.totalAmount()).isEqualTo(12000);
        assertThat(reordered.shippingDate()).isEqualTo(LocalDate.of(2026, 11, 6));
        assertThat(reordered.status()).isEqualTo(OrderStatus.PLACED);

        // 원본: 접수 당시 값 그대로
        OrderItemResponse oldItem = OrderResponse.from(origin).items().get(0);
        assertThat(oldItem.unitPrice()).isEqualTo(5000);
        assertThat(oldItem.lineAmount()).isEqualTo(10000);
        assertThat(origin.getTotalAmount()).isEqualTo(10000);
    }
}
