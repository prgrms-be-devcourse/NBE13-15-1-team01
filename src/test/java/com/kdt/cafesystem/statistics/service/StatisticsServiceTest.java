package com.kdt.cafesystem.statistics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.kdt.cafesystem.menu.entity.Menu;
import com.kdt.cafesystem.order.entity.Order;
import com.kdt.cafesystem.order.entity.OrderItem;
import com.kdt.cafesystem.order.repository.OrderRepository;
import com.kdt.cafesystem.statistics.dto.CustomerStatisticsResponse;
import com.kdt.cafesystem.statistics.dto.MenuStatisticsResponse;
import com.kdt.cafesystem.statistics.dto.OrderStatisticsResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * DB 없이 통계 계산만 검증한다.
 * Repository는 가짜(mock)로 만들고, 돌려줄 주문은 자바 코드로 직접 만든다.
 */
class StatisticsServiceTest {

    private static final LocalDate DATE = LocalDate.of(2026, 11, 3);

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final StatisticsService statisticsService = new StatisticsService(orderRepository);

    private final Menu columbia = menu(1L, "Columbia Nariñó", 5000);
    private final Menu brazil = menu(2L, "Brazil Serra Do Caparaó", 7000);

    @Test
    @DisplayName("A17: 접수일의 주문 수와 PLACED 주문의 수량·금액 합계를 반환한다")
    void orderStatistics() {
        Order order101 = order(101L, OrderItem.of(columbia, 2), OrderItem.of(brazil, 1)); // 17,000원
        Order order102 = order(102L, OrderItem.of(columbia, 1));                          //  5,000원
        givenOrdersReceivedOn(DATE, order101, order102);

        OrderStatisticsResponse result = statisticsService.getOrderStatistics(DATE);

        assertThat(result.date()).isEqualTo(DATE);
        assertThat(result.orderCount()).isEqualTo(2);
        assertThat(result.placedOrderCount()).isEqualTo(2);
        assertThat(result.cancelledOrderCount()).isZero();
        assertThat(result.totalQuantity()).isEqualTo(4);
        assertThat(result.totalAmount()).isEqualTo(22000);
    }

    @Test
    @DisplayName("A17: 취소한 주문은 건수에는 포함하고 수량·금액에서는 뺀다 (명세서 예시: 주문 101 취소)")
    void cancelledOrderExcludedFromTotals() {
        Order order101 = order(101L, OrderItem.of(columbia, 2), OrderItem.of(brazil, 1));
        Order order102 = order(102L, OrderItem.of(columbia, 1));
        order101.cancel(LocalDateTime.of(2026, 11, 3, 13, 30));
        givenOrdersReceivedOn(DATE, order101, order102);

        OrderStatisticsResponse result = statisticsService.getOrderStatistics(DATE);

        assertThat(result.orderCount()).isEqualTo(2);
        assertThat(result.placedOrderCount()).isEqualTo(1);
        assertThat(result.cancelledOrderCount()).isEqualTo(1);
        assertThat(result.totalQuantity()).isEqualTo(1);
        assertThat(result.totalAmount()).isEqualTo(5000);
    }

    @Test
    @DisplayName("A17: 주문이 없는 날도 조회한 날짜와 0으로 채운 객체를 반환한다")
    void noOrders() {
        givenOrdersReceivedOn(DATE);

        OrderStatisticsResponse result = statisticsService.getOrderStatistics(DATE);

        assertThat(result).isEqualTo(new OrderStatisticsResponse(DATE, 0, 0, 0, 0, 0));
    }

    @Test
    @DisplayName("A17: 접수일 범위를 그날 00:00:00 이상 ~ 다음 날 00:00:00 미만으로 조회한다")
    void queryRangeIsWholeDay() {
        Order order = order(101L, OrderItem.of(columbia, 1));
        given(orderRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                LocalDateTime.of(2026, 11, 3, 0, 0, 0),
                LocalDateTime.of(2026, 11, 4, 0, 0, 0)))
                .willReturn(List.of(order));

        OrderStatisticsResponse result = statisticsService.getOrderStatistics(DATE);

