package com.kdt.cafesystem.delivery.dto;

import java.time.LocalDate;
import java.util.List;

public record DeliveryBatchResponse(
        LocalDate shippingDate,
        String email,
        String address,
        String postcode,
        List<Long> orderIds,
        int orderCount,
        long totalQuantity,
        long totalAmount,
        List<DeliveryBatchItemResponse> items
) {
}