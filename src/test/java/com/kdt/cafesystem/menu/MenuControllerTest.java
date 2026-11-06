package com.kdt.cafesystem.menu;

import com.kdt.cafesystem.global.exception.BusinessException;
import com.kdt.cafesystem.global.exception.ErrorCode;
import com.kdt.cafesystem.menu.controller.MenuController;
import com.kdt.cafesystem.menu.dto.MenuResponseDto;
import com.kdt.cafesystem.menu.service.MenuService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MenuController.class)
public class MenuControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    MenuService menuService;

    private final MenuResponseDto dto =
            new MenuResponseDto(1L, 1L, "케냐 AA", 15000, "산미가 가득", true);

    // 정상 1
    @Test
    @DisplayName("A07 메뉴 상세는 200과 id·name·price 필드를 반환한다")
    void find_returns200() throws Exception {
        given(menuService.find(1L)).willReturn(dto);

        mockMvc.perform(get("/api/v1/menus/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("케냐 AA"))
                .andExpect(jsonPath("$.price").value(15000));
    }

    // 정상 2
    @Test
    @DisplayName("A10 메뉴 삭제는 204와 빈 본문을 반환한다")
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/menus/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    // 입력 오류 1 (가격 범위)
    @Test
    @DisplayName("A08 가격이 0이면 400 VALIDATION_ERROR와 price 오류를 반환한다")
    void create_priceZero_returns400() throws Exception {
        String body = """
                {"categoryId":1,"name":"케냐 AA","price":0,"description":"설명"}
                """;

        mockMvc.perform(post("/api/v1/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.price").exists());
    }

    // 입력 오류 2 (다른 규칙: 필수값)
    @Test
    @DisplayName("A21 available이 없으면 400 VALIDATION_ERROR와 available 오류를 반환한다")
    void changeAvailability_null_returns400() throws Exception {
        mockMvc.perform(put("/api/v1/menus/1/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.available").exists());
    }

    // 대상 없음
    @Test
    @DisplayName("A07 없는 메뉴 조회는 404 MENU_NOT_FOUND를 반환한다")
    void find_notFound_returns404() throws Exception {
        given(menuService.find(99L))
                .willThrow(new BusinessException(ErrorCode.MENU_NOT_FOUND));

        mockMvc.perform(get("/api/v1/menus/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MENU_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("메뉴를 찾을 수 없습니다."))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    // 업무 위반
    @Test
    @DisplayName("A10 주문에 쓰인 메뉴 삭제는 409 MENU_IN_USE를 반환한다")
    void delete_inUse_returns409() throws Exception {
        willThrow(new BusinessException(ErrorCode.MENU_IN_USE))
                .given(menuService).delete(1L);

        mockMvc.perform(delete("/api/v1/menus/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MENU_IN_USE"))
                .andExpect(jsonPath("$.message").value("주문에 사용된 메뉴는 삭제할 수 없습니다."))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }
}
