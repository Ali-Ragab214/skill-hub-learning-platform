package com.example.Skill_Hub_Learning_Platform.api.controller;

import com.example.Skill_Hub_Learning_Platform.application.dto.request.LoginRequest;
import com.example.Skill_Hub_Learning_Platform.application.dto.request.RegisterRequest;
import com.example.Skill_Hub_Learning_Platform.application.dto.response.AuthResponse;
import com.example.Skill_Hub_Learning_Platform.application.dto.response.UserResponse;
import com.example.Skill_Hub_Learning_Platform.application.services.auth.AuthService;
import com.example.Skill_Hub_Learning_Platform.application.security.JwtService;
import com.example.Skill_Hub_Learning_Platform.application.security.CustomUserDetailsService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private org.springframework.cache.CacheManager cacheManager;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .name("Alice")
                .email("alice@example.com")
                .password("Password123!")
                .role("Student")
                .build();

        loginRequest = LoginRequest.builder()
                .email("alice@example.com")
                .password("Password123!")
                .build();

        authResponse = AuthResponse.builder()
                .token("dummy-jwt-token")
                .user(UserResponse.builder()
                        .id(1L)
                        .name("Alice")
                        .email("alice@example.com")
                        .role("Student")
                        .build())
                .build();
    }

    @Test
    void register_ShouldReturn201_WhenRequestIsValid() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Registration successful"))
                .andExpect(jsonPath("$.data.token").value("dummy-jwt-token"))
                .andExpect(jsonPath("$.data.user.email").value("alice@example.com"));
    }

    @Test
    void login_ShouldReturn200_WhenCredentialsAreValid() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.token").value("dummy-jwt-token"));
    }
}
