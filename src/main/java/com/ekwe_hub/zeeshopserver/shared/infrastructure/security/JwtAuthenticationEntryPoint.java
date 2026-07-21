package com.ekwe_hub.zeeshopserver.shared.infrastructure.security;

import com.ekwe_hub.zeeshopserver.shared.api.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Without this, Spring Security has no AuthenticationEntryPoint registered
 * (this API uses neither httpBasic() nor formLogin()) and falls back to
 * Http403ForbiddenEntryPoint, which returns a bare 403 for a missing/invalid/
 * expired JWT — indistinguishable from an authenticated-but-forbidden
 * request. This entry point restores the intended 401 and shapes the body
 * like every other error response (see GlobalExceptionHandler), even though
 * it fires from the security filter chain rather than a controller, so it
 * cannot be a @RestControllerAdvice handler.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException authException) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(
                ApiResponse.error("Authentication is required to access this resource")));
    }
}
