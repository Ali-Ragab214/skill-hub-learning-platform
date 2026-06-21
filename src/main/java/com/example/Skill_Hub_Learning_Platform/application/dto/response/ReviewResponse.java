package com.example.Skill_Hub_Learning_Platform.application.dto.response;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Long courseId,
        String courseTitle,
        Long studentId,
        String studentName,
        Integer rating,
        String comment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
