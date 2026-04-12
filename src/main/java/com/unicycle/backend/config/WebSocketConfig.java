package com.unicycle.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // İnsanların birbirine mesaj fırlatacağı "yayın" kanalı
        config.enableSimpleBroker("/topic");
        // Kullanıcıların mesaj gönderirken kullanacağı adresin başlığı
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // VS Code'daki sitemizin arka plana canlı bağlanacağı ana kapı!
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*") // Şimdilik güvenlik engelini (CORS) kaldırıyoruz
                .withSockJS();
    }
}