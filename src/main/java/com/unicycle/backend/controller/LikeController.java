package com.unicycle.backend.controller;

import com.unicycle.backend.model.ProductLike;
import com.unicycle.backend.repository.ProductLikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map; // 🚀 YENİ: Veriyi hatasız almak için Map kullanıyoruz

@RestController
@RequestMapping("/api/interaction/likes")
@CrossOrigin(origins = "*")
public class LikeController {

    @Autowired
    private ProductLikeRepository likeRepository;

    // 1. SAYACI ÇALIŞTIR (GET)
    @GetMapping("/count/{productId}")
    public ResponseEntity<Long> getLikeCount(@PathVariable Long productId) {
        long count = likeRepository.countByProductId(productId);
        return ResponseEntity.ok(count);
    }

    // 🚀 2. BEĞENİYİ VERİTABANINA YAZ (GÜNCELLENDİ: Map Kullanımı)
    @PostMapping
    public ResponseEntity<String> addLike(@RequestBody Map<String, Long> payload) {
        // React'tan gelen JSON verisini doğrudan Map ile çekiyoruz. (Çeviri hatası ihtimali SIFIR)
        Long userId = payload.get("userId");
        Long productId = payload.get("productId");

        // Güvenlik: Eğer veri boş gelmişse Java çökmesin diye uyarı versin
        if (userId == null || productId == null) {
            return ResponseEntity.badRequest().body("Eksik veri geldi!");
        }

        // Eğer daha önce beğenmemişse kaydet
        if (!likeRepository.existsByUserIdAndProductId(userId, productId)) {
            ProductLike like = new ProductLike();
            like.setUserId(userId);
            like.setProductId(productId);
            likeRepository.save(like);
        }
        return ResponseEntity.ok("Beğeni başarıyla kaydedildi.");
    }

    // 3. BEĞENİYİ VERİTABANINDAN SİL (DELETE)
    @Transactional
    @DeleteMapping
    public ResponseEntity<String> removeLike(@RequestParam Long userId, @RequestParam Long productId) {
        likeRepository.deleteByUserIdAndProductId(userId, productId);
        return ResponseEntity.ok("Beğeni başarıyla silindi.");
    }
}