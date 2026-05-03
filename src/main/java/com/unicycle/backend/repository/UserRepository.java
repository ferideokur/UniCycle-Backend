package com.unicycle.backend.repository;

import com.unicycle.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 📧 Email ile kullanıcı bulma (Login için)
    Optional<User> findByEmail(String email);

    // 🔍 İsim ile kullanıcı arama (Arama motoru için)
    List<User> findByFullNameContainingIgnoreCase(String fullName);

    // ⏳ STATÜ SORGULAMA (Admin Paneli için en kritik sorgu)
    // "PENDING" veya "ACTIVE" olan kullanıcıları listeler.
    List<User> findByStatus(String status);
}