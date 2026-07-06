package com.ekwe_hub.zeeshopserver.userauth.dto.request;

import jakarta.validation.constraints.NotBlank;

/** Shared body shape for /refresh and /logout — both just need the raw refresh token. */
public record TokenRequest(

        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {
}
