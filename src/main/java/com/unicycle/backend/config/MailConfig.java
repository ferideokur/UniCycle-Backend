package com.unicycle.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);

        // Senin açtığın profesyonel mail ve aldığımız uygulama şifresi
        mailSender.setUsername("unicycledestek@gmail.com");
        mailSender.setPassword("cikeatsrwgokaicc");

        // Google'ın istediği güvenlik ayarları
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true"); // Konsolda mailin gittiğini canlı izlemek için

        return mailSender;
    }
}