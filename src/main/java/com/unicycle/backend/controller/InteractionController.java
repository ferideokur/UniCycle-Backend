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
// 🚀 GÜNCELLEME: Localhost'u da ekledik ki sen test ederken CORS hatası yeme!
@CrossOrigin(origins = {"https://uni-cycle-seven.vercel.app", "http://localhost:3000"})
public class InteractionController {

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/follow")
    @Transactional
    public ResponseEntity<?> toggleFollow(@RequestBody Map<String, Long> payload) {
        Long followerId = payload.get("followerId");
        Long followingId = payload.get("followingId");

        User follower = userRepository.findById(followerId).orElseThrow();
        User following = userRepository.findById(followingId).orElseThrow();

        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            followRepository.deleteByFollowerAndFollowing(follower, following);
            return ResponseEntity.ok(Map.of("message", "Takipten çıkıldı", "isFollowing", false));
        } else {
            Follow newFollow = new Follow();
            newFollow.setFollower(follower);
            newFollow.setFollowing(following);
            followRepository.save(newFollow);

            Notification notification = new Notification();
            notification.setUser(following);
            notification.setMessage(follower.getFullName() + " seni takip etmeye başladı.");
            notificationRepository.save(notification);

            return ResponseEntity.ok(Map.of("message", "Takip edildi", "isFollowing", true));
        }
    }

    @GetMapping("/notifications/{userId}")
    public ResponseEntity<?> getNotifications(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/notifications")
    public ResponseEntity<?> createCustomNotification(@RequestBody Map<String, Object> payload) {
        try {
            Long targetUserId = Long.valueOf(payload.get("userId").toString());
            String message = payload.get("message").toString();

            User targetUser = userRepository.findById(targetUserId).orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

            Notification notification = new Notification();
            notification.setUser(targetUser);
            notification.setMessage(message);
            notification.setRead(false);
            notification.setCreatedAt(java.time.LocalDateTime.now());

            notificationRepository.save(notification);

            return ResponseEntity.ok(Map.of("success", true, "message", "Bildirim veritabanına kaydedildi!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Bildirim kaydedilemedi: " + e.getMessage());
        }
    }

    // TEKLİ SİLME
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

    // 🚀 YENİ: KULLANICININ TÜM BİLDİRİMLERİNİ TEK SEFERDE SİLME!
    @DeleteMapping("/notifications/user/{userId}")
    @Transactional
    public ResponseEntity<?> deleteAllUserNotifications(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId).orElseThrow();
            List<Notification> userNotifs = notificationRepository.findByUserOrderByCreatedAtDesc(user);
            notificationRepository.deleteAll(userNotifs);
            return ResponseEntity.ok(Map.of("success", true, "message", "Tüm bildirimler başarıyla uçuruldu!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Toplu silme hatası: " + e.getMessage());
        }
    }
}