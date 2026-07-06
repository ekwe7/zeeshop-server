package com.ekwe_hub.zeeshopserver.userauth.repository;

import com.ekwe_hub.zeeshopserver.userauth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    void deleteByUserId(UUID userId);
}
