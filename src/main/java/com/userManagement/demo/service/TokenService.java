package com.userManagement.demo.service;

import com.userManagement.demo.model.Token;
import com.userManagement.demo.model.User;
import com.userManagement.demo.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Service
public class TokenService {

    private final TokenRepository tokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    // How long tokens stay valid before expiring (minutes)
    @Value("${token.expiration-minutes:30}")
    private long expirationMinutes;

    public TokenService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public String createToken(User user) {

        String rawToken = generateRawToken();

        String hashedToken = hashToken(rawToken);

        Instant expiryTime = Instant.now().plusSeconds(expirationMinutes * 60);
        Token token = new Token(hashedToken, user, expiryTime);
        tokenRepository.save(token);

        return rawToken;
    }

    // ---- 2. VALIDATE A TOKEN (called when user clicks the email link) ----
    public Optional<Token> validateToken(String rawToken) {
        String hashedToken = hashToken(rawToken);

        Optional<Token> tokenOptional = tokenRepository.findByTokenHash(hashedToken);

        if (tokenOptional.isEmpty()) {
            return Optional.empty(); // token doesn't exist
        }

        Token token = tokenOptional.get();

        if (token.getUsedAt() != null) {
            return Optional.empty(); // already used - reject (one-time-use rule)
        }

        if (token.getExpiresAt().isBefore(Instant.now())) {
            return Optional.empty(); // expired
        }

        return Optional.of(token);
    }

    // ---- 3. MARK TOKEN AS USED (called right after a successful validation) ----
    public void markTokenAsUsed(Token token) {
        token.setUsedAt(Instant.now());
        tokenRepository.save(token);
    }

    // ---- HELPER: generate a secure random raw token ----
    private String generateRawToken() {
        byte[] randomBytes = new byte[32]; // 256 bits of randomness
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    // ---- HELPER: hash the raw token using SHA-256 ----
    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes());

            // convert bytes to a lowercase hex string (matches your Token.java column comment)
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is always available in the JDK, so this should never actually happen
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
