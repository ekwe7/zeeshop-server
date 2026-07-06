package com.ekwe_hub.zeeshopserver.userauth;

import com.ekwe_hub.zeeshopserver.userauth.dto.request.LoginRequest;
import com.ekwe_hub.zeeshopserver.userauth.dto.request.TokenRequest;
import com.ekwe_hub.zeeshopserver.userauth.dto.response.AuthResponse;
import com.ekwe_hub.zeeshopserver.userauth.entity.Permission;
import com.ekwe_hub.zeeshopserver.userauth.entity.Role;
import com.ekwe_hub.zeeshopserver.userauth.entity.User;
import com.ekwe_hub.zeeshopserver.userauth.repository.RefreshTokenRepository;
import com.ekwe_hub.zeeshopserver.userauth.repository.RoleRepository;
import com.ekwe_hub.zeeshopserver.userauth.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end coverage for login, JWT issuance/validation, refresh rotation, and logout —
 * against the real security filter chain and an in-memory H2 database (see
 * application-test.yml), not mocks.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "Test1234!"; // seeded, see application-test.yml
    private static final String PROTECTED_PATH = "/api/does-not-exist-but-requires-auth";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void loginWithCorrectCredentialsReturnsAccessAndRefreshTokens() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(ADMIN_USERNAME, ADMIN_PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    void loginWithWrongPasswordIsRejected() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(ADMIN_USERNAME, "wrong-password"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void loginWithUnknownUsernameIsRejectedWithoutRevealingWhichFieldWasWrong() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("no-such-user", "whatever"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessTokenAuthenticatesRequestsToProtectedRoutes() throws Exception {
        String accessToken = login(ADMIN_USERNAME, ADMIN_PASSWORD).accessToken();

        // No matching handler exists at PROTECTED_PATH, so a 404 (not 401/403) proves
        // the security filter chain accepted the token and let the request through.
        mockMvc.perform(get(PROTECTED_PATH).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void protectedRouteRejectsRequestsWithNoToken() throws Exception {
        int status = mockMvc.perform(get(PROTECTED_PATH))
                .andExpect(status().is4xxClientError())
                .andReturn().getResponse().getStatus();

        assertNotEquals(404, status);
    }

    @Test
    void protectedRouteRejectsATamperedToken() throws Exception {
        String accessToken = login(ADMIN_USERNAME, ADMIN_PASSWORD).accessToken();

        int status = mockMvc.perform(get(PROTECTED_PATH).header("Authorization", "Bearer " + accessToken + "tampered"))
                .andExpect(status().is4xxClientError())
                .andReturn().getResponse().getStatus();

        assertNotEquals(404, status);
    }

    @Test
    void refreshRotatesTheRefreshTokenAndInvalidatesThePreviousOne() throws Exception {
        String originalRefreshToken = login(ADMIN_USERNAME, ADMIN_PASSWORD).refreshToken();

        AuthResponse refreshed = refresh(originalRefreshToken);
        assertNotEquals(originalRefreshToken, refreshed.refreshToken());

        // the rotated-out token is now dead
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TokenRequest(originalRefreshToken))))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void logoutRevokesTheRefreshTokenButLeavesTheUnexpiredAccessTokenUsable() throws Exception {
        AuthResponse tokens = login(ADMIN_USERNAME, ADMIN_PASSWORD);

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TokenRequest(tokens.refreshToken()))))
                .andExpect(status().isOk());

        // refresh token is dead post-logout
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TokenRequest(tokens.refreshToken()))))
                .andExpect(status().isUnprocessableEntity());

        // the access token is short-lived and stateless, so it is deliberately NOT
        // revoked by logout — this is a known tradeoff, not a bug (see JwtService).
        mockMvc.perform(get(PROTECTED_PATH).header("Authorization", "Bearer " + tokens.accessToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void logoutIsIdempotentOnAnAlreadyRevokedToken() throws Exception {
        AuthResponse tokens = login(ADMIN_USERNAME, ADMIN_PASSWORD);
        TokenRequest body = new TokenRequest(tokens.refreshToken());

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        // logging out again with the same (already-revoked) token still succeeds
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    void logoutAllRevokesEveryRefreshTokenForTheUserNotJustOne() throws Exception {
        AuthResponse sessionOne = login(ADMIN_USERNAME, ADMIN_PASSWORD);
        AuthResponse sessionTwo = login(ADMIN_USERNAME, ADMIN_PASSWORD);

        mockMvc.perform(post("/api/auth/logout-all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TokenRequest(sessionOne.refreshToken()))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TokenRequest(sessionOne.refreshToken()))))
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TokenRequest(sessionTwo.refreshToken()))))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void aValidAccessTokenForAUserDeletedAfterIssuanceIsRejectedNotServerError() throws Exception {
        Role role = roleRepository.findByName("STAFF")
                .orElseGet(() -> roleRepository.save(Role.builder().name("STAFF").permissions(Set.of(Permission.SALES_READ)).build()));

        User throwaway = userRepository.save(User.builder()
                .username("throwaway-user")
                .email("throwaway@example.com")
                .password(passwordEncoder.encode("Throwaway1!"))
                .role(role)
                .enabled(true)
                .build());

        String accessToken = login("throwaway-user", "Throwaway1!").accessToken();

        refreshTokenRepository.deleteByUserId(throwaway.getId());
        userRepository.delete(throwaway);
        userRepository.flush();

        // must not surface as a 500 — the filter should treat this as unauthenticated
        mockMvc.perform(get(PROTECTED_PATH).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().is4xxClientError());
    }

    private AuthResponse login(String usernameOrEmail, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(usernameOrEmail, password))))
                .andExpect(status().isOk())
                .andReturn();
        return extractData(result);
    }

    private AuthResponse refresh(String refreshToken) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TokenRequest(refreshToken))))
                .andExpect(status().isOk())
                .andReturn();
        return extractData(result);
    }

    private AuthResponse extractData(MvcResult result) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return objectMapper.treeToValue(root.get("data"), AuthResponse.class);
    }
}
