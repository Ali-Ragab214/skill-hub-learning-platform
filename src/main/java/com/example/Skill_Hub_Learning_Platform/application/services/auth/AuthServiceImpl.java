package com.example.Skill_Hub_Learning_Platform.application.services.auth;

import com.example.Skill_Hub_Learning_Platform.application.dto.request.LoginRequest;
import com.example.Skill_Hub_Learning_Platform.application.dto.request.RegisterRequest;
import com.example.Skill_Hub_Learning_Platform.application.dto.response.AuthResponse;
import com.example.Skill_Hub_Learning_Platform.application.exceptions.BadRequestException;
import com.example.Skill_Hub_Learning_Platform.application.exceptions.DuplicateEmailException;
import com.example.Skill_Hub_Learning_Platform.application.mapper.UserMapper;
import com.example.Skill_Hub_Learning_Platform.application.security.JwtService;
import com.example.Skill_Hub_Learning_Platform.domain.enums.Role;
import com.example.Skill_Hub_Learning_Platform.domain.models.User;
import com.example.Skill_Hub_Learning_Platform.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Starting registration process for email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email already exists - {}", request.getEmail());
            throw new DuplicateEmailException("Email already registered. Please use a different email or try logging in.");
        }

        Role role;
        if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
            try {
                role = Role.valueOf(request.getRole());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid role provided: {}", request.getRole());
                throw new BadRequestException(
                        "Invalid role: '" + request.getRole() + "'. Accepted values are: Student, Instructor, Admin"
                );
            }
        } else {
            role = Role.Student;
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        var user = User.builder()
                .name(request.getName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .password(encodedPassword)
                .role(role)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getId());

        var token = jwtService.generateToken(savedUser);
        var userResponse = userMapper.toUserResponse(savedUser);

        return AuthResponse.builder()
                .token(token)
                .user(userResponse)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Starting login process for email: {}", request.getEmail());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            log.warn("Login failed for email: {} - Invalid credentials", request.getEmail());
            throw new BadRequestException("Invalid email or password");
        }

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("User not found after successful authentication: {}", request.getEmail());
                    return new BadRequestException("Invalid email or password");
                });

        if (!user.isEnabled()) {
            log.warn("Login attempted for disabled account: {}", request.getEmail());
            throw new BadRequestException("Account is disabled. Please contact support.");
        }

        var token = jwtService.generateToken(user);
        var userResponse = userMapper.toUserResponse(user);

        log.info("User logged in successfully: {}", user.getId());

        return AuthResponse.builder()
                .token(token)
                .user(userResponse)
                .build();
    }
}
