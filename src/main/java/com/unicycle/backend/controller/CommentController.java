package com.unicycle.backend.controller;

import com.unicycle.backend.model.Comment;
import com.unicycle.backend.model.Product;
import com.unicycle.backend.model.User;
import com.unicycle.backend.repository.CommentRepository;
import com.unicycle.backend.repository.ProductRepository;
import com.unicycle.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "https://uni-cycle-seven.vercel.app")
public class CommentController {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CommentController(CommentRepository commentRepository, UserRepository userRepository, ProductRepository productRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long productId) {
        return ResponseEntity.ok(commentRepository.findByProductIdOrderByCreatedAtDesc(productId));
    }

    @PostMapping
    public ResponseEntity<?> addComment(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.valueOf(payload.get("userId").toString());
            Long productId = Long.valueOf(payload.get("productId").toString());
            String text = payload.get("text").toString();

            Optional<User> userOpt = userRepository.findById(userId);
            Optional<Product> prodOpt = productRepository.findById(productId);

            if (userOpt.isEmpty() || prodOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Kullanıcı veya ilan bulunamadı.");
            }

            Comment comment = new Comment();
            comment.setUser(userOpt.get());
            comment.setProduct(prodOpt.get());
            comment.setText(text);

            return ResponseEntity.ok(commentRepository.save(comment));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Hata: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable Long id) {
        if (commentRepository.existsById(id)) {
            commentRepository.deleteById(id);
            return ResponseEntity.ok("Silindi");
        }
        return ResponseEntity.badRequest().body("Yorum bulunamadı.");
    }
}