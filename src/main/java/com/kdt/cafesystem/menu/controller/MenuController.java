package com.kdt.cafesystem.menu.controller;

import com.kdt.cafesystem.menu.dto.MenuAvailabilityRequestDto;
import com.kdt.cafesystem.menu.dto.MenuPageResponseDto;
import com.kdt.cafesystem.menu.dto.MenuRequestDto;
import com.kdt.cafesystem.menu.dto.MenuResponseDto;
import com.kdt.cafesystem.menu.service.MenuService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    //A06 전체 메뉴 목록 ->200
    @GetMapping
    public List<MenuResponseDto> findAll() {
        return menuService.findAll();
    }

    //A07 메뉴 상세 -> 200
    @GetMapping("/{menuId}")
    public MenuResponseDto find(@PathVariable Long menuId) {
        return menuService.find(menuId);
    }

    // 심화 별도 메뉴 페이징 API
    @GetMapping("/page")
    public MenuPageResponseDto findPage(Pageable pageable) {
        return menuService.list(pageable);
    }

    //A08 메뉴 등록 -> 201
    @PostMapping
    public ResponseEntity<MenuResponseDto> create(@Valid @RequestBody MenuRequestDto menuRequestDto) {
        MenuResponseDto response = menuService.create(menuRequestDto);
        URI location = URI.create("/api/v1/menus/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    //A09 메뉴 일반 수정 -> 200
    @PutMapping("/{menuId}")
    public ResponseEntity<MenuResponseDto> update(@PathVariable Long menuId,
                                                  @Valid @RequestBody MenuRequestDto menuRequestDto) {
        return ResponseEntity.ok(menuService.update(menuId, menuRequestDto));
    }

    //A10 메뉴 삭제 -> 204
    @DeleteMapping("/{menuId}")
    public ResponseEntity<Void> delete(@PathVariable Long menuId) {
        menuService.delete(menuId);
        return ResponseEntity.noContent().build();
    }

    //A19 판매 중 메뉴 -> 200
    @GetMapping("/available")
    public ResponseEntity<List<MenuResponseDto>> findAvailable() {
        return ResponseEntity.ok(menuService.findAvailable());
    }

    //A20 메뉴명 검색 -> 200
    @GetMapping("/search")
    public ResponseEntity<List<MenuResponseDto>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(menuService.search(keyword));
    }

    //A21 판매 여부 변경 -> 200
    @PutMapping("/{menuId}/availability")
    public ResponseEntity<MenuResponseDto> changeAvailability(
            @PathVariable Long menuId,
            @Valid @RequestBody MenuAvailabilityRequestDto requestDto) {
        return ResponseEntity.ok(menuService.changeAvailability(menuId, requestDto));
    }

}
