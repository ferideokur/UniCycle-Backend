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
@CrossOrigin(origins = "*") // Vercel'den veya Localhost'tan engelsiz veri çekimi için yıldıza çevrildi
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

            // Eğer modelinizde createdAt alanı otomatik dolmuyorsa (şimdiki zamanı atıyoruz):
            // msg.setCreatedAt(new Date());

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

            // Mesajlar okunduğunda isRead'i true yapalım
            for (Message m : history) {
                // Eğer mesajı ben aldıysam ve henüz okunmadıysa, okundu olarak işaretle
                if (m.getReceiver().getId().equals(user1Id) && !m.isRead()) {
                    m.setRead(true);
                    needsSave = true;
                }
            }

            if (needsSave) {
                messageRepository.saveAll(history); // Tek tek değil, hepsini birden kaydet (Performans için)
            }

            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Geçmiş alınamadı: " + e.getMessage());
        }
    }

    // 3. GELEN KUTUSU LİSTESİ (Kimlerle konuşmuş?)
    @GetMapping("/inbox/{userId}")
    public ResponseEntity<?> getInbox(@PathVariable Long userId) {
        try {
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

                    // Eğer mesaj bana geldiyse ve okunmadıysa sayacı 1 yap, aksi halde 0
                    int unreadCount = (m.getReceiver().getId().equals(userId) && !m.isRead()) ? 1 : 0;
                    chatInfo.put("unread", unreadCount);

                    inbox.put(otherUser.getId(), chatInfo);
                } else {
                    // Eğer bu kişiyle daha önce bir mesaj bulunduysa (LinkedHashMap'te varsa)
                    // ve bu sıradaki mesaj da bana geldiyse ve okunmadıysa sayacı artır
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
}