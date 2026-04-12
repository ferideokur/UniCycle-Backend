package com.unicycle.backend.controller;

import com.unicycle.backend.model.Message;
import com.unicycle.backend.model.User;
import com.unicycle.backend.repository.MessageRepository;
import com.unicycle.backend.repository.UserRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public ChatController(SimpMessagingTemplate messagingTemplate,
                          MessageRepository messageRepository,
                          UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    // 🟢 1. KULLANICI SİTEYE GİRDİĞİNDE: Çevrimiçi Yap ve Kordon Bağla
    @MessageMapping("/chat.connect")
    public void connectUser(@Payload Long userId, SimpMessageHeaderAccessor headerAccessor) {
        // Ajanımızın kordon koptuğunda (çıkışta) bulabilmesi için kullanıcının ID'sini kordona kazıyoruz
        headerAccessor.getSessionAttributes().put("userId", userId);

        userRepository.findById(userId).ifPresent(user -> {
            user.setOnline(true);
            userRepository.save(user);
            System.out.println("🟢 ÇEVRİMİÇİ OLDU: " + user.getFullName());
        });
    }

    // 💬 2. MESAJ GÖNDERİLDİĞİNDE: Veritabanına Kaydet ve Hedefe Fırlat
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatRequest request) {
        User sender = userRepository.findById(request.senderId).orElse(null);
        User receiver = userRepository.findById(request.receiverId).orElse(null);

        if (sender != null && receiver != null) {
            // 1. Mesajı veritabanına gerçekten kaydediyoruz
            Message message = new Message();
            message.setSender(sender);
            message.setReceiver(receiver);
            message.setContent(request.content);
            messageRepository.save(message);

            System.out.println("💬 MESAJ GİTTİ: " + sender.getFullName() + " -> " + receiver.getFullName());

            // 2. Mesajı radyo frekansı gibi sadece bu iki kişinin özel kanalına yolluyoruz
            messagingTemplate.convertAndSend("/queue/messages/" + receiver.getId(), message);
            messagingTemplate.convertAndSend("/queue/messages/" + sender.getId(), message);
        }
    }

    // Gelen JSON verisini karşılamak için oluşturduğumuz minik bir kalıp
    public static class ChatRequest {
        public Long senderId;
        public Long receiverId;
        public String content;
    }
}