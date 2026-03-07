package com.unicycle.backend.service;

import com.unicycle.backend.model.User;
import com.unicycle.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // --- 1. KAYIT OLMA (REGISTER) METODU ---
    // Öğrenciyi veritabanına kaydetmeden önceki o kritik son durak
    public User registerUser(User user) {
        // 1. Kullanıcının girdiği saf şifreyi al ve kırılmaz BCrypt formatına çevir
        String encodedPassword = passwordEncoder.encode(user.getPassword());

        // 2. Orijinal şifreyi, bu şifrelenmiş haliyle tamamen değiştir
        user.setPassword(encodedPassword);

        // 3. Güvenli hale gelmiş öğrenciyi veritabanına (Neon) kaydet!
        return userRepository.save(user);
    }

    // --- 2. YENİ EKLENEN GİRİŞ YAPMA (LOGIN) METODUMUZ 🚀 ---
    public User loginUser(String email, String rawPassword) {
        // 1. Veritabanından e-postaya göre öğrenciyi bul (Yoksa hata fırlat)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Bu e-posta ile kayıtlı bir öğrenci bulunamadı!"));

        // 2. Şifreler eşleşiyor mu kontrol et (Kullanıcının girdiği şifre vs Veritabanındaki şifreli şifre)
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("Şifre hatalı, lütfen tekrar dene!");
        }

        // 3. Her şey doğruysa öğrenciyi içeri al!
        return user;
    }
}