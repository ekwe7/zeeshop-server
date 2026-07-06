package com.ekwe_hub.zeeshopserver.userauth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateUserRequest(

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid address")
        String email,

        // Optional — leave blank/omitted to keep the current password
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,

        @NotNull(message = "Role is required")
        UUID roleId,

        @NotNull(message = "Enabled flag is required")
        Boolean enabled
) {
}
