package com.kdt.cafesystem.order.dto;

import com.kdt.cafesystem.order.entity.Order;
import com.kdt.cafesystem.order.entity.OrderStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 주문 응답. Entity를 그대로 내보내지 않고 이 DTO로 변환한다.
 * 시각(createdAt, cancelledAt)은 명세 형식 "2026-11-03T13:00:00+09:00" 문자열로 내보낸다.
 */
public record OrderResponse(
        Long id,
        String email,
        String address,
        String postcode,
        String memo,
        OrderStatus status,
        String createdAt,
        LocalDate shippingDate,
        String cancelledAt,
        int totalAmount,
        List<OrderItemResponse> items
) {
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter SEOUL_OFFSET = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getEmail(),
                order.getAddress(),
                order.getPostcode(),
                order.getMemo(),
                order.getStatus(),
                format(order.getCreatedAt()),
                order.getShippingDate(),
                format(order.getCancelledAt()),
                order.getTotalAmount(),
                order.getItems().stream().map(OrderItemResponse::from).toList());
    }

    /** 서울 시각을 "yyyy-MM-ddTHH:mm:ss+09:00" 형식으로. null이면 null(접수 상태의 cancelledAt) */
    public static String format(LocalDateTime seoulTime) {
        return seoulTime == null ? null : seoulTime.atZone(SEOUL).format(SEOUL_OFFSET);
    }
}
