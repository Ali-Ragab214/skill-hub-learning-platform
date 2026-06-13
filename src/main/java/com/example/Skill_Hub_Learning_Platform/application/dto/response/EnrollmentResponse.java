package com.example.Skill_Hub_Learning_Platform.application.dto.response;

import java.time.LocalDateTime;

public record EnrollmentResponse(
        Long id ,
        Long courseId,
        String courseTitle,
        Integer progress,
        LocalDateTime enrolledAt
) {
}
