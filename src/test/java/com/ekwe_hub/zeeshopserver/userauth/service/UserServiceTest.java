package com.ekwe_hub.zeeshopserver.userauth.service;

import com.ekwe_hub.zeeshopserver.shared.api.exception.DuplicateResourceException;
import com.ekwe_hub.zeeshopserver.shared.api.exception.ResourceNotFoundException;
import com.ekwe_hub.zeeshopserver.userauth.dto.request.CreateUserRequest;
import com.ekwe_hub.zeeshopserver.userauth.dto.request.UpdateUserRequest;
import com.ekwe_hub.zeeshopserver.userauth.dto.response.UserResponse;
import com.ekwe_hub.zeeshopserver.userauth.entity.Role;
import com.ekwe_hub.zeeshopserver.userauth.entity.User;
import com.ekwe_hub.zeeshopserver.userauth.mapper.UserMapper;
import com.ekwe_hub.zeeshopserver.userauth.repository.RoleRepository;
import com.ekwe_hub.zeeshopserver.userauth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests: UserRepository/RoleRepository/UserMapper/PasswordEncoder
 * are all mocked, no Spring context and no real persistence involved.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UUID userId;
    private UUID roleId;
    private Role role;
    private User user;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        roleId = UUID.randomUUID();

        role = Role.builder().name("STAFF").build();
        role.setId(roleId);

        user = User.builder()
                .username("jdoe")
                .email("jdoe@example.com")
                .password("hashed-password")
                .role(role)
                .enabled(true)
                .build();
        user.setId(userId);

        userResponse = UserResponse.builder()
                .id(userId)
                .username("jdoe")
                .email("jdoe@example.com")
                .roleName("STAFF")
                .enabled(true)
                .build();
    }

    @Test
    void getAllUsers_mapsEveryPersistedUser() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        List<UserResponse> result = userService.getAllUsers();

        assertThat(result).containsExactly(userResponse);
    }

    @Test
    void getUser_returnsMappedResponse_whenUserExists() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.getUser(userId);

        assertThat(result).isEqualTo(userResponse);
    }

    @Test
    void getUser_throwsResourceNotFound_whenUserMissing() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createUser_hashesPasswordAndPersists_whenUsernameAndEmailAreFree() {
        CreateUserRequest request = new CreateUserRequest("jdoe", "jdoe@example.com", "plain-password", roleId, true);

        when(userRepository.existsByUsername("jdoe")).thenReturn(false);
        when(userRepository.existsByEmail("jdoe@example.com")).thenReturn(false);
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("plain-password")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        UserResponse result = userService.createUser(request);

        assertThat(result).isEqualTo(userResponse);

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertThat(savedUser.getValue().getPassword()).isEqualTo("hashed-password");
        assertThat(savedUser.getValue().getRole()).isEqualTo(role);
        assertThat(savedUser.getValue().isEnabled()).isTrue();
    }

    @Test
    void createUser_defaultsEnabledToTrue_whenNotSpecified() {
        CreateUserRequest request = new CreateUserRequest("jdoe", "jdoe@example.com", "plain-password", roleId, null);

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        userService.createUser(request);

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertThat(savedUser.getValue().isEnabled()).isTrue();
    }

    @Test
    void createUser_throwsDuplicateResource_whenUsernameTaken() {
        CreateUserRequest request = new CreateUserRequest("jdoe", "jdoe@example.com", "plain-password", roleId, true);
        when(userRepository.existsByUsername("jdoe")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_throwsDuplicateResource_whenEmailTaken() {
        CreateUserRequest request = new CreateUserRequest("jdoe", "jdoe@example.com", "plain-password", roleId, true);
        when(userRepository.existsByUsername("jdoe")).thenReturn(false);
        when(userRepository.existsByEmail("jdoe@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_throwsResourceNotFound_whenRoleMissing() {
        CreateUserRequest request = new CreateUserRequest("jdoe", "jdoe@example.com", "plain-password", roleId, true);
        when(userRepository.existsByUsername("jdoe")).thenReturn(false);
        when(userRepository.existsByEmail("jdoe@example.com")).thenReturn(false);
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_updatesFieldsAndRehashesPassword_whenPasswordSupplied() {
        UpdateUserRequest request = new UpdateUserRequest("jdoe2", "jdoe2@example.com", "new-password", roleId, false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameAndIdNot("jdoe2", userId)).thenReturn(false);
        when(userRepository.existsByEmailAndIdNot("jdoe2@example.com", userId)).thenReturn(false);
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("new-password")).thenReturn("new-hashed-password");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        userService.updateUser(userId, request);

        assertThat(user.getUsername()).isEqualTo("jdoe2");
        assertThat(user.getEmail()).isEqualTo("jdoe2@example.com");
        assertThat(user.isEnabled()).isFalse();
        assertThat(user.getPassword()).isEqualTo("new-hashed-password");
    }

    @Test
    void updateUser_keepsExistingPassword_whenPasswordOmitted() {
        UpdateUserRequest request = new UpdateUserRequest("jdoe", "jdoe@example.com", null, roleId, true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameAndIdNot("jdoe", userId)).thenReturn(false);
        when(userRepository.existsByEmailAndIdNot("jdoe@example.com", userId)).thenReturn(false);
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        userService.updateUser(userId, request);

        assertThat(user.getPassword()).isEqualTo("hashed-password");
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void updateUser_throwsResourceNotFound_whenUserMissing() {
        UpdateUserRequest request = new UpdateUserRequest("jdoe", "jdoe@example.com", null, roleId, true);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(userId, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateUser_throwsDuplicateResource_whenUsernameTakenBySomeoneElse() {
        UpdateUserRequest request = new UpdateUserRequest("taken", "jdoe@example.com", null, roleId, true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameAndIdNot("taken", userId)).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(userId, request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_throwsDuplicateResource_whenEmailTakenBySomeoneElse() {
        UpdateUserRequest request = new UpdateUserRequest("jdoe", "taken@example.com", null, roleId, true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameAndIdNot("jdoe", userId)).thenReturn(false);
        when(userRepository.existsByEmailAndIdNot("taken@example.com", userId)).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(userId, request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_throwsResourceNotFound_whenRoleMissing() {
        UUID missingRoleId = UUID.randomUUID();
        UpdateUserRequest request = new UpdateUserRequest("jdoe", "jdoe@example.com", null, missingRoleId, true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameAndIdNot("jdoe", userId)).thenReturn(false);
        when(userRepository.existsByEmailAndIdNot("jdoe@example.com", userId)).thenReturn(false);
        when(roleRepository.findById(missingRoleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(userId, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_deletesUser_whenUserExists() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.deleteUser(userId);

        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void deleteUser_throwsResourceNotFound_whenUserMissing() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).delete(any());
    }
}
