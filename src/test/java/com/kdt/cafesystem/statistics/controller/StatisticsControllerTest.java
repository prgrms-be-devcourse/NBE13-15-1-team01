package com.kdt.cafesystem.statistics.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kdt.cafesystem.statistics.dto.CustomerStatisticsResponse;
import com.kdt.cafesystem.statistics.dto.MenuStatisticsResponse;
import com.kdt.cafesystem.statistics.dto.OrderStatisticsResponse;
import com.kdt.cafesystem.statistics.service.StatisticsService;
import java.time.LocalDate;
import java.util.List;
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


    @Test
    @DisplayName("A18: 메뉴 통계를 200과 명세서의 필드 이름으로 반환한다")
    void menuStatistics_ok() throws Exception {
        LocalDate date = LocalDate.of(2026, 11, 3);
        given(statisticsService.getMenuStatistics(date)).willReturn(List.of(
                new MenuStatisticsResponse(1L, "Columbia Nariñó", 5000, 3, 15000),
                new MenuStatisticsResponse(2L, "Brazil Serra Do Caparaó", 7000, 1, 7000)));

        mockMvc.perform(get("/api/v1/statistics/menus").param("date", "2026-11-03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].menuId").value(1))
                .andExpect(jsonPath("$[0].menuName").value("Columbia Nariñó"))
                .andExpect(jsonPath("$[0].unitPrice").value(5000))
                .andExpect(jsonPath("$[0].totalQuantity").value(3))
                .andExpect(jsonPath("$[0].totalAmount").value(15000))
                .andExpect(jsonPath("$[1].menuId").value(2));
    }

    @Test
    @DisplayName("A18: 주문이 없는 날은 200과 빈 배열을 반환한다")
    void menuStatistics_empty() throws Exception {
        given(statisticsService.getMenuStatistics(LocalDate.of(2030, 1, 1))).willReturn(List.of());

        mockMvc.perform(get("/api/v1/statistics/menus").param("date", "2030-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("A18: 2000~2099년 범위를 벗어난 날짜는 400 VALIDATION_ERROR와 fieldErrors.date를 반환한다")
    void menuStatistics_outOfRange() throws Exception {
        mockMvc.perform(get("/api/v1/statistics/menus").param("date", "1999-12-31"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.date").exists());
    }


    @Test
    @DisplayName("A24: 고객 통계를 200과 명세서의 필드 이름으로 반환한다")
    void customerStatistics_ok() throws Exception {
        LocalDate date = LocalDate.of(2026, 11, 3);
        given(statisticsService.getCustomerStatistics(date)).willReturn(List.of(
                new CustomerStatisticsResponse(date, "buyer@example.com", 2, 1, 1, 1, 5000)));

        mockMvc.perform(get("/api/v1/statistics/customers").param("date", "2026-11-03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2026-11-03"))
                .andExpect(jsonPath("$[0].email").value("buyer@example.com"))
                .andExpect(jsonPath("$[0].orderCount").value(2))
                .andExpect(jsonPath("$[0].placedOrderCount").value(1))
                .andExpect(jsonPath("$[0].cancelledOrderCount").value(1))
                .andExpect(jsonPath("$[0].totalQuantity").value(1))
                .andExpect(jsonPath("$[0].totalAmount").value(5000));
    }

    @Test
    @DisplayName("A24: 주문이 없는 날은 200과 빈 배열을 반환한다")
    void customerStatistics_empty() throws Exception {
        given(statisticsService.getCustomerStatistics(LocalDate.of(2030, 1, 1))).willReturn(List.of());

        mockMvc.perform(get("/api/v1/statistics/customers").param("date", "2030-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("A24: 2000~2099년 범위를 벗어난 날짜는 400 VALIDATION_ERROR와 fieldErrors.date를 반환한다")
    void customerStatistics_outOfRange() throws Exception {
        mockMvc.perform(get("/api/v1/statistics/customers").param("date", "2100-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.date").exists());
    }
}