package com.unicycle.backend.repository;

import com.unicycle.backend.model.Message;
import com.unicycle.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE (m.sender = :user1 AND m.receiver = :user2) OR (m.sender = :user2 AND m.receiver = :user1) ORDER BY m.createdAt ASC")
    List<Message> findChatHistory(@Param("user1") User user1, @Param("user2") User user2);

    @Query("SELECT m FROM Message m WHERE m.sender = :user OR m.receiver = :user ORDER BY m.createdAt DESC")
    List<Message> findAllUserMessages(@Param("user") User user);

    long countByReceiverAndIsReadFalse(User receiver);

    // 🚀 ÇÖZÜM: İki kişi arasındaki tüm sohbeti SQL'den KÖKTEN silen komut!
    @Modifying
    @Transactional
    @Query("DELETE FROM Message m WHERE (m.sender.id = :user1Id AND m.receiver.id = :user2Id) OR (m.sender.id = :user2Id AND m.receiver.id = :user1Id)")
    void deleteConversation(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);
}