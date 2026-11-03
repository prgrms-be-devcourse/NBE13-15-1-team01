package com.kdt.cafesystem.category.repository;

import com.kdt.cafesystem.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepo extends JpaRepository<Category,Long> {


    Optional<Category> findByName(String name);

    List<Category> findAllByOrderByIdAsc();
}
