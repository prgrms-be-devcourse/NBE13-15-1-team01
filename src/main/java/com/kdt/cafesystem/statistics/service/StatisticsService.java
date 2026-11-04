package com.kdt.cafesystem.statistics.service;

import com.kdt.cafesystem.order.entity.Order;
import com.kdt.cafesystem.order.entity.OrderItem;
import com.kdt.cafesystem.order.entity.OrderStatus;
import com.kdt.cafesystem.order.repository.OrderRepository;
import com.kdt.cafesystem.statistics.dto.MenuStatisticsResponse;
import com.kdt.cafesystem.statistics.dto.OrderStatisticsResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

    // A18 메뉴 통계 정렬: id, 가격, 메뉴명 오름차순
    private static final Comparator<MenuKey> MENU_ORDER = Comparator
            .comparing(MenuKey::menuId)
            .thenComparingInt(MenuKey::unitPrice)
            .thenComparing(MenuKey::menuName);

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

    // A18 접수일별 메뉴 통계
    public List<MenuStatisticsResponse> getMenuStatistics(LocalDate date) {
        List<Order> orders = findOrdersReceivedOn(date);

        // 메뉴id,이름,가격이 같은 항목끼리 수량을 더한다 (PLACED만)
        Map<MenuKey, Long> quantityByMenu = new HashMap<>();
        for (Order order : orders) {
            if (order.getStatus() != OrderStatus.PLACED) {
                continue;  // 취소 주문의 항목은 집계하지 않는다
            }
            for (OrderItem item : order.getItems()) {
                MenuKey key = new MenuKey(item.getMenuId(), item.getMenuName(), item.getUnitPrice());
                quantityByMenu.merge(key, (long) item.getQuantity(), Long::sum);
            }
        }

        return quantityByMenu.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(MENU_ORDER))
                .map(entry -> {
                    MenuKey key = entry.getKey();
                    long totalQuantity = entry.getValue();
                    return new MenuStatisticsResponse(
                            key.menuId(),
                            key.menuName(),
                            key.unitPrice(),
                            totalQuantity,
                            key.unitPrice() * totalQuantity);
                })
                .toList();
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

    // A18 합산 기준: 이 세 값이 모두 같아야 같은 메뉴 줄로 본다
    private record MenuKey(Long menuId, String menuName, int unitPrice) {
    }
}