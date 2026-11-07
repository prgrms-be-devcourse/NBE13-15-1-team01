package com.kdt.cafesystem.category.service;

import com.kdt.cafesystem.category.dto.CategoryRequestDto;
import com.kdt.cafesystem.category.dto.CategoryResponseDto;
import com.kdt.cafesystem.category.entity.Category;
import com.kdt.cafesystem.category.repository.CategoryRepo;
import com.kdt.cafesystem.global.exception.BusinessException;
import com.kdt.cafesystem.global.exception.ErrorCode;
import com.kdt.cafesystem.menu.repository.MenuRepo;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepo categoryRepo;
    private final MenuRepo menuRepo;

    /** A01 카테고리 목록 */
    @Transactional(readOnly = true)
    public List<CategoryResponseDto> findAll() {
        return categoryRepo.findAllByOrderByIdAsc().stream()
                .map(this::toDto)
                .toList();
    }

    /** A02 카테고리 상세 */
    @Transactional(readOnly = true)
    public CategoryResponseDto find(Long id) {
        return toDto(getCategory(id));
    }

    /** A03 카테고리 등록 */
    public CategoryResponseDto create(CategoryRequestDto request) {
        validate(request);

        String name = request.name().trim();
        if (categoryRepo.findByName(name).isPresent()) {
            throw new BusinessException(ErrorCode.CATEGORY_NAME_DUPLICATED);
        }
        try {
            return toDto(categoryRepo.saveAndFlush(new Category(name)));
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.CATEGORY_NAME_DUPLICATED);
        }
    }

    /** A04 카테고리 수정 */
    public CategoryResponseDto update(Long id, CategoryRequestDto request) {
        Category category = getCategory(id);
        validate(request);

        String name = request.name().trim();
        Optional<Category> sameName = categoryRepo.findByName(name);
        if (sameName.isPresent() && !sameName.get().getId().equals(id)) {
            throw new BusinessException(ErrorCode.CATEGORY_NAME_DUPLICATED);
        }

        category.setName(name);
        return toDto(category);
    }

    /** A05 카테고리 삭제 */
    public void delete(Long id) {
        Category category = getCategory(id);
        if (menuRepo.existsByCategoryId(id)) {
            throw new BusinessException(ErrorCode.CATEGORY_IN_USE);
        }
        categoryRepo.delete(category);
    }

    private Category getCategory(Long id) {
        return categoryRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private CategoryResponseDto toDto(Category category) {
        return new CategoryResponseDto(category.getId(), category.getName());
    }

    // 카테고리명은 앞뒤 공백 제거 후 1~30자
    private void validate(CategoryRequestDto request) {
        if (request.name() == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        String name = request.name().trim();
        if (name.isEmpty() || name.length() > 30) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }
}
