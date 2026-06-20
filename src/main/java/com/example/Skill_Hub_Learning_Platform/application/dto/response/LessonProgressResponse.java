package com.example.Skill_Hub_Learning_Platform.application.dto.response;

import java.time.LocalDateTime;

public record LessonProgressResponse(
        Long id,
        Long lessonId,
        String lessonTitle,
        boolean completed,
        LocalDateTime completedAt
) {}
