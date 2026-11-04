package com.kdt.cafesystem.category.controller;

import com.kdt.cafesystem.category.dto.CategoryRequestDto;
import com.kdt.cafesystem.category.dto.CategoryResponseDto;
import com.kdt.cafesystem.category.service.CategoryService;
import com.kdt.cafesystem.menu.dto.MenuResponseDto;
import com.kdt.cafesystem.menu.service.MenuService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final MenuService menuService;

    public CategoryController(CategoryService categoryService, MenuService menuService) {
        this.categoryService = categoryService;
        this.menuService = menuService;
    }

    // A01 카테고리 목록 → 200
    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> findAll() {
        return ResponseEntity.ok(categoryService.findAll());
    }

    // A02 카테고리 상세 → 200
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDto> find(@PathVariable Long categoryId) {
        return ResponseEntity.ok(categoryService.find(categoryId));
    }

    // A03 카테고리 등록 → 201 + Location
    @PostMapping
    public ResponseEntity<CategoryResponseDto> create(@Valid @RequestBody CategoryRequestDto requestDto) {
        CategoryResponseDto response = categoryService.create(requestDto);
        URI location = URI.create("/api/v1/categories/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    // A04 카테고리 수정 → 200
    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDto> update(@PathVariable Long categoryId,
                                                      @Valid @RequestBody CategoryRequestDto requestDto) {
        return ResponseEntity.ok(categoryService.update(categoryId, requestDto));
    }

    // A05 카테고리 삭제 → 204, 빈 본문
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> delete(@PathVariable Long categoryId) {
        categoryService.delete(categoryId);
        return ResponseEntity.noContent().build();
    }

    // A15 카테고리별 메뉴 → 200
    @GetMapping("/{categoryId}/menus")
    public ResponseEntity<List<MenuResponseDto>> findAllByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(menuService.findByCategory(categoryId));
    }
}
