package com.unicycle.backend.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.List;

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

    // 🖼️ Profil Fotoğrafı
    @Column(name = "profile_image", columnDefinition = "TEXT")
    private String profileImage;

    // 🖼️ Kapak Fotoğrafı - SİSTEMİ BOĞMAMASI İÇİN ENGELLENDİ
    @JsonIgnore
    @Column(name = "cover_image", columnDefinition = "TEXT")
    private String coverImage;

    // ↕️ Kapak Fotoğrafı Dikey Pozisyonu
    @Column(name = "cover_y")
    private Integer coverY = 50;

    // 👑 ROZET SİSTEMİ
    @Column(name = "role")
    private String role = "USER";

    // ⏳ ONAY SİSTEMİ
    @Column(name = "status")
    private String status = "PENDING";

    // 🚀 ÖĞRENCİ BELGESİ - SİSTEMİ BOĞMAMASI İÇİN ENGELLENDİ
    @JsonIgnore
    @Column(name = "document_base64", columnDefinition = "TEXT")
    private String documentBase64;

    // 🚀 ŞİFRE SIFIRLAMA KODU (OTP)
    @Column(name = "otp_code", length = 6)
    private String otpCode;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_active")
    private LocalDateTime lastActive;

    @Column(name = "is_online")
    private Boolean isOnline = false;

    // 🚀🚀 KÖKTEN SİLME VE SONSUZ DÖNGÜ KIRICI SİHİRLER BURADA 🚀🚀

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products;

    // Loglardan yakaladığımız o gizli katiller (Mesajlar ve Bildirimler)!
    // Bunlar senin eski kodunda yoktu, sistemi bunlar çökertiyordu.

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notifications;

    @JsonIgnore
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> sentMessages;

    @JsonIgnore
    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> receivedMessages;

    public User() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.role == null) this.role = "USER";
        if (this.status == null) this.status = "PENDING";
    }

    // --- BÜTÜN GETTER VE SETTER'LAR ---
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

    public String getRole() { return (role == null) ? "USER" : role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return (status == null) ? "ACTIVE" : status; }
    public void setStatus(String status) { this.status = status; }

    public String getDocumentBase64() { return documentBase64; }
    public void setDocumentBase64(String documentBase64) { this.documentBase64 = documentBase64; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastActive() { return lastActive; }
    public void setLastActive(LocalDateTime lastActive) { this.lastActive = lastActive; }

    public Boolean getIsOnline() { return isOnline; }
    public void setOnline(Boolean online) { this.isOnline = online; }

    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }

    public List<Notification> getNotifications() { return notifications; }
    public void setNotifications(List<Notification> notifications) { this.notifications = notifications; }

    public List<Message> getSentMessages() { return sentMessages; }
    public void setSentMessages(List<Message> sentMessages) { this.sentMessages = sentMessages; }

    public List<Message> getReceivedMessages() { return receivedMessages; }
    public void setReceivedMessages(List<Message> receivedMessages) { this.receivedMessages = receivedMessages; }
}