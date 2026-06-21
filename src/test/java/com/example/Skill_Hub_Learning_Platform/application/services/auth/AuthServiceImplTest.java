package com.example.Skill_Hub_Learning_Platform.application.services.auth;

import com.example.Skill_Hub_Learning_Platform.application.dto.request.LoginRequest;
import com.example.Skill_Hub_Learning_Platform.application.dto.request.RegisterRequest;
import com.example.Skill_Hub_Learning_Platform.application.dto.response.AuthResponse;
import com.example.Skill_Hub_Learning_Platform.application.dto.response.UserResponse;
import com.example.Skill_Hub_Learning_Platform.application.exceptions.BadRequestException;
import com.example.Skill_Hub_Learning_Platform.application.exceptions.DuplicateEmailException;
import com.example.Skill_Hub_Learning_Platform.application.mapper.UserMapper;
import com.example.Skill_Hub_Learning_Platform.application.security.JwtService;
import com.example.Skill_Hub_Learning_Platform.domain.enums.Role;
import com.example.Skill_Hub_Learning_Platform.domain.models.User;
import com.example.Skill_Hub_Learning_Platform.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .name("Test User")
                .email("test@example.com")
                .password("StrongPass123!")
                .role("Student")
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("StrongPass123!")
                .build();

        user = User.builder()
                .name("Test User")
                .email("test@example.com")
                .password("encodedPassword")
                .role(Role.Student)
                .enabled(true)
                .build();
        user.setId(1L);

        userResponse = UserResponse.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .role("Student")
                .build();
    }

    @Test
    void register_Success() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
        when(userMapper.toUserResponse(any(User.class))).thenReturn(userResponse);

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("test@example.com", response.getUser().getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_ThrowsDuplicateEmailException() {
        when(userRepository.existsByEmail(any())).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_ThrowsBadRequestException_ForInvalidRole() {
        registerRequest.setRole("INVALID_ROLE");

        assertThrows(BadRequestException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_Success() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any())).thenReturn("jwt-token");
        when(userMapper.toUserResponse(any())).thenReturn(userResponse);

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("test@example.com", response.getUser().getEmail());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_ThrowsBadRequestException_OnFailedAuthentication() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new AuthenticationException("Invalid credentials") {});

        assertThrows(BadRequestException.class, () -> authService.login(loginRequest));
    }

    @Test
    void login_ThrowsBadRequestException_WhenUserDisabled() {
        user.setEnabled(false);
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> authService.login(loginRequest));
    }
}
