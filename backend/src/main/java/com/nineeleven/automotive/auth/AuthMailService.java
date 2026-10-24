package com.nineeleven.automotive.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class AuthMailService {
    private final JavaMailSender mailSender;
    private final String from;
    private final String frontendUrl;
    public AuthMailService(JavaMailSender mailSender,
            @Value("${app.mail.from:noreply@9elevenautomotive.example}") String from,
            @Value("${app.frontend-url:http://localhost:4200}") String frontendUrl) {
        this.mailSender = mailSender; this.from = from; this.frontendUrl = frontendUrl;
    }
    public void sendVerification(String email, String token) {
        send(email, "Verify your 9-Eleven account", "Verify your email address: " + frontendUrl
                + "/verify-email?token=" + token + "\n\nThis link expires in 24 hours.");
    }
    public void sendPasswordReset(String email, String token) {
        send(email, "Reset your 9-Eleven password", "Reset your password: " + frontendUrl
                + "/reset-password?token=" + token + "\n\nThis link expires in 30 minutes.");
    }
    public void sendSetPassword(String email, String token) {
        send(email, "Set your 9-Eleven account password", "Set your password: " + frontendUrl
                + "/reset-password?token=" + token + "\n\nThis link expires in 30 minutes.");
    }
    private void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from); message.setTo(to); message.setSubject(subject); message.setText(body);
        mailSender.send(message);
    }
}
