package com.kdt.cafesystem.menu.service;

import com.kdt.cafesystem.category.entity.Category;
import com.kdt.cafesystem.category.repository.CategoryRepo;
import com.kdt.cafesystem.global.exception.BusinessException;
import com.kdt.cafesystem.global.exception.ErrorCode;
import com.kdt.cafesystem.menu.dto.MenuAvailabilityRequestDto;
import com.kdt.cafesystem.menu.dto.MenuPageResponseDto;
import com.kdt.cafesystem.menu.dto.MenuRequestDto;
import com.kdt.cafesystem.menu.dto.MenuResponseDto;
import com.kdt.cafesystem.menu.entity.Menu;
import com.kdt.cafesystem.menu.repository.MenuRepo;
import com.kdt.cafesystem.order.repository.OrderItemRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MenuService {

    private final MenuRepo menuRepo;
    private final CategoryRepo categoryRepo;
    private final OrderItemRepository orderItemRepository;
    private final AvailableMenuCache availableMenuCache;

    /** A06 전체 메뉴 목록 */
    @Transactional(readOnly = true)
    public List<MenuResponseDto> findAll() {
        return toDtoList(menuRepo.findAllByOrderByIdAsc());
    }

    /** A07 메뉴 상세 */
    @Transactional(readOnly = true)
    public MenuResponseDto find(Long id) {
        return toDto(getMenu(id));
    }

    /** A15 카테고리별 메뉴 */
    @Transactional(readOnly = true)
    public List<MenuResponseDto> findByCategory(Long categoryId) {
        getCategory(categoryId);
        return toDtoList(menuRepo.findByCategoryIdOrderByIdAsc(categoryId));
    }

    /** A08 메뉴 등록 */
    public MenuResponseDto create(MenuRequestDto request) {
        validate(request);
        Category category = getCategory(request.categoryId());

        Menu menu = new Menu(request.name().trim(), request.price(), request.description(), category);
        MenuResponseDto saved = toDto(menuRepo.save(menu));
        availableMenuCache.evict();
        return saved;
    }

    /** A09 메뉴 일반 수정 */
    public MenuResponseDto update(Long id, MenuRequestDto request) {
        validate(request);
        Menu menu = getMenu(id);
        Category category = getCategory(request.categoryId());

        menu.setCategory(category);
        menu.setName(request.name().trim());
        menu.setPrice(request.price());
        menu.setDescription(request.description());
        availableMenuCache.evict();
        return toDto(menu);
    }

    /** A10 메뉴 삭제: 주문에 사용된 메뉴는 409 */
    public void delete(Long id) {
        Menu menu = getMenu(id);
        if (orderItemRepository.existsByMenu_Id(id)) {
            throw new BusinessException(ErrorCode.MENU_IN_USE);
        }
        menuRepo.delete(menu);
        availableMenuCache.evict();
    }

    /** A19 판매 중 메뉴: Redis 캐시가 있으면 DB를 조회하지 않는다 */
    @Transactional(readOnly = true)
    public List<MenuResponseDto> findAvailable() {
        Optional<List<MenuResponseDto>> cached = availableMenuCache.get();
        if (cached.isPresent()) {
            return cached.get();
        }
        List<MenuResponseDto> menus = toDtoList(menuRepo.findByAvailableTrueOrderByIdAsc());
        availableMenuCache.put(menus);
        return menus;
    }

    /** A20 메뉴명 검색: 결과가 없으면 [] */
    @Transactional(readOnly = true)
    public List<MenuResponseDto> search(String keyword) {
        if (keyword == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        // 검색어는 앞뒤 공백 제거 후 1~100자
        String trimmedKeyword = keyword.trim();
        if (trimmedKeyword.isEmpty() || trimmedKeyword.length() > 100) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        return toDtoList(menuRepo.findByNameContainingOrderByIdAsc(trimmedKeyword));
    }

    /** 선택 심화: 별도 메뉴 페이징. 정렬은 id 오름차순으로 고정 */
    @Transactional(readOnly = true)
    public MenuPageResponseDto list(Pageable pageable) {
        if (pageable.getPageNumber() < 0 || pageable.getPageSize() < 1 || pageable.getPageSize() > 50) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }

        Pageable fixed = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("id"));
        Page<Menu> found = menuRepo.findAll(fixed);
        return new MenuPageResponseDto(
                toDtoList(found.getContent()),
                found.getNumber(),
                found.getSize(),
                found.getTotalElements(),
                found.getTotalPages());
    }

    /** A21 판매 여부 변경: available만 바꾼다 */
    public MenuResponseDto changeAvailability(Long id, MenuAvailabilityRequestDto request) {
        Menu menu = getMenu(id);
        menu.setAvailable(request.available());
        availableMenuCache.evict();
        return toDto(menu);
    }

    private Menu getMenu(Long id) {
        return menuRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));
    }

    private Category getCategory(Long categoryId) {
        return categoryRepo.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private List<MenuResponseDto> toDtoList(List<Menu> menus) {
        return menus.stream()
                .map(this::toDto)
                .toList();
    }

    private MenuResponseDto toDto(Menu menu) {
        return new MenuResponseDto(menu.getId(), menu.getCategory().getId(),
                menu.getName(), menu.getPrice(), menu.getDescription(), menu.isAvailable());
    }

    // 메뉴명 1~100자, 가격 1~1,000,000원, 설명 300자 이하 (모두 앞뒤 공백 제거 후)
    private void validate(MenuRequestDto request) {
        if (request.categoryId() == null || request.name() == null
                || request.price() == null || request.description() == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        String name = request.name().trim();
        if (name.isEmpty() || name.length() > 100) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        if (request.price() < 1 || request.price() > 1_000_000) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        if (request.description().trim().length() > 300) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }
}
