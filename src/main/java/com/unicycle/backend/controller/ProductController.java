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

@RestController
@RequestMapping("/api/products")
// VERCEL VIP BİLETİ ZATEN BURADAYDI!
@CrossOrigin(origins = "https://uni-cycle-seven.vercel.app")
public class ProductController {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductController(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // CREATE PRODUCT
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

            Product newProduct = new Product();
            newProduct.setUser(owner);
            newProduct.setTitle(request.getTitle());
            newProduct.setCategory(request.getCategory());
            newProduct.setItemCondition(request.getItemCondition());
            newProduct.setPriceType(request.getPriceType());
            newProduct.setPrice(request.getPrice());
            newProduct.setDescription(request.getDescription());
            newProduct.setPhotosBase64(request.getPhotosBase64());

            Product savedProduct = productRepository.save(newProduct);
            return ResponseEntity.ok(savedProduct);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("System Error: " + e.getMessage());
        }
    }

    // GET ALL PRODUCTS
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return ResponseEntity.ok(products);
    }

    // 🔍 NEW: SEARCH PRODUCTS ENDPOINT
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam("q") String query) {
        try {
            // Searches both titles and categories
            List<Product> results = productRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(query, query);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // DELETE PRODUCT
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
}