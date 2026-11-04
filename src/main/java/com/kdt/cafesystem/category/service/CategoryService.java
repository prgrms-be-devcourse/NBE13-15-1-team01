package com.kdt.cafesystem.category.service;

import com.kdt.cafesystem.category.dto.CategoryRequestDto;
import com.kdt.cafesystem.category.dto.CategoryResponseDto;
import com.kdt.cafesystem.category.entity.Category;
import com.kdt.cafesystem.category.repository.CategoryRepo;
import com.kdt.cafesystem.global.exception.BusinessException;
import com.kdt.cafesystem.global.exception.ErrorCode;
import com.kdt.cafesystem.menu.repository.MenuRepo;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepo categoryRepo;
    private final MenuRepo menuRepo;

    public CategoryService(CategoryRepo categoryRepo, MenuRepo menuRepo) {
        this.categoryRepo = categoryRepo;
        this.menuRepo = menuRepo;
    }


    @Transactional(readOnly = true)
    public List<CategoryResponseDto> findAll() {
        List<Category> categories = categoryRepo.findAllByOrderByIdAsc();
        List<CategoryResponseDto> result = new ArrayList<>();
        for (Category category : categories) {
            result.add(toDto(category));
        }
        return result;
    }

    // A02
    @Transactional(readOnly = true)
    public CategoryResponseDto find(Long id) {
        Optional<Category> foundCategory = categoryRepo.findById(id);
        if (foundCategory.isEmpty()) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        return toDto(foundCategory.get());
    }

    // A03
    public CategoryResponseDto create(CategoryRequestDto req) {
        validate(req);

        String name = req.name().trim();
        if (categoryRepo.findByName(name).isPresent()) {
            throw new BusinessException(ErrorCode.CATEGORY_NAME_DUPLICATED);
        }
        try {
            Category saved = categoryRepo.saveAndFlush(new Category(name));
            return toDto(saved);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.CATEGORY_NAME_DUPLICATED);
        }
    }

    // A04
    public CategoryResponseDto update(Long id, CategoryRequestDto req) {
        Optional<Category> foundCategory = categoryRepo.findById(id);
        if (foundCategory.isEmpty()) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        validate(req);

        Category category = foundCategory.get();
        String name = req.name().trim();

        Optional<Category> found = categoryRepo.findByName(name);
        if (found.isPresent() && !found.get().getId().equals(id)) {
            throw new BusinessException(ErrorCode.CATEGORY_NAME_DUPLICATED);
        }

        category.setName(name);
        return toDto(category);
    }

    // A05
    public void delete(Long id) {
        Optional<Category> foundCategory = categoryRepo.findById(id);
        if (foundCategory.isEmpty()) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        Category category = foundCategory.get();
        if (menuRepo.existsByCategoryId(id)) {
            throw new BusinessException(ErrorCode.CATEGORY_IN_USE);
        }
        categoryRepo.delete(category);
    }

    private CategoryResponseDto toDto(Category c) {
        return new CategoryResponseDto(c.getId(), c.getName());
    }

    // 카테고리명 검증
    private void validate(CategoryRequestDto req) {
        String name = req.name().trim();

        // 카테고리명은 앞뒤 공백 제거 후 1~30자
        if (name.isEmpty() || name.length() > 30) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }
}

