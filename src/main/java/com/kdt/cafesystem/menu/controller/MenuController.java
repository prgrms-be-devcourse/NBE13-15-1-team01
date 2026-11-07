package com.kdt.cafesystem.menu.controller;

import com.kdt.cafesystem.menu.dto.MenuAvailabilityRequestDto;
import com.kdt.cafesystem.menu.dto.MenuPageResponseDto;
import com.kdt.cafesystem.menu.dto.MenuRequestDto;
import com.kdt.cafesystem.menu.dto.MenuResponseDto;
import com.kdt.cafesystem.menu.service.MenuService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    /** A06 전체 메뉴 목록 */
    @GetMapping
    public List<MenuResponseDto> findAll() {
        return menuService.findAll();
    }

    /** A07 메뉴 상세 */
    @GetMapping("/{menuId}")
    public MenuResponseDto find(@PathVariable Long menuId) {
        return menuService.find(menuId);
    }

    /** 선택 심화: 별도 메뉴 페이징  GET /api/v1/menus/page?page=0&size=10 */
    @GetMapping("/page")
    public MenuPageResponseDto findPage(Pageable pageable) {
        return menuService.list(pageable);
    }

    /** A08 메뉴 등록 → 201 + Location */
    @PostMapping
    public ResponseEntity<MenuResponseDto> create(@Valid @RequestBody MenuRequestDto request) {
        MenuResponseDto response = menuService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/menus/" + response.id())).body(response);
    }

    /** A09 메뉴 일반 수정 */
    @PutMapping("/{menuId}")
    public MenuResponseDto update(@PathVariable Long menuId,
                                  @Valid @RequestBody MenuRequestDto request) {
        return menuService.update(menuId, request);
    }

    /** A10 메뉴 삭제 → 204, 빈 본문 */
    @DeleteMapping("/{menuId}")
    public ResponseEntity<Void> delete(@PathVariable Long menuId) {
        menuService.delete(menuId);
        return ResponseEntity.noContent().build();
    }

    /** A19 판매 중 메뉴 */
    @GetMapping("/available")
    public List<MenuResponseDto> findAvailable() {
        return menuService.findAvailable();
    }

    /** A20 메뉴명 검색  GET /api/v1/menus/search?keyword=... */
    @GetMapping("/search")
    public List<MenuResponseDto> search(@RequestParam String keyword) {
        return menuService.search(keyword);
    }

    /** A21 판매 여부 변경 */
    @PutMapping("/{menuId}/availability")
    public MenuResponseDto changeAvailability(@PathVariable Long menuId,
                                              @Valid @RequestBody MenuAvailabilityRequestDto request) {
        return menuService.changeAvailability(menuId, request);
    }
}
