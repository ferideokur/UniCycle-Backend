package com.unicycle.backend.repository;

import com.unicycle.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Öğrenci giriş yaparken "Bu e-postaya sahip biri var mı?" diye sormamızı sağlayacak özel radarımız
    Optional<User> findByEmail(String email);

}