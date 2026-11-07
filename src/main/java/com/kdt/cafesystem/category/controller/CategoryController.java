package com.kdt.cafesystem.category.controller;

import com.kdt.cafesystem.category.dto.CategoryRequestDto;
import com.kdt.cafesystem.category.dto.CategoryResponseDto;
import com.kdt.cafesystem.category.service.CategoryService;
import com.kdt.cafesystem.menu.dto.MenuResponseDto;
import com.kdt.cafesystem.menu.service.MenuService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final MenuService menuService;

    /** A01 카테고리 목록 */
    @GetMapping
    public List<CategoryResponseDto> findAll() {
        return categoryService.findAll();
    }

    /** A02 카테고리 상세 */
    @GetMapping("/{categoryId}")
    public CategoryResponseDto find(@PathVariable Long categoryId) {
        return categoryService.find(categoryId);
    }

    /** A03 카테고리 등록 → 201 + Location */
    @PostMapping
    public ResponseEntity<CategoryResponseDto> create(@Valid @RequestBody CategoryRequestDto request) {
        CategoryResponseDto response = categoryService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/categories/" + response.id())).body(response);
    }

    /** A04 카테고리 수정 */
    @PutMapping("/{categoryId}")
    public CategoryResponseDto update(@PathVariable Long categoryId,
                                      @Valid @RequestBody CategoryRequestDto request) {
        return categoryService.update(categoryId, request);
    }

    /** A05 카테고리 삭제 → 204, 빈 본문 */
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> delete(@PathVariable Long categoryId) {
        categoryService.delete(categoryId);
        return ResponseEntity.noContent().build();
    }

    /** A15 카테고리별 메뉴 */
    @GetMapping("/{categoryId}/menus")
    public List<MenuResponseDto> findAllByCategory(@PathVariable Long categoryId) {
        return menuService.findByCategory(categoryId);
    }
}
