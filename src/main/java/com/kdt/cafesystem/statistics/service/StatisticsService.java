package com.kdt.cafesystem.statistics.service;

import com.kdt.cafesystem.order.entity.Order;
import com.kdt.cafesystem.order.entity.OrderItem;
import com.kdt.cafesystem.order.entity.OrderStatus;
import com.kdt.cafesystem.order.repository.OrderRepository;
import com.kdt.cafesystem.statistics.dto.OrderStatisticsResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

    private final OrderRepository orderRepository;

    // A17 접수일별 주문 통계
    public OrderStatisticsResponse getOrderStatistics(LocalDate date) {
        List<Order> orders = findOrdersReceivedOn(date);

        return new OrderStatisticsResponse(
                date,
                orders.size(),
                countByStatus(orders, OrderStatus.PLACED),
                countByStatus(orders, OrderStatus.CANCELLED),
                totalQuantityOfPlaced(orders),
                totalAmountOfPlaced(orders));
    }

    // 서울 기준 접수일이 date인 주문(취소 포함): date 00:00:00 이상 ~ 다음 날 00:00:00 미만
    private List<Order> findOrdersReceivedOn(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return orderRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start, end);
    }

    private long countByStatus(List<Order> orders, OrderStatus status) {
        return orders.stream()
                .filter(order -> order.getStatus() == status)
                .count();
    }

    // 수량은 PLACED 주문만 더한다.
    private long totalQuantityOfPlaced(List<Order> orders) {
        return orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.PLACED)
                .flatMap(order -> order.getItems().stream())
                .mapToLong(OrderItem::getQuantity)
                .sum();
    }

    // 금액은 PLACED 주문만 더한다.
    private long totalAmountOfPlaced(List<Order> orders) {
        return orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.PLACED)
                .mapToLong(Order::getTotalAmount)
                .sum();
    }
}