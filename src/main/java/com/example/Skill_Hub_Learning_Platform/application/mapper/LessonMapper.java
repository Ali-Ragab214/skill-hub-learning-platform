package com.example.Skill_Hub_Learning_Platform.application.mapper;

import com.example.Skill_Hub_Learning_Platform.application.dto.request.LessonRequest;
import com.example.Skill_Hub_Learning_Platform.application.dto.response.LessonResponse;
import com.example.Skill_Hub_Learning_Platform.domain.models.Lesson;
import com.example.Skill_Hub_Learning_Platform.domain.models.Section;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
public class LessonMapper {

    public LessonResponse toResponse(Lesson lesson) {
        return new LessonResponse(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getVideoUrl(),
                lesson.getDuration(),
                lesson.getIsPreview(),
                lesson.getOrderIndex(),
                lesson.getSection().getId()
        );
    }

    public Lesson toEntity(LessonRequest request, Section section) {
        return Lesson.builder()
                .title(request.getTitle())
                .videoUrl(request.getVideoUrl())
                .duration(request.getDuration())
                .isPreview(request.getIsPreview())
                .orderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0)
                .section(section)
                .build();
    }
}
