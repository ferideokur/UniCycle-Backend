package com.unicycle.backend.controller;

import com.unicycle.backend.model.User;
import com.unicycle.backend.service.UserService;
import com.unicycle.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder; // 🚀 ŞİFRELEYİCİ İMPORT EDİLDİ

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
// 🚀 CORS: Hem Vercel hem localhost için izinli
@CrossOrigin(origins = {"https://uni-cycle-seven.vercel.app", "http://localhost:3000"})
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder; // 🚀 ŞİFRELEYİCİ EKLENDİ

    @Autowired
    public UserController(UserService userService, UserRepository userRepository, JdbcTemplate jdbcTemplate, JavaMailSender mailSender, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder; // 🚀 ŞİFRELEYİCİ BAŞLATILDI
    }

    // 1️⃣ KULLANICI KAYDI (🚀 ÇİFTE KİLİT YÖNTEMİ)
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            // KİLİT 1: Kaydetmeden önce PENDING yapıyoruz
            user.setStatus("PENDING");
            user.setRole("USER");

            // Arka plandaki inatçı servise gönderiyoruz
            User savedUser = userService.registerUser(user);

            // KİLİT 2: Eğer servis inat edip ACTIVE yaptıysa, arkasından gidip ZORLA eziyoruz!
            savedUser.setStatus("PENDING");
            savedUser.setRole("USER");
            userRepository.save(savedUser); // Veritabanına çiviliyoruz!

            return ResponseEntity.ok("Success: " + savedUser.getEmail());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }

    // 2️⃣ GİRİŞ YAP (LOGIN) - 🚀 PENDING ENGELİ KALDIRILDI!
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginData) {
        try {
            String email = loginData.get("email");
            String password = loginData.get("password");

            User user = userService.loginUser(email, password);

            // 🚫 GÜVENLİK DUVARI: SADECE Yasaklı/Kovulmuş (SUSPENDED) olanları engelliyoruz!
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
            userData.put("status", user.getStatus()); // Statüsünü Frontend'e gönderiyoruz (ACTIVE veya PENDING)
            userData.put("message", "Login Successful");

            return ResponseEntity.ok(userData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Giriş başarısız: " + e.getMessage());
        }
    }

    // 🚀🚀 ŞİFREMİ UNUTTUM AŞAMA 1: MAİLE KOD (OTP) GÖNDERME 🚀🚀
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam("email") String email) {
        try {
            // Kullanıcıyı veritabanında ara
            Optional<User> userOptional = userRepository.findByEmail(email);

            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Kullanıcı bulunamadı");
            }

            User user = userOptional.get();

            // 6 haneli rastgele kod oluştur
            String otp = String.format("%06d", new java.util.Random().nextInt(999999));

            // Kodu veritabanına kaydet
            user.setOtpCode(otp);
            userRepository.save(user);

            // Maili hazırla ve gönder
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

            return ResponseEntity.ok("Kod başarıyla gönderildi.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Mail gönderilirken hata oluştu: " + e.getMessage());
        }
    }

    // 🚀🚀 ŞİFREMİ UNUTTUM AŞAMA 2: KODU DOĞRULAYIP YENİ ŞİFREYİ KAYDETME 🚀🚀
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

            // Güvenlik Kontrolü: Gelen kod ile veritabanındaki kod aynı mı?
            if (user.getOtpCode() != null && user.getOtpCode().equals(otpCode)) {

                // 🚀 HATA BURADAYDI! YENİ ŞİFRE ARTIK VERİTABANINA ŞİFRELENEREK KAYDEDİLİYOR 🚀
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setOtpCode(null); // Kodu kullandıktan sonra güvenlik için SIFIRLA!

                userRepository.save(user);

                return ResponseEntity.ok("Şifre başarıyla güncellendi.");
            } else {
                // Kod hatalıysa veya yoksa engelle!
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
            return userRepository.findById(id)
                    .map(user -> {
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
                        return ResponseEntity.ok(userData);
                    })
                    .orElse(ResponseEntity.notFound().build());
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

    @GetMapping("/fix-db")
    public ResponseEntity<?> fixDatabase() {
        try {
            jdbcTemplate.execute("UPDATE users SET status = 'ACTIVE' WHERE status IS NULL");
            jdbcTemplate.execute("UPDATE users SET role = 'USER' WHERE role IS NULL");
            return ResponseEntity.ok("✅ VERİTABANI ONARILDI!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Hata oluştu: " + e.getMessage());
        }
    }
}