package com.kdt.cafesystem.order.dto;

import com.kdt.cafesystem.order.entity.Order;
import com.kdt.cafesystem.order.entity.OrderStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 응답. Entity를 그대로 내보내지 않고 이 DTO로 변환한다.
 * 필드 이름은 명세서 응답 예시에 맞게 조정할 것.
 */
public record OrderResponse(
        Long id,
        String email,
        String address,
        String postcode,
        String memo,
        OrderStatus status,
        LocalDateTime createdAt,
        LocalDate shippingDate,
        LocalDateTime cancelledAt,
        int totalAmount,
        List<OrderItemResponse> items
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getEmail(),
                order.getAddress(),
                order.getPostcode(),
                order.getMemo(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getShippingDate(),
                order.getCancelledAt(),
                order.getTotalAmount(),
                order.getItems().stream().map(OrderItemResponse::from).toList());
    }
}
