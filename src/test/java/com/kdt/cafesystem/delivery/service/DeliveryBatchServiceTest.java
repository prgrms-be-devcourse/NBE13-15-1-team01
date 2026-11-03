package com.kdt.cafesystem.delivery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.kdt.cafesystem.delivery.dto.DeliveryBatchItemResponse;
import com.kdt.cafesystem.delivery.dto.DeliveryBatchResponse;
import com.kdt.cafesystem.menu.entity.Menu;
import com.kdt.cafesystem.order.entity.Order;
import com.kdt.cafesystem.order.entity.OrderItem;
import com.kdt.cafesystem.order.entity.OrderStatus;
import com.kdt.cafesystem.order.repository.OrderRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * DB 없이 묶기·합산·정렬 로직만 검증한다.
 * Repository는 가짜(mock)로 만들고, 돌려줄 주문은 자바 코드로 직접 만든다.
 */
class DeliveryBatchServiceTest {

    private static final LocalDate SHIPPING_DATE = LocalDate.of(2026, 11, 3);

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final DeliveryBatchService deliveryBatchService = new DeliveryBatchService(orderRepository);

    @Test
    @DisplayName("A14: 같은 이메일의 주문을 한 묶음으로 만들고, 같은 메뉴 항목의 수량을 합산한다")
    void mergeSameEmailOrders() {
        Menu columbia = menu(1L, "Columbia Nariñó", 5000);
        Menu brazil = menu(2L, "Brazil Serra Do Caparaó", 7000);
        Order order101 = order(101L, "buyer@example.com", OrderItem.of(columbia, 2), OrderItem.of(brazil, 1));
        Order order102 = order(102L, "buyer@example.com", OrderItem.of(columbia, 1));
        givenPlacedOrders(order101, order102);

        List<DeliveryBatchResponse> batches = deliveryBatchService.findBatches(SHIPPING_DATE);

        assertThat(batches).hasSize(1);
        DeliveryBatchResponse batch = batches.get(0);
        assertThat(batch.orderIds()).containsExactly(101L, 102L);
        assertThat(batch.orderCount()).isEqualTo(2);
        assertThat(batch.totalQuantity()).isEqualTo(4);
        assertThat(batch.totalAmount()).isEqualTo(22000);
        assertThat(batch.items())
                .extracting(DeliveryBatchItemResponse::menuId,
                        DeliveryBatchItemResponse::quantity,
                        DeliveryBatchItemResponse::lineAmount)
                .containsExactly(
                        tuple(1L, 3, 15000L),
                        tuple(2L, 1, 7000L));
    }

    @Test
    @DisplayName("A14: 대소문자만 다른 이메일은 다른 묶음이며, 묶음은 이메일 오름차순이다")
    void separateByEmailCaseSensitive() {
        Menu columbia = menu(1L, "Columbia Nariñó", 5000);
        givenPlacedOrders(
                order(101L, "buyer@example.com", OrderItem.of(columbia, 1)),
                order(102L, "Buyer@example.com", OrderItem.of(columbia, 1)));

        List<DeliveryBatchResponse> batches = deliveryBatchService.findBatches(SHIPPING_DATE);

        assertThat(batches)
                .extracting(DeliveryBatchResponse::email)
                .containsExactly("Buyer@example.com", "buyer@example.com");
    }

    @Test
    @DisplayName("A14: 같은 메뉴라도 주문 당시 단가가 다르면 합치지 않고 단가 오름차순으로 나눈다")
    void doNotMergeDifferentPrice() {
        Menu columbia = menu(1L, "Columbia Nariñó", 5000);
        OrderItem cheap = OrderItem.of(columbia, 1);   // 5,000원일 때 주문
        columbia.setPrice(6000);                        // 메뉴 가격 수정
        OrderItem expensive = OrderItem.of(columbia, 1); // 6,000원일 때 주문
        givenPlacedOrders(
                order(101L, "buyer@example.com", expensive),
                order(102L, "buyer@example.com", cheap));

        List<DeliveryBatchResponse> batches = deliveryBatchService.findBatches(SHIPPING_DATE);

        assertThat(batches.get(0).items())
                .extracting(DeliveryBatchItemResponse::unitPrice, DeliveryBatchItemResponse::quantity)
                .containsExactly(tuple(5000, 1), tuple(6000, 1));
    }

    @Test
    @DisplayName("A14: 그 배송일에 PLACED 주문이 없으면 빈 목록을 반환한다")
    void emptyWhenNoOrders() {
        givenPlacedOrders();

        assertThat(deliveryBatchService.findBatches(SHIPPING_DATE)).isEmpty();
    }

    // ===== 테스트 데이터 만들기 =====

    private void givenPlacedOrders(Order... orders) {
        given(orderRepository.findByShippingDateAndStatusOrderByIdAsc(SHIPPING_DATE, OrderStatus.PLACED))
                .willReturn(List.of(orders));
    }

    private Menu menu(Long id, String name, int price) {
        Menu menu = new Menu(name, price, "", null);
        ReflectionTestUtils.setField(menu, "id", id);  // DB가 없어서 ID를 직접 넣는다
        return menu;
    }

    private Order order(Long id, String email, OrderItem... items) {
        Order order = Order.create(email, "서울시 실습로 10 101호", "01234", "",
                LocalDateTime.of(2026, 11, 3, 13, 0), SHIPPING_DATE);
        for (OrderItem item : items) {
            order.addItem(item);
        }
        ReflectionTestUtils.setField(order, "id", id);
        return order;
    }
}