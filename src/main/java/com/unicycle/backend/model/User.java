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

    // 📝 Kullanıcı Biyografisi
    @Column(name = "bio", length = 255)
    private String bio;

    // 🖼️ Profil Fotoğrafı (Base64)
    @Column(name = "profile_image", columnDefinition = "TEXT")
    private String profileImage;

    // 🖼️ Kapak Fotoğrafı (Base64)
    @Column(name = "cover_image", columnDefinition = "TEXT")
    private String coverImage;

    // ↕️ Kapak Fotoğrafı Dikey Pozisyonu
    @Column(name = "cover_y")
    private Integer coverY = 50;

    // 👑 ROZET SİSTEMİ: Kullanıcının Rolü (USER veya ADMIN)
    @Column(name = "role")
    private String role = "USER";

    // ⏳ ONAY SİSTEMİ: Kullanıcının Durumu (PENDING, ACTIVE, SUSPENDED)
    @Column(name = "status")
    private String status = "PENDING";

    // 📄 ÖĞRENCİ BELGESİ: Admin panelinde inceleyeceğin dosya (Base64 TEXT)
    @Column(name = "document_url", columnDefinition = "TEXT")
    private String documentUrl;

    // 🚀 YENİ EKLENDİ: ŞİFRE SIFIRLAMA KODU (OTP) 🚀
    @Column(name = "otp_code", length = 6)
    private String otpCode;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_active")
    private LocalDateTime lastActive;

    @Column(name = "is_online")
    private Boolean isOnline = false;

    // Boş Constructor (Hibernate için şart)
    public User() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        // Yeni kayıtlar her zaman PENDING olarak başlasın
        if (this.role == null) this.role = "USER";
        if (this.status == null) this.status = "PENDING";
    }

    // --- GETTER VE SETTER'LAR ---

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

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }

    public Integer getCoverY() { return coverY; }
    public void setCoverY(Integer coverY) { this.coverY = coverY; }

    // 🚀 SİHİRLİ GETTER'LAR
    public String getRole() {
        return (role == null) ? "USER" : role;
    }
    public void setRole(String role) { this.role = role; }

    public String getStatus() {
        return (status == null) ? "ACTIVE" : status;
    }
    public void setStatus(String status) { this.status = status; }

    public String getDocumentUrl() { return documentUrl; }
    public void setDocumentUrl(String documentUrl) { this.documentUrl = documentUrl; }

    // 🚀 YENİ GETTER VE SETTER (OTP KODU) 🚀
    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastActive() { return lastActive; }
    public void setLastActive(LocalDateTime lastActive) { this.lastActive = lastActive; }

    public Boolean getIsOnline() { return isOnline; }
    public void setOnline(Boolean online) { this.isOnline = online; }
}