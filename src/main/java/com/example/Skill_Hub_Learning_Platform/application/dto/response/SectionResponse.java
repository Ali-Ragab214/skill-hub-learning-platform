package com.example.Skill_Hub_Learning_Platform.application.dto.response;

import java.util.List;

public record SectionResponse(
        Long id,
        String title,
        Integer orderIndex,
        List<LessonResponse> lessons
) {
}
