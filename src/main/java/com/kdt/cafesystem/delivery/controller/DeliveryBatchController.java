package com.kdt.cafesystem.delivery.controller;

import com.kdt.cafesystem.delivery.dto.DeliveryBatchResponse;
import com.kdt.cafesystem.delivery.service.DeliveryBatchService;
import com.kdt.cafesystem.global.validation.AllowedDate;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/delivery-batches")
@RequiredArgsConstructor
public class DeliveryBatchController {

    private final DeliveryBatchService deliveryBatchService;

    /** A14 배송 묶음  GET /api/v1/delivery-batches?shippingDate=2026-11-03 */
    @GetMapping
    public List<DeliveryBatchResponse> findBatches(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @AllowedDate
            LocalDate shippingDate) {
        return deliveryBatchService.findBatches(shippingDate);
    }
}