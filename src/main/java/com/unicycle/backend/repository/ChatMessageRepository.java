package com.unicycle.backend.repository;

import com.unicycle.backend.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // İki kullanıcı arasındaki tüm geçmiş mesajları tarihe göre sıralı getiren özel komut
    @Query("SELECT m FROM ChatMessage m WHERE (m.senderUsername = :user1 AND m.receiverUsername = :user2) OR (m.senderUsername = :user2 AND m.receiverUsername = :user1) ORDER BY m.timestamp ASC")
    List<ChatMessage> findChatHistory(@Param("user1") String user1, @Param("user2") String user2);
}