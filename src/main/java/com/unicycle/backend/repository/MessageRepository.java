package com.unicycle.backend.repository;

import com.unicycle.backend.model.Message;
import com.unicycle.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // İki kullanıcı arasındaki geçmiş sohbeti tarihe göre sıralı getirir
    @Query("SELECT m FROM Message m WHERE (m.sender = :user1 AND m.receiver = :user2) OR (m.sender = :user2 AND m.receiver = :user1) ORDER BY m.createdAt ASC")
    List<Message> findChatHistory(User user1, User user2);

    // Bir kullanıcının aldığı (veya gönderdiği) tüm mesajları getirir (Gelen Kutusu için)
    @Query("SELECT m FROM Message m WHERE m.sender = :user OR m.receiver = :user ORDER BY m.createdAt DESC")
    List<Message> findAllUserMessages(User user);

    // Okunmamış mesaj sayısını bulur
    long countByReceiverAndIsReadFalse(User receiver);
}