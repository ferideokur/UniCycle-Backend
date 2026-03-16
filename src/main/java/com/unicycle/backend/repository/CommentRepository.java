package com.unicycle.backend.repository;

import com.unicycle.backend.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // Bir ilana ait yorumları en yeniden en eskiye sıralayarak getirir
    List<Comment> findByProductIdOrderByCreatedAtDesc(Long productId);
}