package com.unicycle.backend.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Bildirimin gideceği kişi
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Bildirim Mesajı
    private String message;

    // Okundu mu? (Kalpteki kırmızı noktayı kontrol etmek için)
    private boolean isRead = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    // --- GETTER VE SETTER METOTLARI ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { this.isRead = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}