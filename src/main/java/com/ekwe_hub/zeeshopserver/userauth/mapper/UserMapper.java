package com.ekwe_hub.zeeshopserver.userauth.mapper;

import com.ekwe_hub.zeeshopserver.userauth.dto.request.CreateUserRequest;
import com.ekwe_hub.zeeshopserver.userauth.dto.request.UpdateUserRequest;
import com.ekwe_hub.zeeshopserver.userauth.dto.response.UserResponse;
import com.ekwe_hub.zeeshopserver.userauth.entity.Role;
import com.ekwe_hub.zeeshopserver.userauth.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(CreateUserRequest request, String encodedPassword, Role role) {
        return User.builder()
                .username(request.username())
                .email(request.email())
                .password(encodedPassword)
                .role(role)
                .enabled(request.enabled() == null || request.enabled())
                .build();
    }

    public void updateEntity(UpdateUserRequest request, Role role, User user) {
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setRole(role);
        user.setEnabled(request.enabled());
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roleName(user.getRole().getName())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
