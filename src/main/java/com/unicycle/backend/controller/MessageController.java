package com.unicycle.backend.controller;

import com.unicycle.backend.model.Message;
import com.unicycle.backend.model.User;
import com.unicycle.backend.repository.MessageRepository;
import com.unicycle.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "http://localhost:3000") // React'a izin veriyoruz
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    // 1. MESAJ GÖNDERME
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, Object> payload) {
        try {
            Long senderId = Long.valueOf(payload.get("senderId").toString());
            Long receiverId = Long.valueOf(payload.get("receiverId").toString());
            String content = payload.get("content").toString();

            User sender = userRepository.findById(senderId).orElseThrow();
            User receiver = userRepository.findById(receiverId).orElseThrow();

            Message msg = new Message();
            msg.setSender(sender);
            msg.setReceiver(receiver);
            msg.setContent(content);
            messageRepository.save(msg);

            return ResponseEntity.ok(Map.of("success", true, "message", "Mesaj iletildi", "data", msg));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Mesaj gönderilemedi: " + e.getMessage());
        }
    }

    // 2. İKİ KİŞİ ARASINDAKİ SOHBET GEÇMİŞİNİ GETİRME
    @GetMapping("/history")
    public ResponseEntity<?> getChatHistory(@RequestParam Long user1Id, @RequestParam Long user2Id) {
        User user1 = userRepository.findById(user1Id).orElseThrow();
        User user2 = userRepository.findById(user2Id).orElseThrow();

        List<Message> history = messageRepository.findChatHistory(user1, user2);

        // Mesajlar okunduğunda isRead'i true yapalım
        for (Message m : history) {
            if (m.getReceiver().getId().equals(user1Id) && !m.isRead()) {
                m.setRead(true);
                messageRepository.save(m);
            }
        }

        return ResponseEntity.ok(history);
    }

    // 3. GELEN KUTUSU LİSTESİ (Kimlerle konuşmuş?)
    @GetMapping("/inbox/{userId}")
    public ResponseEntity<?> getInbox(@PathVariable Long userId) {
        User me = userRepository.findById(userId).orElseThrow();
        List<Message> allMessages = messageRepository.findAllUserMessages(me);

        // Son mesajlara göre gruplama yapıp gelen kutusunu oluşturuyoruz
        Map<Long, Map<String, Object>> inbox = new LinkedHashMap<>();

        for (Message m : allMessages) {
            User otherUser = m.getSender().getId().equals(userId) ? m.getReceiver() : m.getSender();

            if (!inbox.containsKey(otherUser.getId())) {
                Map<String, Object> chatInfo = new HashMap<>();
                chatInfo.put("id", otherUser.getId());
                chatInfo.put("name", otherUser.getFullName());
                chatInfo.put("lastMsg", m.getContent());
                chatInfo.put("time", m.getCreatedAt());
                chatInfo.put("unread", m.getReceiver().getId().equals(userId) && !m.isRead() ? 1 : 0);
                inbox.put(otherUser.getId(), chatInfo);
            } else {
                if (m.getReceiver().getId().equals(userId) && !m.isRead()) {
                    Map<String, Object> chatInfo = inbox.get(otherUser.getId());
                    chatInfo.put("unread", (int) chatInfo.get("unread") + 1);
                }
            }
        }
        return ResponseEntity.ok(inbox.values());
    }
}