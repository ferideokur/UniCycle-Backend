package com.unicycle.backend.controller;

import com.unicycle.backend.model.User;
import com.unicycle.backend.service.UserService;
import com.unicycle.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

// 🚀 GMAIL İÇİN GEREKLİ KÜTÜPHANELER GERİ GELDİ
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"https://uni-cycle-seven.vercel.app", "http://localhost:3000"})
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender; // 🚀 GMAIL SİSTEMİ EKLENDİ

    @Autowired
    public UserController(UserService userService, UserRepository userRepository, JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder, JavaMailSender mailSender) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender; // 🚀 BAŞLATILDI
    }

    // 1️⃣ KULLANICI KAYDI
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            user.setStatus("PENDING");
            user.setRole("USER");
            User savedUser = userService.registerUser(user);

            savedUser.setStatus("PENDING");
            savedUser.setRole("USER");
            userRepository.save(savedUser);

            return ResponseEntity.ok("Success: " + savedUser.getEmail());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }

    // 2️⃣ GİRİŞ YAP (LOGIN)
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginData) {
        try {
            String email = loginData.get("email");
            String password = loginData.get("password");

            User user = userService.loginUser(email, password);

            if ("SUSPENDED".equals(user.getStatus())) {
                return ResponseEntity.status(403).body("Hesabınız yönetici tarafından askıya alınmıştır. Lütfen iletişime geçin.");
            }

            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("fullName", user.getFullName());
            userData.put("email", user.getEmail());
            userData.put("lastActive", user.getLastActive());
            userData.put("university", user.getUniversity());
            userData.put("bio", user.getBio());
            userData.put("profileImage", user.getProfileImage());
            userData.put("coverImage", user.getCoverImage());
            userData.put("coverY", user.getCoverY());
            userData.put("role", user.getRole());
            userData.put("status", user.getStatus());
            userData.put("message", "Login Successful");

            return ResponseEntity.ok(userData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Giriş başarısız: " + e.getMessage());
        }
    }

    // 🚀🚀 ŞİFREMİ UNUTTUM: KUSURSUZ GMAIL GÖNDERİMİ 🚀🚀
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam("email") String email) {
        try {
            Optional<User> userOptional = userRepository.findByEmail(email);

            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Kullanıcı bulunamadı");
            }

            User user = userOptional.get();

            // 6 haneli kod
            String otp = String.format("%06d", new java.util.Random().nextInt(999999));
            user.setOtpCode(otp);
            userRepository.save(user);

            // 🚀 GOOGLE (GMAIL) ÜZERİNDEN MAİLİ ANINDA UÇURUYORUZ
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("unicycledestek@gmail.com");
            message.setTo(email);
            message.setSubject("UniCycle - Şifre Sıfırlama Kodu");
            message.setText("Merhaba " + user.getFullName() + ",\n\n"
                    + "Hesabınızın şifresini sıfırlamak için gereken doğrulama kodunuz:\n\n"
                    + "👉 " + otp + " 👈\n\n"
                    + "Bu kodu kimseyle paylaşmayın. Eğer bu işlemi siz yapmadıysanız bu mesajı görmezden gelebilirsiniz.\n\n"
                    + "Sevgiler,\nUniCycle Destek Ekibi");

            mailSender.send(message);
            System.out.println("✅ Gmail ile kod başarıyla gönderildi: " + email);

            return ResponseEntity.ok("Kod başarıyla oluşturuldu.");
        } catch (Exception e) {
            System.out.println("❌ Mail gönderim hatası: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("İşlem hatası: " + e.getMessage());
        }
    }

    // 🚀🚀 ŞİFREMİ UNUTTUM AŞAMA 2: KODU DOĞRULAYIP YENİ ŞİFREYİ KAYDETME
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> payload) {
        try {
            String email = payload.get("email");
            String otpCode = payload.get("otpCode");
            String newPassword = payload.get("newPassword");

            Optional<User> userOptional = userRepository.findByEmail(email);

            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Kullanıcı bulunamadı");
            }

            User user = userOptional.get();

            if (user.getOtpCode() != null && user.getOtpCode().equals(otpCode)) {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setOtpCode(null);
                userRepository.save(user);

                return ResponseEntity.ok("Şifre başarıyla güncellendi.");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Geçersiz doğrulama kodu.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("İşlem sırasında hata oluştu: " + e.getMessage());
        }
    }

    // 3️⃣ ARAMA MOTORU (SEARCH)
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
                map.put("profileImage", u.getProfileImage());
                return map;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Search error");
        }
    }

    // 4️⃣ PROFİL GÖRÜNTÜLEME
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserProfile(@PathVariable Long id) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setPassword(null);
                user.setOtpCode(null);
                return ResponseEntity.ok(user);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Kullanıcı getirilemedi.");
        }
    }

    // 5️⃣ PROFİL GÜNCELLEME
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUserProfile(@PathVariable Long id, @RequestBody Map<String, Object> updateData) {
        try {
            return userRepository.findById(id).map(user -> {
                if (updateData.containsKey("bio")) user.setBio((String) updateData.get("bio"));
                if (updateData.containsKey("profileImage")) user.setProfileImage((String) updateData.get("profileImage"));
                if (updateData.containsKey("coverImage")) user.setCoverImage((String) updateData.get("coverImage"));
                if (updateData.containsKey("university")) user.setUniversity((String) updateData.get("university"));

                if (updateData.containsKey("coverY")) {
                    Object coverYObj = updateData.get("coverY");
                    user.setCoverY(coverYObj != null ? Integer.parseInt(coverYObj.toString()) : 50);
                }

                userRepository.save(user);
                return ResponseEntity.ok("Profil başarıyla güncellendi.");
            }).orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Güncelleme hatası: " + e.getMessage());
        }
    }

    // 6️⃣ SON GÖRÜLME VE ÇIKIŞ
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

    // ==========================================
    // 👑 ADMIN PANELI (GOD MODE) BÖLÜMÜ
    // ==========================================

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getUsersByStatus(@PathVariable String status) {
        try {
            List<User> users = userRepository.findByStatus(status.toUpperCase());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Kullanıcılar çekilirken hata oluştu: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveUser(@PathVariable Long id) {
        try {
            return userRepository.findById(id).map(user -> {
                user.setStatus("ACTIVE");
                userRepository.save(user);
                return ResponseEntity.ok("Kullanıcı başarıyla aktif edildi.");
            }).orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("İşlem sırasında hata oluştu: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/suspend")
    public ResponseEntity<?> suspendUser(@PathVariable Long id) {
        try {
            return userRepository.findById(id).map(user -> {
                user.setStatus("SUSPENDED");
                userRepository.save(user);
                return ResponseEntity.ok("Kullanıcı başarıyla yasaklandı/pasife alındı.");
            }).orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("İşlem sırasında hata oluştu: " + e.getMessage());
        }
    }

    // 🚀 D) KÖKTEN SİLME
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            if (userRepository.existsById(id)) {

                jdbcTemplate.update("DELETE FROM notifications WHERE user_id = ?", id);
                jdbcTemplate.update("DELETE FROM follows WHERE follower_id = ? OR following_id = ?", id, id);
                jdbcTemplate.update("DELETE FROM comment WHERE user_id = ?", id);
                jdbcTemplate.update("DELETE FROM products WHERE user_id = ?", id);
                jdbcTemplate.update("DELETE FROM messages WHERE sender_id = ? OR receiver_id = ?", id, id);

                userRepository.deleteById(id);

                return ResponseEntity.ok("Kullanıcı, tüm bildirimleri ve verileriyle birlikte kökten silindi. 💥");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Silme işlemi sırasında hata oluştu: " + e.getMessage());
        }
    }

    // 🚀 BÜYÜK KURTARICI METOT 🚀
    @GetMapping("/fix-db")
    public ResponseEntity<?> fixDatabase() {
        try {
            try {
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN document_base64 TEXT;");
            } catch (Exception ignored) {
            }

            jdbcTemplate.execute("UPDATE users SET status = 'ACTIVE' WHERE status IS NULL");
            jdbcTemplate.execute("UPDATE users SET role = 'USER' WHERE role IS NULL");
            return ResponseEntity.ok("✅ VERİTABANI ONARILDI, EKSİK KOLON EKLENDİ VE KİLİT AÇILDI!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Hata oluştu: " + e.getMessage());
        }
    }
}