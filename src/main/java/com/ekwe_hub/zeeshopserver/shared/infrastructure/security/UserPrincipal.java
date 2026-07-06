package com.ekwe_hub.zeeshopserver.shared.infrastructure.security;

import com.ekwe_hub.zeeshopserver.userauth.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Adapts the plain User entity to Spring Security's UserDetails contract.
 *
 * Authorities are the role name prefixed with "ROLE_" (required by Spring
 * Security's hasRole() checks) plus one authority per Permission on that role
 * (so hasAuthority("SALES_WRITE") works directly without a "ROLE_" prefix).
 */
@Getter
public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final String username;
    private final String email;
    private final String password;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.enabled = user.isEnabled();
        this.authorities = buildAuthorities(user);
    }

    private static Set<GrantedAuthority> buildAuthorities(User user) {
        Stream<GrantedAuthority> roleAuthority = Stream.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()));

        Stream<GrantedAuthority> permissionAuthorities = user.getRole().getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority(permission.name()));

        return Stream.concat(roleAuthority, permissionAuthorities)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
