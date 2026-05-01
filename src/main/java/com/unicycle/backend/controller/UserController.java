package com.unicycle.backend.controller;

import com.unicycle.backend.model.User;
import com.unicycle.backend.service.UserService;
import com.unicycle.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "https://uni-cycle-seven.vercel.app")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserController(UserService userService, UserRepository userRepository, JdbcTemplate jdbcTemplate) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    // REGISTER
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User savedUser = userService.registerUser(user);
            return ResponseEntity.ok("Success: " + savedUser.getEmail());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginData) {
        try {
            String email = loginData.get("email");
            String password = loginData.get("password");

            User user = userService.loginUser(email, password);

            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("fullName", user.getFullName());
            userData.put("email", user.getEmail());
            userData.put("lastActive", user.getLastActive());
            userData.put("university", user.getUniversity());
            // 🚀 YENİ: Login olurken artık profil verilerini de gönderiyoruz
            userData.put("bio", user.getBio());
            userData.put("profileImage", user.getProfileImage());
            userData.put("coverImage", user.getCoverImage());
            userData.put("coverY", user.getCoverY());
            userData.put("message", "Login Successful");

            return ResponseEntity.ok(userData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Login failed: " + e.getMessage());
        }
    }

    // 🔍 SEARCH
    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam("q") String query) {
        try {
            List<User> users = userRepository.findByFullNameContainingIgnoreCase(query);

            List<Map<String, Object>> response = users.stream().map(u -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", u.getId());
                map.put("fullName", u.getFullName());
                map.put("email", u.getEmail());
                map.put("lastActive", u.getLastActive());
                map.put("university", u.getUniversity());
                // Arama sonuçlarında fotoğraf da çıksın diye ekliyoruz:
                map.put("profileImage", u.getProfileImage());
                return map;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Search error");
        }
    }

    // BAŞKASININ (VEYA KENDİ) PROFİLİNİ GÖRÜNTÜLEME
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserProfile(@PathVariable Long id) {
        try {
            return userRepository.findById(id)
                    .map(user -> {
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("id", user.getId());
                        userData.put("fullName", user.getFullName());
                        userData.put("email", user.getEmail());
                        userData.put("lastActive", user.getLastActive());
                        userData.put("university", user.getUniversity());
                        // 🚀 YENİ: Profil sayfasına girildiğinde resim ve bio gelsin
                        userData.put("bio", user.getBio());
                        userData.put("profileImage", user.getProfileImage());
                        userData.put("coverImage", user.getCoverImage());
                        userData.put("coverY", user.getCoverY());
                        return ResponseEntity.ok(userData);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Kullanıcı getirilemedi.");
        }
    }

    // 📝 KAPSAMLI PROFİL GÜNCELLEME MOTORU (TÜM VERİLER İÇİN)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUserProfile(@PathVariable Long id, @RequestBody Map<String, Object> updateData) {
        try {
            return userRepository.findById(id).map(user -> {
                // Hangi veri gönderildiyse sadece onu günceller
                if (updateData.containsKey("bio")) {
                    user.setBio((String) updateData.get("bio"));
                }
                if (updateData.containsKey("profileImage")) {
                    user.setProfileImage((String) updateData.get("profileImage"));
                }
                if (updateData.containsKey("coverImage")) {
                    user.setCoverImage((String) updateData.get("coverImage"));
                }
                if (updateData.containsKey("coverY")) {
                    Object coverYObj = updateData.get("coverY");
                    if (coverYObj != null) {
                        user.setCoverY(Integer.parseInt(coverYObj.toString()));
                    } else {
                        user.setCoverY(50); // Boş gelirse varsayılan 50 (ortada) olsun
                    }
                }
                if (updateData.containsKey("university")) {
                    user.setUniversity((String) updateData.get("university"));
                }

                userRepository.save(user);
                return ResponseEntity.ok("Profil başarıyla güncellendi.");
            }).orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Güncelleme hatası: " + e.getMessage());
        }
    }

    // 🚨 HESAP SİLME MOTORU (TEHLİKELİ BÖLGE)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            if (userRepository.existsById(id)) {
                userRepository.deleteById(id);
                return ResponseEntity.ok("Kullanıcı ve ona ait tüm veriler başarıyla silindi.");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Silme işlemi sırasında hata oluştu: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/ping")
    public ResponseEntity<?> pingUser(@PathVariable Long id) {
        return userRepository.findById(id).map(user -> {
            user.setLastActive(LocalDateTime.now());
            userRepository.save(user);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/logout")
    public ResponseEntity<?> logoutUser(@PathVariable Long id) {
        return userRepository.findById(id).map(user -> {
            user.setLastActive(LocalDateTime.now().minusMinutes(10));
            userRepository.save(user);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    // 🚨 SİHİRLİ VERİTABANI DÜZELTME BUTONU
    @GetMapping("/fix-db")
    public ResponseEntity<?> fixDatabase() {
        try {
            jdbcTemplate.execute("UPDATE users SET university = 'Piri Reis Üniversitesi' WHERE university IS NULL");
            jdbcTemplate.execute("UPDATE products SET university = 'Piri Reis Üniversitesi' WHERE university IS NULL");
            return ResponseEntity.ok("✅ BÜYÜK BAŞARI! Bütün eski ilanlar ve hesabi olanlar Piri Reis Üniversitesi olarak güncellendi. Arayüze dönüp kontrol edebilirsin!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Hata oluştu: " + e.getMessage());
        }
    }
}