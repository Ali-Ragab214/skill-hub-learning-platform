package com.example.Skill_Hub_Learning_Platform.application.services.auth;

import com.example.Skill_Hub_Learning_Platform.application.dto.request.LoginRequest;
import com.example.Skill_Hub_Learning_Platform.application.dto.request.RegisterRequest;
import com.example.Skill_Hub_Learning_Platform.application.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
