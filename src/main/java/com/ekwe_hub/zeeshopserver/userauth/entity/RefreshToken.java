package com.ekwe_hub.zeeshopserver.userauth.entity;

import com.ekwe_hub.zeeshopserver.shared.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A persisted, revocable refresh token — deliberately NOT a JWT.
 *
 * A self-contained JWT refresh token can't be invalidated on logout without a
 * server-side blocklist, which just re-introduces state anyway. Storing the
 * token (hashed) here lets logout delete the row outright, and lets a stolen
 * token be revoked without waiting for it to expire.
 *
 * tokenHash stores SHA-256 of the raw token, never the raw value, so a
 * database leak doesn't hand out valid refresh tokens directly.
 */
@Getter
@Setter
@Entity
@Table(name = "refresh_tokens")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken extends AuditableEntity {

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean revoked = false;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
