package com.kdt.cafesystem.order.dto;

import com.kdt.cafesystem.order.entity.OrderItem;

public record OrderItemResponse(
        Long id,
        Long menuId,
        String menuName,
        int unitPrice,
        int quantity,
        int lineAmount
) {
    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getMenuId(),
                item.getMenuName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getLineAmount());
    }
}
