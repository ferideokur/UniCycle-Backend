package com.unicycle.backend.repository;

import com.unicycle.backend.model.ProductLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductLikeRepository extends JpaRepository<ProductLike, Long> {

    // 1. Bir ilanın toplam kaç kez beğenildiğini SQL ile sayar (SELECT COUNT(*)...)
    long countByProductId(Long productId);

    // 2. Bir kullanıcı o ilanı zaten beğenmiş mi diye kontrol eder
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    // 3. Kullanıcı takipten çıkınca o beğeniyi veritabanından siler
    void deleteByUserIdAndProductId(Long userId, Long productId);
}
