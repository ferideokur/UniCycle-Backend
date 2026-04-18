package com.unicycle.backend.controller;

import com.unicycle.backend.model.Message;
import com.unicycle.backend.model.User;
import com.unicycle.backend.repository.MessageRepository;
import com.unicycle.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
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
        try {
            User user1 = userRepository.findById(user1Id).orElseThrow();
            User user2 = userRepository.findById(user2Id).orElseThrow();

            List<Message> history = messageRepository.findChatHistory(user1, user2);
            boolean needsSave = false;

            for (Message m : history) {
                if (m.getReceiver().getId().equals(user1Id) && !m.isRead()) {
                    m.setRead(true);
                    needsSave = true;
                }
            }

            if (needsSave) {
                messageRepository.saveAll(history);
            }

            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Geçmiş alınamadı: " + e.getMessage());
        }
    }

    // 3. GELEN KUTUSU LİSTESİ
    @GetMapping("/inbox/{userId}")
    public ResponseEntity<?> getInbox(@PathVariable Long userId) {
        try {
            User me = userRepository.findById(userId).orElseThrow();
            List<Message> allMessages = messageRepository.findAllUserMessages(me);

            Map<Long, Map<String, Object>> inbox = new LinkedHashMap<>();

            for (Message m : allMessages) {
                User otherUser = m.getSender().getId().equals(userId) ? m.getReceiver() : m.getSender();

                if (!inbox.containsKey(otherUser.getId())) {
                    Map<String, Object> chatInfo = new HashMap<>();
                    chatInfo.put("id", otherUser.getId());
                    chatInfo.put("name", otherUser.getFullName());
                    chatInfo.put("lastMsg", m.getContent());
                    chatInfo.put("time", m.getCreatedAt());

                    int unreadCount = (m.getReceiver().getId().equals(userId) && !m.isRead()) ? 1 : 0;
                    chatInfo.put("unread", unreadCount);

                    inbox.put(otherUser.getId(), chatInfo);
                } else {
                    if (m.getReceiver().getId().equals(userId) && !m.isRead()) {
                        Map<String, Object> chatInfo = inbox.get(otherUser.getId());
                        chatInfo.put("unread", (int) chatInfo.get("unread") + 1);
                    }
                }
            }
            return ResponseEntity.ok(inbox.values());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Gelen kutusu alınamadı: " + e.getMessage());
        }
    }

    // 🚀 ÇÖZÜM 1: TEK BİR MESAJI SİLME API'Sİ
    @DeleteMapping("/{msgId}")
    public ResponseEntity<?> deleteSingleMessage(@PathVariable Long msgId) {
        try {
            messageRepository.deleteById(msgId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Mesaj veritabanından kalıcı olarak silindi."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Mesaj silinemedi: " + e.getMessage());
        }
    }

    // 🚀 ÇÖZÜM 2: TÜM SOHBETİ (KİŞİYİ) SİLME API'Sİ
    @DeleteMapping("/conversation")
    public ResponseEntity<?> deleteEntireConversation(@RequestParam Long user1Id, @RequestParam Long user2Id) {
        try {
            messageRepository.deleteConversation(user1Id, user2Id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Sohbet geçmişi veritabanından kalıcı olarak silindi."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Sohbet silinemedi: " + e.getMessage());
        }
    }
}