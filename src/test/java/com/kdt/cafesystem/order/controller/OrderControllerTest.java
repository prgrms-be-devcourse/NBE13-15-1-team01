package com.kdt.cafesystem.order.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kdt.cafesystem.global.exception.BusinessException;
import com.kdt.cafesystem.global.exception.ErrorCode;
import com.kdt.cafesystem.order.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    OrderService orderService;

    @Test
    @DisplayName("A11: 우편번호가 5자리 미만이면 400 VALIDATION_ERROR와 fieldErrors.postcode를 반환한다")
    void createOrder_invalidPostcode() throws Exception {
        String body = """
                {
                  "email": "buyer@example.com",
                  "address": "서울시 강남구 테헤란로 1",
                  "postcode": "1234",
                  "items": [{ "menuId": 1, "quantity": 1 }]
                }
                """;

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.postcode").exists());
    }

    @Test
    @DisplayName("A13: 없는 주문을 조회하면 404 ORDER_NOT_FOUND와 빈 fieldErrors를 반환한다")
    void findOrder_notFound() throws Exception {
        given(orderService.findById(999L))
                .willThrow(new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        mockMvc.perform(get("/api/v1/orders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    private String orderBody(String itemsJson) {
        return """
                {
                  "email": "buyer@example.com",
                  "address": "서울시 강남구 테헤란로 1",
                  "postcode": "06234",
                  "items": %s
                }
                """.formatted(itemsJson);
    }

    @Test
    @DisplayName("A11: 서로 다른 메뉴가 10개를 넘으면 400 VALIDATION_ERROR와 fieldErrors.items를 반환한다")
    void createOrder_tooManyItems() throws Exception {
        String items = java.util.stream.IntStream.rangeClosed(1, 11)
                .mapToObj(id -> "{ \"menuId\": " + id + ", \"quantity\": 1 }")
                .collect(java.util.stream.Collectors.joining(",", "[", "]"));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody(items)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.items").exists());
    }

    @Test
    @DisplayName("A11: 같은 메뉴 ID가 중복되면 400 VALIDATION_ERROR와 fieldErrors.items를 반환한다")
    void createOrder_duplicateMenuId() throws Exception {
        String items = "[{ \"menuId\": 1, \"quantity\": 1 }, { \"menuId\": 1, \"quantity\": 2 }]";

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody(items)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.items").exists());
    }
}
