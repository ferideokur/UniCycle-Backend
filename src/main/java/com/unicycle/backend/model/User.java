package com.unicycle.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    // 🎓 Kullanıcının Üniversite Bilgisi
    @Column(name = "university", length = 150)
    private String university;

    // 📝 YENİ: Kullanıcı Biyografisi
    @Column(name = "bio", length = 255)
    private String bio;

    // 🖼️ YENİ: Profil Fotoğrafı (Base64 çok uzun olduğu için TEXT olmak zorunda!)
    @Column(name = "profile_image", columnDefinition = "TEXT")
    private String profileImage;

    // 🖼️ YENİ: Kapak Fotoğrafı (Base64)
    @Column(name = "cover_image", columnDefinition = "TEXT")
    private String coverImage;

    // ↕️ YENİ: Kapak Fotoğrafı Dikey Pozisyonu (0-100 arası)
    @Column(name = "cover_y")
    private Integer coverY;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // KULLANICININ SON GÖRÜLME ZAMANI
    @Column(name = "last_active")
    private LocalDateTime lastActive;

    // 🟢 ÇEVRİMİÇİ DURUMU
    @Column(name = "is_online")
    private Boolean isOnline = false;

    // Boş Constructor (Hibernate için şart)
    public User() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // --- MANUEL GETTER VE SETTER'LAR ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUniversity() { return university; }
    public void setUniversity(String university) { this.university = university; }

    // 🚀 YENİ EKLENENLERİN GETTER/SETTER'LARI
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }

    public Integer getCoverY() { return coverY; }
    public void setCoverY(Integer coverY) { this.coverY = coverY; }
    // ----------------------------------------

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastActive() { return lastActive; }
    public void setLastActive(LocalDateTime lastActive) { this.lastActive = lastActive; }

    public Boolean getIsOnline() { return isOnline; }
    public void setOnline(Boolean online) { this.isOnline = online; }
}