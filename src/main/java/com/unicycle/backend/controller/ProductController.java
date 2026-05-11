package com.unicycle.backend.controller;

import com.unicycle.backend.dto.ProductRequestDTO;
import com.unicycle.backend.model.Product;
import com.unicycle.backend.model.User;
import com.unicycle.backend.repository.ProductRepository;
import com.unicycle.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = {"https://uni-cycle-seven.vercel.app", "http://localhost:3000"})
public class ProductController {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductController(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // 1️⃣ CREATE PRODUCT (İLAN OLUŞTURMA)
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody ProductRequestDTO request) {
        try {
            if (request.getUserId() == null) {
                return ResponseEntity.badRequest().body("Error: Missing userId!");
            }

            Optional<User> optionalUser = userRepository.findById(request.getUserId());
            if (optionalUser.isEmpty()) {
                return ResponseEntity.badRequest().body("Error: User not found!");
            }
            User owner = optionalUser.get();

            // 🚀 GÜVENLİK DUVARI: YASAKLI VEYA ONAYSIZ KİŞİ İLAN VEREMEZ! 🚀
            if (!"ACTIVE".equals(owner.getStatus())) {
                return ResponseEntity.status(403).body("İlan verebilmek için hesabınızın yöneticiler tarafından onaylanmış ve aktif olması gerekmektedir.");
            }

            Product newProduct = new Product();
            newProduct.setUser(owner);
            newProduct.setTitle(request.getTitle());
            newProduct.setCategory(request.getCategory());
            newProduct.setItemCondition(request.getItemCondition());
            newProduct.setPriceType(request.getPriceType());
            newProduct.setPrice(request.getPrice());
            newProduct.setDescription(request.getDescription());
            newProduct.setPhotosBase64(request.getPhotosBase64());
            newProduct.setUniversity(request.getUniversity());

            Product savedProduct = productRepository.save(newProduct);
            return ResponseEntity.ok(mapProductToSafeObject(savedProduct));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("System Error: " + e.getMessage());
        }
    }

    // 2️⃣ GET ALL PRODUCTS
    @GetMapping
    public ResponseEntity<?> getProducts(@RequestParam(value = "university", required = false) String university) {
        try {
            List<Product> products;

            if (university != null && !university.trim().isEmpty()) {
                products = productRepository.findByUniversityIgnoreCase(university.trim());
            } else {
                products = productRepository.findAll();
            }

            List<Map<String, Object>> safeProducts = new ArrayList<>();
            for (Product p : products) {
                safeProducts.add(mapProductToSafeObject(p));
            }

            return ResponseEntity.ok(safeProducts);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Backend Hatası: İlanlar çekilirken Java çöktü -> " + e.getMessage());
        }
    }

    // 3️⃣ SEARCH PRODUCTS (ARAMA)
    @GetMapping("/search")
    public ResponseEntity<?> searchProducts(@RequestParam("q") String query) {
        try {
            List<Product> results = productRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(query, query);

            List<Map<String, Object>> safeProducts = new ArrayList<>();
            for (Product p : results) {
                safeProducts.add(mapProductToSafeObject(p));
            }

            return ResponseEntity.ok(safeProducts);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Backend Hatası: Arama yaparken çöktü -> " + e.getMessage());
        }
    }

    // 4️⃣ DELETE PRODUCT (SİLME)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            if (!productRepository.existsById(id)) {
                return ResponseEntity.badRequest().body("Error: Product not found!");
            }
            productRepository.deleteById(id);
            return ResponseEntity.ok("{\"message\": \"Deleted successfully!\"}");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Server Error: " + e.getMessage());
        }
    }

    private Map<String, Object> mapProductToSafeObject(Product p) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", p.getId());
        map.put("title", p.getTitle());
        map.put("category", p.getCategory());
        map.put("itemCondition", p.getItemCondition());
        map.put("priceType", p.getPriceType());
        map.put("price", p.getPrice());
        map.put("description", p.getDescription());
        map.put("photosBase64", p.getPhotosBase64());
        map.put("university", p.getUniversity());

        if (p.getUser() != null) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", p.getUser().getId());
            userMap.put("fullName", p.getUser().getFullName());
            userMap.put("university", p.getUser().getUniversity());
            map.put("user", userMap);
        }
        return map;
    }
}