package com.kdt.cafesystem.statistics.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kdt.cafesystem.statistics.dto.OrderStatisticsResponse;
import com.kdt.cafesystem.statistics.service.StatisticsService;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StatisticsController.class)
class StatisticsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    StatisticsService statisticsService;

    @Test
    @DisplayName("A17: 주문 통계를 200과 명세서의 필드 이름으로 반환한다")
    void orderStatistics_ok() throws Exception {
        LocalDate date = LocalDate.of(2026, 11, 3);
        given(statisticsService.getOrderStatistics(date))
                .willReturn(new OrderStatisticsResponse(date, 2, 1, 1, 1, 5000));

        mockMvc.perform(get("/api/v1/statistics/orders").param("date", "2026-11-03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-11-03"))
                .andExpect(jsonPath("$.orderCount").value(2))
                .andExpect(jsonPath("$.placedOrderCount").value(1))
                .andExpect(jsonPath("$.cancelledOrderCount").value(1))
                .andExpect(jsonPath("$.totalQuantity").value(1))
                .andExpect(jsonPath("$.totalAmount").value(5000));
    }

    @Test
    @DisplayName("A17: 2000~2099년 범위를 벗어난 날짜는 400 VALIDATION_ERROR와 fieldErrors.date를 반환한다")
    void orderStatistics_outOfRange() throws Exception {
        mockMvc.perform(get("/api/v1/statistics/orders").param("date", "2100-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.date").exists());
    }
}