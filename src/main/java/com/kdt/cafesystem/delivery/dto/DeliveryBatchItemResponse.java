package com.kdt.cafesystem.delivery.dto;

public record DeliveryBatchItemResponse(
        Long menuId,
        String menuName,
        int unitPrice,
        int quantity,
        long lineAmount
) {
}
