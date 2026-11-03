package com.kdt.cafesystem.delivery.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kdt.cafesystem.delivery.dto.DeliveryBatchItemResponse;
import com.kdt.cafesystem.delivery.dto.DeliveryBatchResponse;
import com.kdt.cafesystem.delivery.service.DeliveryBatchService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DeliveryBatchController.class)
class DeliveryBatchControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    DeliveryBatchService deliveryBatchService;

    @Test
    @DisplayName("A14: 배송 묶음을 200과 명세서의 필드 이름으로 반환한다")
    void findBatches_ok() throws Exception {
        LocalDate shippingDate = LocalDate.of(2026, 11, 3);
        given(deliveryBatchService.findBatches(shippingDate)).willReturn(List.of(
                new DeliveryBatchResponse(shippingDate, "buyer@example.com", "서울시 실습로 10 101호", "01234",
                        List.of(101L, 102L), 2, 4, 22000,
                        List.of(new DeliveryBatchItemResponse(1L, "Columbia Nariñó", 5000, 3, 15000),
                                new DeliveryBatchItemResponse(2L, "Brazil Serra Do Caparaó", 7000, 1, 7000)))));

        mockMvc.perform(get("/api/v1/delivery-batches").param("shippingDate", "2026-11-03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].shippingDate").value("2026-11-03"))
                .andExpect(jsonPath("$[0].postcode").value("01234"))
                .andExpect(jsonPath("$[0].orderIds[1]").value(102))
                .andExpect(jsonPath("$[0].orderCount").value(2))
                .andExpect(jsonPath("$[0].totalAmount").value(22000))
                .andExpect(jsonPath("$[0].items[0].lineAmount").value(15000));
    }

    @Test
    @DisplayName("A14: 2000~2099년 범위를 벗어난 배송일은 400 VALIDATION_ERROR와 fieldErrors.shippingDate를 반환한다")
    void findBatches_outOfRange() throws Exception {
        mockMvc.perform(get("/api/v1/delivery-batches").param("shippingDate", "1999-12-31"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.shippingDate").exists());
    }
}