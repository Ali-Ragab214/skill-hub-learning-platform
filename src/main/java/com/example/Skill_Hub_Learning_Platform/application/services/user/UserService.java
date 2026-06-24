package com.example.Skill_Hub_Learning_Platform.application.services.user;

import com.example.Skill_Hub_Learning_Platform.application.dto.response.UserResponse;
import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();
    void deleteUser(Long id);
}
