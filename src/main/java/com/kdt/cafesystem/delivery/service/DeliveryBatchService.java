package com.kdt.cafesystem.delivery.service;

import com.kdt.cafesystem.delivery.dto.DeliveryBatchItemResponse;
import com.kdt.cafesystem.delivery.dto.DeliveryBatchResponse;
import com.kdt.cafesystem.order.entity.Order;
import com.kdt.cafesystem.order.entity.OrderItem;
import com.kdt.cafesystem.order.entity.OrderStatus;
import com.kdt.cafesystem.order.repository.OrderRepository;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryBatchService {

    // 항목 정렬
    private static final Comparator<ItemKey> ITEM_ORDER = Comparator
            .comparing(ItemKey::menuId)
            .thenComparingInt(ItemKey::unitPrice)
            .thenComparing(ItemKey::menuName);

    private final OrderRepository orderRepository;

    /** A14 배송 묶음 */
    public List<DeliveryBatchResponse> findBatches(LocalDate shippingDate) {
        List<Order> orders = orderRepository
                .findByShippingDateAndStatusOrderByIdAsc(shippingDate, OrderStatus.PLACED);

        Map<String, List<Order>> ordersByEmail = orders.stream()
                .collect(Collectors.groupingBy(Order::getEmail, TreeMap::new, Collectors.toList()));

        return ordersByEmail.values().stream()
                .map(ordersOfEmail -> toBatch(shippingDate, ordersOfEmail))
                .toList();
    }

    private DeliveryBatchResponse toBatch(LocalDate shippingDate, List<Order> ordersOfEmail) {
        Order first = ordersOfEmail.get(0);

        List<Long> orderIds = ordersOfEmail.stream()
                .map(Order::getId)
                .toList();

        List<DeliveryBatchItemResponse> items = mergeItems(ordersOfEmail);

        long totalQuantity = items.stream().mapToLong(DeliveryBatchItemResponse::quantity).sum();
        long totalAmount = items.stream().mapToLong(DeliveryBatchItemResponse::lineAmount).sum();

        return new DeliveryBatchResponse(
                shippingDate,
                first.getEmail(),
                first.getAddress(),
                first.getPostcode(),
                orderIds,
                orderIds.size(),
                totalQuantity,
                totalAmount,
                items);
    }

    // id, 이름, 가격이 같은 항목끼리 합침
    private List<DeliveryBatchItemResponse> mergeItems(List<Order> ordersOfEmail) {
        Map<ItemKey, Integer> quantityByKey = new HashMap<>();
        for (Order order : ordersOfEmail) {
            for (OrderItem item : order.getItems()) {
                ItemKey key = new ItemKey(item.getMenuId(), item.getMenuName(), item.getUnitPrice());
                quantityByKey.merge(key, item.getQuantity(), Integer::sum);
            }
        }

        return quantityByKey.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(ITEM_ORDER))
                .map(entry -> {
                    ItemKey key = entry.getKey();
                    int quantity = entry.getValue();
                    return new DeliveryBatchItemResponse(
                            key.menuId(),
                            key.menuName(),
                            key.unitPrice(),
                            quantity,
                            (long) key.unitPrice() * quantity);
                })
                .toList();
    }

    // 합산 기준: id, 이름, 가격
    private record ItemKey(Long menuId, String menuName, int unitPrice) {
    }
}
