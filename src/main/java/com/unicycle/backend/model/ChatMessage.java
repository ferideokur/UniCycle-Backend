package com.unicycle.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String senderUsername; // Mesajı gönderen
    private String receiverUsername; // Mesajı alan
    private String content; // Mesajın içeriği

    private LocalDateTime timestamp; // Gönderilme zamanı (Senin istediğin zaman özelliği)

    private boolean isRead; // Karşı taraf okudu mu?

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now(); // Mesaj veritabanına kaydedildiği anın saatini otomatik alır
    }
}