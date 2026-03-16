package com.unicycle.backend.repository;

import com.unicycle.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    // 🚀 Custom SQL Query for User Search
    // Searches users by their full name (case-insensitive)
    List<User> findByFullNameContainingIgnoreCase(String fullName);
}