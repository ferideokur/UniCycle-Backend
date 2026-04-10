package com.unicycle.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. CORS'u aç (Artık hem localhost hem Vercel girebilir)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 2. CSRF korumasını kapat (Yoksa POST işlemlerini engeller)
                .csrf(csrf -> csrf.disable())
                // 3. Şimdilik tüm API kapılarını kilitsiz bırak
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    // 🚀 İŞTE SİSTEMİN ÇÖKMESİNİ ENGELLEYEN O EKSİK PARÇA!
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 🔥 İŞTE BÖLÜM SONU CANAVARINI YENDİĞİMİZ YER! 🔥
        // Hem senin bilgisayarına hem de Vercel'deki canlı siteye izin verdik.
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "https://uni-cycle-seven.vercel.app"
        ));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}