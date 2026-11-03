package com.kdt.cafesystem.category.repository;

import com.kdt.cafesystem.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepo extends JpaRepository<Category,Long> {

}
