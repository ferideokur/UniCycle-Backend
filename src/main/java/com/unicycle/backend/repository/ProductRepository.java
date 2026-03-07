package com.unicycle.backend.repository;

import com.unicycle.backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Product entity.
 * Spring Data JPA will automatically generate the implementation for standard CRUD operations.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Spring Boot, tüm kaydetme, silme ve listeleme işlemlerini
    // bizim için otomatik olarak arka planda halledecek!
}