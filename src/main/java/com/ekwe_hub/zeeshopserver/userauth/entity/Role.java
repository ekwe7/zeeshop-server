package com.ekwe_hub.zeeshopserver.userauth.entity;

import com.ekwe_hub.zeeshopserver.shared.domain.AuditableEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * A named collection of permissions, e.g. "ADMIN", "MANAGER", "STAFF".
 *
 * Kept as a String rather than an enum so new roles can be introduced by an
 * administrator without a code change/redeploy — only the Permission values
 * themselves are fixed, since those correspond to real authorization checks
 * in code.
 */
@Getter
@Setter
@Entity
@Table(name = "roles")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends AuditableEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "permission", nullable = false, length = 50)
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();
}
