package com.unicycle.backend.repository;

import com.unicycle.backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // 🚀 Arama Çubuğu İçin: Başlık veya Kategoriye göre arama
    List<Product> findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(String title, String category);

    // 🌟 YENİ: Sadece Belirli Bir Üniversitenin İlanlarını Getirmek İçin
    List<Product> findByUniversityIgnoreCase(String university);
}