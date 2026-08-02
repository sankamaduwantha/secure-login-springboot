package com.userManagement.demo.service;

import com.userManagement.demo.model.Token;
import com.userManagement.demo.model.User;
import com.userManagement.demo.repository.TokenRepository;
import com.userManagement.demo.util.CryptoUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.security.MessageDigest;

import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Optional;

@Service
public class TokenService {

    private final TokenRepository tokenRepository;
    private final CryptoUtil cryptoUtil;

    @Value("${app.security.token.expiration-minutes:30}")
    private long expirationMinutes;

    public TokenService(TokenRepository tokenRepository, CryptoUtil cryptoUtil) {
        this.tokenRepository = tokenRepository;
        this.cryptoUtil = cryptoUtil;
    }

    public String createToken(User user) {
        try {
            Instant createdAt = Instant.now();
            String email = user.getEmail();

            String payload = email + "|" + createdAt.toEpochMilli();
            String rawToken = cryptoUtil.encrypt(payload);

            String tokenHash = hashToken(rawToken);

            Instant expiresAt = createdAt.plusSeconds(expirationMinutes * 60);
            Token token = new Token(tokenHash, email, Token.TokenType.EMAIL_VERIFICATION, user, createdAt, expiresAt);
            tokenRepository.save(token);

            return rawToken;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create verification token", e);
        }
    }

    private String hashToken(String rawToken) {
    try {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(rawToken.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
        throw new IllegalStateException("SHA-256 algorithm not available", e);
    }
}



    public Optional<Token> validateToken(String rawToken) {

    String decrypted;
    try {
        decrypted = cryptoUtil.decrypt(rawToken);
    } catch (Exception e) {
        return Optional.empty();
    }

    String[] parts = decrypted.split("\\|");
    if (parts.length != 2) {
        return Optional.empty();
    }

    String email = parts[0];

    String tokenHash = hashToken(rawToken);
    Optional<Token> tokenOptional = tokenRepository.findByTokenHash(tokenHash);

    if (tokenOptional.isEmpty()) {
        return Optional.empty();
    }

    Token token = tokenOptional.get();


     if (token.getExpiresAt().isBefore(Instant.now())) {
        return Optional.empty();
    }

    if (!token.getEmail().equals(email)) {
        return Optional.empty();
    }


    if (token.getUsedAt() != null) {
        return Optional.empty();
    }


    return Optional.of(token);
}



    public void markTokenAsUsed(Token token) {
        token.setUsedAt(Instant.now());
        tokenRepository.save(token);
    }
}