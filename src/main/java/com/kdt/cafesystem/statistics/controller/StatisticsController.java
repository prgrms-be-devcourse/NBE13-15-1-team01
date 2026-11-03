package com.kdt.cafesystem.statistics.controller;

import com.kdt.cafesystem.global.validation.AllowedDate;
import com.kdt.cafesystem.statistics.dto.OrderStatisticsResponse;
import com.kdt.cafesystem.statistics.service.StatisticsService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    // A17 접수일별 주문 통계 GET
    @GetMapping("/orders")
    public OrderStatisticsResponse getOrderStatistics(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @AllowedDate
            LocalDate date) {
        return statisticsService.getOrderStatistics(date);
    }
}