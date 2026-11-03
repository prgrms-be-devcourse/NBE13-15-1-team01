package com.kdt.cafesystem.category.controller;

import com.kdt.cafesystem.category.dto.CategoryRequestDto;
import com.kdt.cafesystem.category.dto.CategoryResponseDto;
import com.kdt.cafesystem.category.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // A01 카테고리 목록 → 200
    @GetMapping
    public List<CategoryResponseDto> findAll() {
        return categoryService.findAll();
    }

    // A02 카테고리 상세 → 200
    @GetMapping("/{categoryId}")
    public CategoryResponseDto find(@PathVariable Long categoryId) {
        return categoryService.find(categoryId);
    }

    // A03 카테고리 등록 → 201
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponseDto create(@Valid @RequestBody CategoryRequestDto req) {
        return categoryService.create(req);
    }

    // A04 카테고리 수정 → 200
    @PutMapping("/{categoryId}")
    public CategoryResponseDto update(@PathVariable Long categoryId,
                                      @Valid @RequestBody CategoryRequestDto req) {
        return categoryService.update(categoryId, req);
    }

    // A05 카테고리 삭제 → 204, 빈 본문
    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long categoryId) {
        categoryService.delete(categoryId);
    }
}