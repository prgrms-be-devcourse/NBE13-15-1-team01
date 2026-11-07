package com.kdt.cafesystem.menu.repository;

import com.kdt.cafesystem.menu.entity.Menu;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepo extends JpaRepository<Menu, Long> {

    boolean existsByCategoryId(Long categoryId);

    List<Menu> findAllByOrderByIdAsc();

    List<Menu> findByCategoryIdOrderByIdAsc(Long categoryId);

    List<Menu> findByAvailableTrueOrderByIdAsc();

    List<Menu> findByNameContainingOrderByIdAsc(String keyword);
}
