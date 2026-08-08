package com.ekwe_hub.zeeshopserver.userauth.service;

import com.ekwe_hub.zeeshopserver.shared.api.exception.BusinessRuleViolationException;
import com.ekwe_hub.zeeshopserver.shared.api.exception.DuplicateResourceException;
import com.ekwe_hub.zeeshopserver.shared.api.exception.ResourceNotFoundException;
import com.ekwe_hub.zeeshopserver.userauth.dto.request.ChangePasswordRequest;
import com.ekwe_hub.zeeshopserver.userauth.dto.request.CreateUserRequest;
import com.ekwe_hub.zeeshopserver.userauth.dto.request.UpdateUserRequest;
import com.ekwe_hub.zeeshopserver.userauth.dto.response.UserResponse;
import com.ekwe_hub.zeeshopserver.userauth.entity.Role;
import com.ekwe_hub.zeeshopserver.userauth.entity.User;
import com.ekwe_hub.zeeshopserver.userauth.mapper.UserMapper;
import com.ekwe_hub.zeeshopserver.userauth.repository.RoleRepository;
import com.ekwe_hub.zeeshopserver.userauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Admin-facing CRUD for user accounts (as opposed to AuthService, which
 * handles login/refresh/logout for an already-existing user).
 *
 * Passwords are never stored or compared in plain text: every raw password
 * that enters here goes straight through PasswordEncoder (BCrypt, see
 * SecurityConfig) before touching the database.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    public UserResponse getUser(UUID id) {
        return userMapper.toResponse(findUser(id));
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("User", "username", request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("User", "email", request.email());
        }

        User user = userMapper.toEntity(
                request,
                passwordEncoder.encode(request.password()),
                findRole(request.roleId())
        );

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = findUser(id);

        if (userRepository.existsByUsernameAndIdNot(request.username(), id)) {
            throw new DuplicateResourceException("User", "username", request.username());
        }
        if (userRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new DuplicateResourceException("User", "email", request.email());
        }

        userMapper.updateEntity(request, findRole(request.roleId()), user);

        if (request.password() != null && !request.password().isBlank()) {
            user.updatePassword(passwordEncoder.encode(request.password()));
        }

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(UUID id) {
        userRepository.delete(findUser(id));
    }

    @Transactional
    public UserResponse activateUser(UUID id) {
        User user = findUser(id);
        user.activate();
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse deactivateUser(UUID id) {
        User user = findUser(id);
        user.deactivate();
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void changePassword(UUID id, ChangePasswordRequest request) {
        User user = findUser(id);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessRuleViolationException("Current password is incorrect");
        }

        user.updatePassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    private User findUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private Role findRole(String roleIdentifier) {
        try {
            UUID roleId = UUID.fromString(roleIdentifier);
            return roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));
        } catch (IllegalArgumentException e) {
            return roleRepository.findByName(roleIdentifier.toUpperCase())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + roleIdentifier));
        }
    }
}
