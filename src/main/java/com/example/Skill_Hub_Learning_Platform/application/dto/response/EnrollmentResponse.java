package com.example.Skill_Hub_Learning_Platform.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record EnrollmentResponse(
        Long id,
        Long courseId,
        String courseTitle,
        Integer progress,
        LocalDateTime enrolledAt,
        Integer completedLessons,
        Integer totalLessons,
        List<SectionProgressResponse> sectionProgress
) {
    public EnrollmentResponse(Long id, Long courseId, String courseTitle, Integer progress, LocalDateTime enrolledAt) {
        this(id, courseId, courseTitle, progress, enrolledAt, 0, 0, List.of());
    }
}
