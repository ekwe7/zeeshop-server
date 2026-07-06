package com.ekwe_hub.zeeshopserver.userauth.controller;

import com.ekwe_hub.zeeshopserver.shared.api.response.ApiResponse;
import com.ekwe_hub.zeeshopserver.userauth.dto.request.CreateUserRequest;
import com.ekwe_hub.zeeshopserver.userauth.dto.request.UpdateUserRequest;
import com.ekwe_hub.zeeshopserver.userauth.dto.response.UserResponse;
import com.ekwe_hub.zeeshopserver.userauth.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests for the HTTP layer: UserService is mocked, so these only
 * verify that UserController delegates correctly and shapes the
 * ResponseEntity/ApiResponse as expected. No Spring context, no MockMvc,
 * no security filter chain — authorization (@PreAuthorize) is Spring
 * Security's concern, not the controller's.
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UUID userId;
    private UUID roleId;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        roleId = UUID.randomUUID();
        userResponse = UserResponse.builder()
                .id(userId)
                .username("jdoe")
                .email("jdoe@example.com")
                .roleName("STAFF")
                .enabled(true)
                .build();
    }

    @Test
    void getAllUsers_returnsOkWithServiceResult() {
        when(userService.getAllUsers()).thenReturn(List.of(userResponse));

        ResponseEntity<ApiResponse<List<UserResponse>>> response = userController.getAllUsers();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).containsExactly(userResponse);
    }

    @Test
    void getUser_returnsOkWithServiceResult() {
        when(userService.getUser(userId)).thenReturn(userResponse);

        ResponseEntity<ApiResponse<UserResponse>> response = userController.getUser(userId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(userResponse);
    }

    @Test
    void createUser_returnsCreatedWithServiceResult() {
        CreateUserRequest request = new CreateUserRequest("jdoe", "jdoe@example.com", "plain-password", roleId, true);
        when(userService.createUser(request)).thenReturn(userResponse);

        ResponseEntity<ApiResponse<UserResponse>> response = userController.createUser(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(userResponse);
        verify(userService).createUser(request);
    }

    @Test
    void updateUser_returnsOkWithServiceResult() {
        UpdateUserRequest request = new UpdateUserRequest("jdoe", "jdoe@example.com", null, roleId, true);
        when(userService.updateUser(userId, request)).thenReturn(userResponse);

        ResponseEntity<ApiResponse<UserResponse>> response = userController.updateUser(userId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(userResponse);
        verify(userService).updateUser(userId, request);
    }

    @Test
    void deleteUser_returnsOkAndDelegatesToService() {
        ResponseEntity<ApiResponse<Void>> response = userController.deleteUser(userId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        verify(userService).deleteUser(userId);
    }
}
