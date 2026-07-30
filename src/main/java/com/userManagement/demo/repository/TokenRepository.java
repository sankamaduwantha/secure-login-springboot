package com.userManagement.demo.repository;

import com.userManagement.demo.model.Token;
import com.userManagement.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

/**
 * Data access for password-reset {@link Token}s.
 *
 * <p>The caller must SHA-256 hash a raw reset token before calling {@link #findByTokenHash(String)}.
 * The {@code deleteBy...} methods are derived delete queries and require an active transaction,
 * so the calling service must be annotated {@code @Transactional}.
 */
public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByTokenHash(String tokenHash);

    void deleteByUser(User user);

    void deleteByExpiresAtBefore(Instant cutoff);
}
