package com.kdt.cafesystem.category.controller;

import com.kdt.cafesystem.category.dto.CategoryResponseDto;
import com.kdt.cafesystem.category.service.CategoryService;
import com.kdt.cafesystem.global.exception.BusinessException;
import com.kdt.cafesystem.global.exception.ErrorCode;
import com.kdt.cafesystem.menu.service.MenuService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CategoryService categoryService;

    // CategoryController가 생성자로 함께 주입받으므로 필요
    @MockitoBean
    MenuService menuService;

    // 정상 1
    @Test
    @DisplayName("A01 카테고리 목록은 200과 id·name 필드를 반환한다")
    void list_returns200() throws Exception {
        given(categoryService.findAll())
                .willReturn(List.of(new CategoryResponseDto(1L, "싱글오리진")));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("싱글오리진"));
    }

    // 정상 2
    @Test
    @DisplayName("A05 카테고리 삭제는 204와 빈 본문을 반환한다")
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/categories/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    // 입력 오류 1
    @Test
    @DisplayName("A03 카테고리명이 공백이면 400 VALIDATION_ERROR와 name 오류를 반환한다")
    void create_blankName_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }

    // 입력 오류 2 (다른 규칙: 최대 길이 30자)
    @Test
    @DisplayName("A03 카테고리명이 30자를 넘으면 400 VALIDATION_ERROR와 name 오류를 반환한다")
    void create_nameTooLong_returns400() throws Exception {
        String longName = "가".repeat(31);

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + longName + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }

    // 대상 없음
    @Test
    @DisplayName("A02 없는 카테고리 조회는 404 CATEGORY_NOT_FOUND를 반환한다")
    void find_notFound_returns404() throws Exception {
        given(categoryService.find(99L))
                .willThrow(new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        mockMvc.perform(get("/api/v1/categories/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("카테고리를 찾을 수 없습니다."))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    // 업무 위반
    @Test
    @DisplayName("A03 중복된 카테고리명 등록은 409 CATEGORY_NAME_DUPLICATED를 반환한다")
    void create_duplicate_returns409() throws Exception {
        given(categoryService.create(any()))
                .willThrow(new BusinessException(ErrorCode.CATEGORY_NAME_DUPLICATED));

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"싱글오리진\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CATEGORY_NAME_DUPLICATED"))
                .andExpect(jsonPath("$.message").value("같은 이름의 카테고리가 이미 있습니다."))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }
}
