package com.kdt.cafesystem.menu.repository;

import com.kdt.cafesystem.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepo extends JpaRepository<Menu,Long> {

    boolean existsByCategoryId(Long id);

    List<Menu> findAllByOrderByIdAsc();

    List<Menu> findByCategoryIdOrderByIdAsc(Long categoryId);

    List<Menu> findByAvailableTrueOrderByIdAsc();

    List<Menu> findByNameContainingOrderByIdAsc(String keyword);
}
