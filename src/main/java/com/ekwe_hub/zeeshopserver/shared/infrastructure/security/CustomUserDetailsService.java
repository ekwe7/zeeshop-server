package com.ekwe_hub.zeeshopserver.shared.infrastructure.security;

import com.ekwe_hub.zeeshopserver.userauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Looks up a User by username or email — AuthService passes whatever the
 * client submitted as the login identifier and this resolves either.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        return userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .map(UserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with username/email: " + usernameOrEmail));
    }
}