        // 범위를 잘못 만들면 가짜 Repository가 빈 목록을 돌려줘서 0이 나온다
        assertThat(result.orderCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("A18: PLACED 주문의 항목을 메뉴별로 합산하고 메뉴 ID 오름차순으로 반환한다")
    void menuStatistics() {
        Order order101 = order(101L, OrderItem.of(brazil, 1), OrderItem.of(columbia, 2));
        Order order102 = order(102L, OrderItem.of(columbia, 1));
        givenOrdersReceivedOn(DATE, order101, order102);

        List<MenuStatisticsResponse> result = statisticsService.getMenuStatistics(DATE);

        assertThat(result)
                .extracting(MenuStatisticsResponse::menuId,
                        MenuStatisticsResponse::menuName,
                        MenuStatisticsResponse::unitPrice,
                        MenuStatisticsResponse::totalQuantity,
                        MenuStatisticsResponse::totalAmount)
                .containsExactly(
                        tuple(1L, "Columbia Nariñó", 5000, 3L, 15000L),
                        tuple(2L, "Brazil Serra Do Caparaó", 7000, 1L, 7000L));
    }

    @Test
    @DisplayName("A18: 취소한 주문의 항목은 빼고, 취소 주문에만 있던 메뉴는 목록에 넣지 않는다")
    void menuStatisticsExcludesCancelled() {
        Order order101 = order(101L, OrderItem.of(columbia, 2), OrderItem.of(brazil, 1));
        Order order102 = order(102L, OrderItem.of(columbia, 1));
        order101.cancel(LocalDateTime.of(2026, 11, 3, 13, 30));
        givenOrdersReceivedOn(DATE, order101, order102);

        List<MenuStatisticsResponse> result = statisticsService.getMenuStatistics(DATE);

        assertThat(result)
                .extracting(MenuStatisticsResponse::menuId,
                        MenuStatisticsResponse::totalQuantity,
                        MenuStatisticsResponse::totalAmount)
                .containsExactly(tuple(1L, 1L, 5000L));
    }

    @Test
    @DisplayName("A18: 같은 메뉴라도 주문 당시 단가가 다르면 따로 집계하고 단가 오름차순으로 반환한다")
    void menuStatisticsSplitsByUnitPrice() {
        OrderItem cheap = OrderItem.of(columbia, 2);     // 5,000원일 때 주문
        columbia.setPrice(6000);                          // 메뉴 가격 수정
        OrderItem expensive = OrderItem.of(columbia, 1); // 6,000원일 때 주문
        givenOrdersReceivedOn(DATE, order(101L, expensive), order(102L, cheap));

        List<MenuStatisticsResponse> result = statisticsService.getMenuStatistics(DATE);

        assertThat(result)
                .extracting(MenuStatisticsResponse::unitPrice,
                        MenuStatisticsResponse::totalQuantity,
                        MenuStatisticsResponse::totalAmount)
                .containsExactly(
                        tuple(5000, 2L, 10000L),
                        tuple(6000, 1L, 6000L));
    }

    @Test
    @DisplayName("A18: 주문이 없는 날은 빈 목록을 반환한다")
    void menuStatisticsEmpty() {
        givenOrdersReceivedOn(DATE);

        assertThat(statisticsService.getMenuStatistics(DATE)).isEmpty();
    }

    @Test
    @DisplayName("A24: 이메일별로 주문 수와 PLACED 주문의 수량·금액을 계산하고 이메일 오름차순으로 반환한다")
    void customerStatistics() {
        Order order101 = order(101L, "buyer@example.com", OrderItem.of(columbia, 2), OrderItem.of(brazil, 1)); // 17,000원
        Order order102 = order(102L, "buyer@example.com", OrderItem.of(columbia, 1));                          //  5,000원
        Order order103 = order(103L, "abc@example.com", OrderItem.of(brazil, 1));                              //  7,000원
        givenOrdersReceivedOn(DATE, order101, order102, order103);

        List<CustomerStatisticsResponse> result = statisticsService.getCustomerStatistics(DATE);

        assertThat(result).containsExactly(
                new CustomerStatisticsResponse(DATE, "abc@example.com", 1, 1, 0, 1, 7000),
                new CustomerStatisticsResponse(DATE, "buyer@example.com", 2, 2, 0, 4, 22000));
    }

    @Test
    @DisplayName("A24: 취소한 주문은 건수에만 포함하고 수량·금액에서는 뺀다 (명세서 예시: 주문 101 취소)")
    void customerStatisticsWithCancelledOrder() {
        Order order101 = order(101L, "buyer@example.com", OrderItem.of(columbia, 2), OrderItem.of(brazil, 1));
        Order order102 = order(102L, "buyer@example.com", OrderItem.of(columbia, 1));
        order101.cancel(LocalDateTime.of(2026, 11, 3, 13, 30));
        givenOrdersReceivedOn(DATE, order101, order102);

        List<CustomerStatisticsResponse> result = statisticsService.getCustomerStatistics(DATE);

        assertThat(result).containsExactly(
                new CustomerStatisticsResponse(DATE, "buyer@example.com", 2, 1, 1, 1, 5000));
    }

    @Test
    @DisplayName("A24: 주문을 모두 취소한 고객도 목록에 포함하고 PLACED 주문 수·수량·금액은 0이다")
    void customerWithOnlyCancelledOrders() {
        Order cancelled = order(101L, "cancel@example.com", OrderItem.of(columbia, 2));
        cancelled.cancel(LocalDateTime.of(2026, 11, 3, 13, 30));
        Order placed = order(102L, "buyer@example.com", OrderItem.of(brazil, 1));
        givenOrdersReceivedOn(DATE, cancelled, placed);

        List<CustomerStatisticsResponse> result = statisticsService.getCustomerStatistics(DATE);

        assertThat(result).containsExactly(
                new CustomerStatisticsResponse(DATE, "buyer@example.com", 1, 1, 0, 1, 7000),
                new CustomerStatisticsResponse(DATE, "cancel@example.com", 1, 0, 1, 0, 0));
    }

    @Test
    @DisplayName("A24: 대소문자만 다른 이메일은 다른 고객이다")
    void customerEmailIsCaseSensitive() {
        givenOrdersReceivedOn(DATE,
                order(101L, "buyer@example.com", OrderItem.of(columbia, 1)),
                order(102L, "Buyer@example.com", OrderItem.of(columbia, 1)));

        List<CustomerStatisticsResponse> result = statisticsService.getCustomerStatistics(DATE);

        assertThat(result)
                .extracting(CustomerStatisticsResponse::email)
                .containsExactly("Buyer@example.com", "buyer@example.com");
    }

    @Test
    @DisplayName("A24: 주문이 없는 날은 빈 목록을 반환한다")
    void customerStatisticsEmpty() {
        givenOrdersReceivedOn(DATE);

        assertThat(statisticsService.getCustomerStatistics(DATE)).isEmpty();
    }

    // ===== 테스트 데이터 만들기 =====

    private void givenOrdersReceivedOn(LocalDate date, Order... orders) {
        given(orderRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                date.atStartOfDay(), date.plusDays(1).atStartOfDay()))
                .willReturn(List.of(orders));
    }

    private static Menu menu(Long id, String name, int price) {
        Menu menu = new Menu(name, price, "", null);
        ReflectionTestUtils.setField(menu, "id", id);  // DB가 없어서 ID를 직접 넣는다
        return menu;
    }

    private Order order(Long id, OrderItem... items) {
        Order order = Order.create("buyer@example.com", "서울시 실습로 10 101호", "01234", "",
                LocalDateTime.of(2026, 11, 3, 13, 0), DATE);
        for (OrderItem item : items) {
            order.addItem(item);
        }
        ReflectionTestUtils.setField(order, "id", id);
        return order;
    }


    // A24: 이메일을 지정해서 주문 만들기
    private Order order(Long id, String email, OrderItem... items) {
        Order order = Order.create(email, "서울시 실습로 10 101호", "01234", "",
                LocalDateTime.of(2026, 11, 3, 13, 0), DATE);
        for (OrderItem item : items) {
            order.addItem(item);
        }
        ReflectionTestUtils.setField(order, "id", id);
        return order;
    }
}