package com.userManagement.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    // Base URL of your app - used to build the verification/reset links
    @Value("${app.base-url}")
    private String baseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ---- 1. SEND EMAIL VERIFICATION LINK ----
    public void sendVerificationEmail(String toEmail, String rawToken) {
        String link = baseUrl + "/api/auth/verify-email?token=" + rawToken;

        String subject = "Verify your email address";
        String body = "Click the link below to verify your email:\n\n" + link
                + "\n\nThis link expires in 30 minutes. If you didn't request this, ignore this email.";

        sendEmail(toEmail, subject, body);
    }

    // ---- 2. SEND PASSWORD RESET LINK ----
    public void sendPasswordResetEmail(String toEmail, String rawToken) {
        String link = baseUrl + "/api/auth/reset-password?token=" + rawToken;

        String subject = "Reset your password";
        String body = "Click the link below to reset your password:\n\n" + link
                + "\n\nThis link expires in 30 minutes. If you didn't request this, ignore this email.";

        sendEmail(toEmail, subject, body);
    }

    // ---- HELPER: actually sends the email ----
    private void sendEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
