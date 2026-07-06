package com.ekwe_hub.zeeshopserver.shared.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Superclass for every JPA entity in ZeeShop.
 *
 * Provides two things automatically:
 *   1. A UUID primary key — avoids exposing sequential IDs in URLs, which leaks business data
 *      (e.g. competitors counting how many orders you have).
 *   2. Audit columns (created/updated by whom and when) — required for traceability and
 *      compliance. Spring's AuditingEntityListener fills these in transparently so no
 *      domain code has to touch them.
 *
 * The auditor identity comes from SystemAuditorAware. Once Spring Security is wired in,
 * swap that class to read from the security context and these columns auto-populate with
 * the authenticated user's identifier.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity {

    // UUID avoids sequential ID guessing and works cleanly across distributed systems
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Stores the authenticated user's identifier (username / email / sub claim)
    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 100)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
}
