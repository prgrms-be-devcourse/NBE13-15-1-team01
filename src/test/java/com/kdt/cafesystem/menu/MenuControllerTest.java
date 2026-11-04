package com.kdt.cafesystem.menu;

import com.kdt.cafesystem.global.exception.BusinessException;
import com.kdt.cafesystem.global.exception.ErrorCode;
import com.kdt.cafesystem.menu.controller.MenuController;
import com.kdt.cafesystem.menu.dto.MenuResponseDto;
import com.kdt.cafesystem.menu.service.MenuService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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

    private static final String VALID_BODY = """
            {"categoryId":1,"name":"케냐 AA","price":15000,"description":"산미가 좋다"}
            """;

    private static String body(String categoryId, String name, String price, String description) {
        return "{\"categoryId\":" + categoryId + ",\"name\":" + name
                + ",\"price\":" + price + ",\"description\":" + description + "}";
    }

    // ---------- A06 ----------

    @Test //"A06 메뉴 목록 → 200"
    void findAll_success() throws Exception {
        given(menuService.findAll()).willReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/menus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("케냐 AA"));
    }

    // ---------- A07 ----------

    @Test //"A07 메뉴 상세 성공 → 200"
    void find_success() throws Exception {
        given(menuService.find(1L)).willReturn(dto);

        mockMvc.perform(get("/api/v1/menus/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("케냐 AA"))
                .andExpect(jsonPath("$.price").value(15000));
    }

    @Test //"A07 없는 메뉴 → 404"
    void find_notFound() throws Exception {
        given(menuService.find(99L))
                .willThrow(new BusinessException(ErrorCode.MENU_NOT_FOUND));

        mockMvc.perform(get("/api/v1/menus/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MENU_NOT_FOUND"));
    }

    // ---------- A08 ----------

    @Test //"A08 등록 성공 → 201 + Location"
    void create_success() throws Exception {
        given(menuService.create(any())).willReturn(dto);

        mockMvc.perform(post("/api/v1/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/menus/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test //"A08 가격 경계값 1, 1000000 → 201"
    void create_priceBoundary_success() throws Exception {
        given(menuService.create(any())).willReturn(dto);

        mockMvc.perform(post("/api/v1/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("1", "\"케냐 AA\"", "1", "\"설명\"")))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("1", "\"케냐 AA\"", "1000000", "\"설명\"")))
                .andExpect(status().isCreated());
    }

    @Test //"A08 이름 공백 → 400"
    void create_blankName() throws Exception {
        mockMvc.perform(post("/api/v1/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("1", "\"   \"", "15000", "\"설명\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test //"A08 가격 0 → 400"
    void create_priceZero() throws Exception {
        mockMvc.perform(post("/api/v1/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("1", "\"케냐 AA\"", "0", "\"설명\"")))
                .andExpect(status().isBadRequest());
    }

    @Test //"A08 가격 1000001 → 400"
    void create_priceTooHigh() throws Exception {
        mockMvc.perform(post("/api/v1/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("1", "\"케냐 AA\"", "1000001", "\"설명\"")))
                .andExpect(status().isBadRequest());
    }

    @Test //"A08 categoryId null → 400"
    void create_categoryIdNull() throws Exception {
        mockMvc.perform(post("/api/v1/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("null", "\"케냐 AA\"", "15000", "\"설명\"")))
                .andExpect(status().isBadRequest());
    }

    @Test //"A08 카테고리 없음 → 404"
    void create_categoryNotFound() throws Exception {
        given(menuService.create(any()))
                .willThrow(new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        mockMvc.perform(post("/api/v1/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"));
    }

    // ---------- A09 ----------

    @Test //"A09 수정 성공 → 200"
    void update_success() throws Exception {
        given(menuService.update(eq(1L), any())).willReturn(dto);

        mockMvc.perform(put("/api/v1/menus/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test //"A09 검증 실패(가격 0) → 400"
    void update_invalid() throws Exception {
        mockMvc.perform(put("/api/v1/menus/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("1", "\"케냐 AA\"", "0", "\"설명\"")))
                .andExpect(status().isBadRequest());
    }

    @Test //"A09 없는 메뉴 → 404"
    void update_notFound() throws Exception {
        given(menuService.update(eq(99L), any()))
                .willThrow(new BusinessException(ErrorCode.MENU_NOT_FOUND));

        mockMvc.perform(put("/api/v1/menus/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound());
    }

    // ---------- A10 ----------

    @Test //"A10 삭제 성공 → 204"
    void delete_success() throws Exception {
        mockMvc.perform(delete("/api/v1/menus/1"))
                .andExpect(status().isNoContent());
    }

    @Test //"A10 없는 메뉴 → 404"
    void delete_notFound() throws Exception {
        willThrow(new BusinessException(ErrorCode.MENU_NOT_FOUND))
                .given(menuService).delete(99L);

        mockMvc.perform(delete("/api/v1/menus/99"))
                .andExpect(status().isNotFound());
    }

    @Test //"A10 주문에 쓰인 메뉴 → 409"
    void delete_inUse() throws Exception {
        willThrow(new BusinessException(ErrorCode.MENU_IN_USE))
                .given(menuService).delete(1L);

        mockMvc.perform(delete("/api/v1/menus/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MENU_IN_USE"));
    }

    // ---------- A19 ----------

    @Test //"A19 판매 중 메뉴 → 200"
    void findAvailable_success() throws Exception {
        given(menuService.findAvailable()).willReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/menus/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].available").value(true));
    }

    // ---------- A20 ----------

    @Test //"A20 검색 성공 → 200"
    void search_success() throws Exception {
        given(menuService.search("케냐")).willReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/menus/search").param("keyword", "케냐"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("케냐 AA"));
    }

    @Test //"A20 keyword 공백 → 400"
    void search_blankKeyword() throws Exception {
        given(menuService.search(" "))
                .willThrow(new BusinessException(ErrorCode.VALIDATION_ERROR));

        mockMvc.perform(get("/api/v1/menus/search").param("keyword", " "))
                .andExpect(status().isBadRequest());
    }

    @Test //"A20 keyword 파라미터 없음 → 400"
    void search_missingKeyword() throws Exception {
        mockMvc.perform(get("/api/v1/menus/search"))
                .andExpect(status().isBadRequest());
    }

    // ---------- A21 ----------

    @Test //"A21 판매 여부 변경 성공 → 200"
    void changeAvailability_success() throws Exception {
        given(menuService.changeAvailability(eq(1L), any())).willReturn(dto);

        mockMvc.perform(put("/api/v1/menus/1/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"available\":false}"))
                .andExpect(status().isOk());
    }

    @Test //"A21 available 누락 → 400"
    void changeAvailability_null() throws Exception {
        mockMvc.perform(put("/api/v1/menus/1/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test //"A21 없는 메뉴 → 404"
    void changeAvailability_notFound() throws Exception {
        given(menuService.changeAvailability(eq(99L), any()))
                .willThrow(new BusinessException(ErrorCode.MENU_NOT_FOUND));

        mockMvc.perform(put("/api/v1/menus/99/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"available\":true}"))
                .andExpect(status().isNotFound());
    }
}
