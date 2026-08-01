package com.userManagement.demo.service;

import com.userManagement.demo.model.Token;
import com.userManagement.demo.model.User;
import com.userManagement.demo.repository.TokenRepository;
import com.userManagement.demo.util.CryptoUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    // ===== CREATE TOKEN =====
    public String createToken(User user) {
        try {
            Instant createdAt = Instant.now();
            String email = user.getEmail();

            // payload eka email + "|" + timestamp widihata handala encrypt karanawa
            String payload = email + "|" + createdAt.toEpochMilli();
            String rawToken = cryptoUtil.encrypt(payload);

            // DB eke record eka save karanawa (usedAt = null, default)
            Instant expiresAt = createdAt.plusSeconds(expirationMinutes * 60);
            Token token = new Token(email, Token.TokenType.EMAIL_VERIFICATION, user, createdAt, expiresAt);
            tokenRepository.save(token);

            return rawToken; // methanin email link ekata yanawa
        } catch (Exception e) {
            throw new RuntimeException("Failed to create verification token", e);
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
    long timestampMillis;
    try {
        timestampMillis = Long.parseLong(parts[1]);
    } catch (NumberFormatException e) {
        return Optional.empty();
    }
    Instant tokenCreatedAt = Instant.ofEpochMilli(timestampMillis);

    // expiry check - decrypted timestamp eken ma
    if (tokenCreatedAt.isBefore(Instant.now().minusSeconds(expirationMinutes * 60))) {
        return Optional.empty();
    }

    // email + not-used token eka DB eken gannawa (createdAt match karanne na)
    Optional<Token> tokenOptional =
            tokenRepository.findByEmailAndUsedAtIsNullOrderByCreatedAtDesc(email);

    if (tokenOptional.isEmpty()) {
        return Optional.empty();
    }

    Token token = tokenOptional.get();

    if (!token.getEmail().equals(email)) {
        return Optional.empty();
    }

    return Optional.of(token);
}

    // ===== MARK TOKEN AS USED =====
    public void markTokenAsUsed(Token token) {
        token.setUsedAt(Instant.now());
        tokenRepository.save(token);
    }
}