package com.unicycle.backend.config;

import com.unicycle.backend.model.User;
import com.unicycle.backend.repository.UserRepository;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;

@Component
public class WebSocketEventListener {

    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketEventListener(UserRepository userRepository, SimpMessagingTemplate messagingTemplate) {
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // 🔴 KULLANICI ÇIKTIĞINDA (Sekmeyi kapattığında veya interneti koptuğunda) BURASI ÇALIŞIR
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(event.getMessage());

        // Bağlantı kopunca, bu kopan kordonun hangi kullanıcıya ait olduğunu hafızadan buluyoruz
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");

        if (userId != null) {
            userRepository.findById(userId).ifPresent(user -> {
                // 1. Veritabanında anında çevrimdışı yap
                user.setOnline(false);
                // 2. Son görülme zamanını tam şu an olarak güncelle
                user.setLastActive(LocalDateTime.now());
                userRepository.save(user);

                System.out.println("Kullanıcı Çıkış Yaptı (Kordon Koptu): " + user.getFullName());

                // İleride buraya: "Herkese bu adamın çıktığını haber ver" kodunu da ekleyeceğiz
            });
        }
    }
}