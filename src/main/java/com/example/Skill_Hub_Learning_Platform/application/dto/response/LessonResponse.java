package com.example.Skill_Hub_Learning_Platform.application.dto.response;

public record LessonResponse(
        Long id,
        String title,
        String videoUrl,
        Integer duration,
        Boolean isPreview,
        Integer orderIndex
) {}
