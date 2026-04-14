package com.unicycle.backend.repository;

import com.unicycle.backend.model.Message;
import com.unicycle.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // BUNU EKLEDİK
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // ÇÖZÜM: @Param anatasyonları eklendi. Artık SQL hangi User'ın hangisi olduğunu bilecek.
    @Query("SELECT m FROM Message m WHERE (m.sender = :user1 AND m.receiver = :user2) OR (m.sender = :user2 AND m.receiver = :user1) ORDER BY m.createdAt ASC")
    List<Message> findChatHistory(@Param("user1") User user1, @Param("user2") User user2);

    // ÇÖZÜM: Aynı şekilde buraya da @Param eklendi.
    @Query("SELECT m FROM Message m WHERE m.sender = :user OR m.receiver = :user ORDER BY m.createdAt DESC")
    List<Message> findAllUserMessages(@Param("user") User user);

    long countByReceiverAndIsReadFalse(User receiver);
}