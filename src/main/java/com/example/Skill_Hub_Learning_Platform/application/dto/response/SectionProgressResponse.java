package com.example.Skill_Hub_Learning_Platform.application.dto.response;

public record SectionProgressResponse(
        Long sectionId,
        String sectionTitle,
        int completedLessons,
        int totalLessons
) {}
