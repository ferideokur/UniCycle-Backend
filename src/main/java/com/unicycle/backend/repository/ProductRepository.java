package com.unicycle.backend.repository;

import com.unicycle.backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // 🚀 Custom SQL Query generation by Spring Data JPA
    // Searches for products where title OR category contains the query (case-insensitive)
    List<Product> findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(String title, String category);
}