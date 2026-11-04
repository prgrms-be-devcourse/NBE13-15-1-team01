package com.kdt.cafesystem.menu.service;

import com.kdt.cafesystem.category.entity.Category;
import com.kdt.cafesystem.category.repository.CategoryRepo;
import com.kdt.cafesystem.global.exception.BusinessException;
import com.kdt.cafesystem.global.exception.ErrorCode;
import com.kdt.cafesystem.menu.dto.MenuAvailabilityRequestDto;
import com.kdt.cafesystem.menu.dto.MenuRequestDto;
import com.kdt.cafesystem.menu.dto.MenuResponseDto;
import com.kdt.cafesystem.menu.entity.Menu;
import com.kdt.cafesystem.menu.repository.MenuRepo;
import com.kdt.cafesystem.order.repository.OrderItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MenuService {

    private final MenuRepo menuRepo;
    private final CategoryRepo categoryRepo;
    private final OrderItemRepository orderItemRepository;
    public MenuService(MenuRepo menuRepo, CategoryRepo categoryRepo, OrderItemRepository orderItemRepository) {
        this.menuRepo = menuRepo;
        this.categoryRepo = categoryRepo;
        this.orderItemRepository = orderItemRepository;
    }

    // A06
    @Transactional(readOnly = true)
    public List<MenuResponseDto> findAll() {
        return toDtoList(menuRepo.findAllByOrderByIdAsc());
    }

    // A07
    @Transactional(readOnly = true)
    public MenuResponseDto find(Long id) {
        Optional<Menu> foundMenu = menuRepo.findById(id);
        if (foundMenu.isEmpty()) {
            throw new BusinessException(ErrorCode.MENU_NOT_FOUND);
        }
        return toDto(foundMenu.get());
    }

    // A15 카테고리별 목록
    @Transactional(readOnly = true)
    public List<MenuResponseDto> findByCategory(Long categoryId) {
        Optional<Category> foundCategory = categoryRepo.findById(categoryId);
        if (foundCategory.isEmpty()) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        return toDtoList(menuRepo.findByCategoryIdOrderByIdAsc(categoryId));
    }

    // A08
    public MenuResponseDto create(MenuRequestDto req) {
        validate(req);

        Optional<Category> foundCategory = categoryRepo.findById(req.categoryId());
        if (foundCategory.isEmpty()) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        Menu menu = new Menu(req.name().trim(), req.price(), req.description(), foundCategory.get());
        return toDto(menuRepo.save(menu));
    }

    // A09
    public MenuResponseDto update(Long id, MenuRequestDto req) {
        validate(req);

        Optional<Menu> foundMenu = menuRepo.findById(id);
        if (foundMenu.isEmpty()) {
            throw new BusinessException(ErrorCode.MENU_NOT_FOUND);
        }
        Optional<Category> foundCategory = categoryRepo.findById(req.categoryId());
        if (foundCategory.isEmpty()) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        Menu menu = foundMenu.get();
        menu.setCategory(foundCategory.get());
        menu.setName(req.name().trim());
        menu.setPrice(req.price());
        menu.setDescription(req.description());
        return toDto(menu);
    }

    // A10
    public void delete(Long id) {
        Optional<Menu> foundMenu = menuRepo.findById(id);

        //찾는 메뉴가 없습니다
        if (foundMenu.isEmpty()) {
            throw new BusinessException(ErrorCode.MENU_NOT_FOUND);
        }
        if(orderItemRepository.existsByMenu_Id(id)){
            throw new BusinessException(ErrorCode.MENU_IN_USE);
        }

        menuRepo.delete(foundMenu.get());
    }

    // A19 판매 중 메뉴
    @Transactional(readOnly = true)
    public List<MenuResponseDto> findAvailable() {
        return toDtoList(menuRepo.findByAvailableTrueOrderByIdAsc());
    }

    // A20 메뉴명 검색: 결과가 없으면 []
    @Transactional(readOnly = true)
    public List<MenuResponseDto> search(String keyword) {
        // 검색어가 null이면 오류
        if (keyword == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }

        // 검색어 앞뒤 공백 제거
        String trimmedKeyword = keyword.trim();

        // 검색어 길이 검증
        if (trimmedKeyword.isEmpty() || trimmedKeyword.length() > 100) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }

        // 메뉴 검색 후 DTO로 변환
        return toDtoList(
                menuRepo.findByNameContainingOrderByIdAsc(trimmedKeyword)
        );
    }

    // A21 판매 여부 변경: available만 바꿈
    public MenuResponseDto changeAvailability(Long id, MenuAvailabilityRequestDto req) {
        Optional<Menu> foundMenu = menuRepo.findById(id);
        if (foundMenu.isEmpty()) {
            throw new BusinessException(ErrorCode.MENU_NOT_FOUND);
        }
        Menu menu = foundMenu.get();
        menu.setAvailable(req.available());
        return toDto(menu);
    }

    private List<MenuResponseDto> toDtoList(List<Menu> menus) {
        List<MenuResponseDto> result = new ArrayList<>();
        for (Menu menu : menus) {
            result.add(toDto(menu));
        }
        return result;
    }

    private MenuResponseDto toDto(Menu m) {
        return new MenuResponseDto(m.getId(), m.getCategory().getId(),
                m.getName(), m.getPrice(), m.getDescription(), m.isAvailable());
    }

    // 메뉴명, 가격, 설명 검증
    private void validate(MenuRequestDto req) {
        if (req.categoryId() == null || req.name() == null
                || req.price() == null || req.description() == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }

        String name = req.name().trim();

        // 메뉴명은 앞뒤 공백 제거 후 1~100자
        if (name.isEmpty() || name.length() > 100) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        // 가격은 정수 1~1,000,000원
        if (req.price() < 1 || req.price() > 1_000_000) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        // 설명은 앞뒤 공백 제거 후 300자 이하
        if (req.description().trim().length() > 300) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }
}