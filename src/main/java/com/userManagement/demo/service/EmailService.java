package com.userManagement.demo.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String toEmail, String rawToken) {
        String link = baseUrl + "/api/auth/verify-email?token=" + rawToken;

        String subject = "Verify your email address";

        String htmlBody = """
                <p>Click the link below to verify your email:</p>
                <p><a href="%s" style="display:inline-block;padding:10px 20px;
                background-color:#4CAF50;color:#ffffff;text-decoration:none;
                border-radius:5px;">Verify your email</a></p>
                <p>This link expires in 30 minutes. If you didn't request this, ignore this email.</p>
                """.formatted(link);

        sendHtmlEmail(toEmail, subject, htmlBody);
    }

    private void sendHtmlEmail(String toEmail, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML content
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send email", e);
        }
    }
}