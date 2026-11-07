package com.kdt.cafesystem.category.repository;

import com.kdt.cafesystem.category.entity.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepo extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    List<Category> findAllByOrderByIdAsc();
}
