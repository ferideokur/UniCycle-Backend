package com.unicycle.backend.controller;

import com.unicycle.backend.dto.ProductRequestDTO;
import com.unicycle.backend.model.Product;
import com.unicycle.backend.model.User;
import com.unicycle.backend.repository.ProductRepository;
import com.unicycle.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * UniCycle İlan Yönetim Merkezi
 * Frontend (Next.js) ile Backend (Java) arasındaki köprü.
 */
@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:3000")
public class ProductController {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductController(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    /**
     * 🚀 YENİ İLAN OLUŞTURMA (POST)
     * Next.js'den gelen ilanı alır, kullanıcıyı kontrol eder ve DB'ye yazar.
     */
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody ProductRequestDTO request) {
        try {
            // 1. GÜVENLİK KONTROLÜ: Gelen pakette kullanıcı ID'si var mı?
            if (request.getUserId() == null) {
                return ResponseEntity.badRequest().body("Hata: İlanı kimin verdiği (userId) belli değil!");
            }

            // 2. KULLANICIYI BUL: Veritabanında bu ID ile biri kayıtlı mı?
            Optional<User> optionalUser = userRepository.findById(request.getUserId());
            if (optionalUser.isEmpty()) {
                return ResponseEntity.badRequest().body("Hata: ID'si " + request.getUserId() + " olan bir kullanıcı veritabanında bulunamadı!");
            }
            User owner = optionalUser.get();

            // 3. MODELİ DOLDUR: DTO'dan (kargo kutusu) gerçek Product nesnesine aktar
            Product newProduct = new Product();
            newProduct.setUser(owner);
            newProduct.setTitle(request.getTitle());
            newProduct.setCategory(request.getCategory());
            newProduct.setItemCondition(request.getItemCondition());
            newProduct.setPriceType(request.getPriceType());
            newProduct.setPrice(request.getPrice());
            newProduct.setDescription(request.getDescription());
            newProduct.setPhotosBase64(request.getPhotosBase64());

            // 4. KAYDET VE CEVAP VER:
            Product savedProduct = productRepository.save(newProduct);
            return ResponseEntity.ok(savedProduct);

        } catch (Exception e) {
            // 💥 KRİTİK HATA YAKALAYICI: Java çökerse sebebi terminale yazılır.
            e.printStackTrace();
            return ResponseEntity.status(500).body("Sistemsel Hata: " + e.getMessage());
        }
    }

    /**
     * 📋 TÜM İLANLARI LİSTELE (GET)
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return ResponseEntity.ok(products);
    }

    /**
     * 🗑️ İLAN SİLME KAPISI (DELETE)
     * Frontend'den gelen ID'ye göre ürünü veritabanından kalıcı olarak siler.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            // Önce böyle bir ürün var mı diye kontrol et
            if (!productRepository.existsById(id)) {
                return ResponseEntity.badRequest().body("Hata: Silinmek istenen ürün bulunamadı!");
            }

            // Varsa acımadan sil!
            productRepository.deleteById(id);
            return ResponseEntity.ok("{\"message\": \"Ürün başarıyla silindi!\"}");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Silme işlemi sırasında sunucu hatası: " + e.getMessage());
        }
    }
}