package com.unicycle.backend.controller;

import com.unicycle.backend.model.Follow;
import com.unicycle.backend.model.Notification;
import com.unicycle.backend.model.User;
import com.unicycle.backend.repository.FollowRepository;
import com.unicycle.backend.repository.NotificationRepository;
import com.unicycle.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interaction")
@CrossOrigin(origins = "https://uni-cycle-seven.vercel.app") // Vercel'den gelen isteklere izin veriyoruz!
public class InteractionController {

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    // 1. TAKİP ETME İŞLEMİ (Butona basılınca burası çalışacak)
    @PostMapping("/follow")
    @Transactional
    public ResponseEntity<?> toggleFollow(@RequestBody Map<String, Long> payload) {
        Long followerId = payload.get("followerId"); // Takip eden kişi (Sen)
        Long followingId = payload.get("followingId"); // Takip edilen kişi (Karşı taraf)

        User follower = userRepository.findById(followerId).orElseThrow();
        User following = userRepository.findById(followingId).orElseThrow();

        // Eğer zaten takip ediyorsa, takipten çıkar (Toggle mantığı)
        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            followRepository.deleteByFollowerAndFollowing(follower, following);
            return ResponseEntity.ok(Map.of("message", "Takipten çıkıldı", "isFollowing", false));
        } else {
            // Takip etmiyorsa, yeni takip oluştur
            Follow newFollow = new Follow();
            newFollow.setFollower(follower);
            newFollow.setFollowing(following);
            followRepository.save(newFollow);

            // BİLDİRİM FIRLAT! 🔔 (Karşı tarafa gidiyor)
            Notification notification = new Notification();
            notification.setUser(following);
            notification.setMessage(follower.getFullName() + " seni takip etmeye başladı.");
            notificationRepository.save(notification);

            return ResponseEntity.ok(Map.of("message", "Takip edildi", "isFollowing", true));
        }
    }

    // 2. KULLANICININ BİLDİRİMLERİNİ GETİR (Sağ üstteki kalp/zil ikonu için)
    @GetMapping("/notifications/{userId}")
    public ResponseEntity<?> getNotifications(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/notifications")
    public ResponseEntity<?> createCustomNotification(@RequestBody Map<String, Object> payload) {
        try {
            // React'tan gelen verileri alıyoruz
            Long targetUserId = Long.valueOf(payload.get("userId").toString());
            String message = payload.get("message").toString();

            // Bildirimin gideceği kullanıcıyı bul
            User targetUser = userRepository.findById(targetUserId).orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

            // Yeni bir bildirim yarat ve kaydet
            Notification notification = new Notification();
            notification.setUser(targetUser);
            notification.setMessage(message);
            notification.setRead(false);
            notification.setCreatedAt(java.time.LocalDateTime.now()); // Sizin tarih objeniz neyse o (Date veya LocalDateTime)

            notificationRepository.save(notification);

            return ResponseEntity.ok(Map.of("success", true, "message", "Bildirim veritabanına kaydedildi!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Bildirim kaydedilemedi: " + e.getMessage());
        }
    }
    // 3. BİLDİRİM SİLME İŞLEMİ (Tekli veya Tümü)
    @DeleteMapping("/notifications/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        try {
            if (notificationRepository.existsById(id)) {
                notificationRepository.deleteById(id);
                return ResponseEntity.ok(Map.of("success", true, "message", "Bildirim başarıyla silindi."));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Bildirim silinirken hata oluştu: " + e.getMessage());
        }
    }
}