package com.unicycle.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mesajı Gönderen Kişi
    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    // Mesajı Alan Kişi
    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(nullable = false, length = 1000)
    private String content;

    // Karşı taraf mesajı okudu mu?
    private boolean isRead = false;

    // MESAJ SİLME
    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    // 🚀 YENİ EKLENEN: WHATSAPP ALINTI BAĞLANTISI
    // Eğer bu mesaj başka bir mesaja cevap olarak atıldıysa, o orijinal mesajın ID'sini tutacak.
    // Normal mesajlarda burası boş (null) kalacak.
    @Column(name = "replied_message_id", nullable = true)
    private Long repliedMessageId;

    private LocalDateTime createdAt = LocalDateTime.now();

    // --- GETTER VE SETTER METOTLARI ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }

    public User getReceiver() { return receiver; }
    public void setReceiver(User receiver) { this.receiver = receiver; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { this.isRead = read; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { this.isDeleted = deleted; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // 🚀 YENİ GETTER VE SETTER
    public Long getRepliedMessageId() { return repliedMessageId; }
    public void setRepliedMessageId(Long repliedMessageId) { this.repliedMessageId = repliedMessageId; }

    public Message() {}
}