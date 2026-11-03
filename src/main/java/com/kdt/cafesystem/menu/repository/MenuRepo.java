package com.kdt.cafesystem.menu.repository;

import com.kdt.cafesystem.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepo extends JpaRepository<Menu,Long> {

    List<Menu> findByCategoryId(Long categoryId);       // 카테고리별 메뉴 목록
    List<Menu> findByAvailableTrue();                   //판매 가능 여부

    boolean existsByCategoryId(Long id);
}
