package com.unicycle.backend.repository;

import com.unicycle.backend.model.Notification;
import com.unicycle.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Bir kullanıcının tüm bildirimlerini en yeniden en eskiye doğru getir
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    // Sadece okunmamış bildirimleri getir (Kalbin üstündeki kırmızı nokta için)
    List<Notification> findByUserAndIsReadFalse(User user);
}