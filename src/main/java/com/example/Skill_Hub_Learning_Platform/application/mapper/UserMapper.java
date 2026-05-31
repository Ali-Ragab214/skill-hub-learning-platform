package com.example.Skill_Hub_Learning_Platform.application.mapper;

import com.example.Skill_Hub_Learning_Platform.application.dto.response.UserResponse;
import com.example.Skill_Hub_Learning_Platform.domain.models.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
