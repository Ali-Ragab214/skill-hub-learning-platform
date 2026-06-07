package com.example.Skill_Hub_Learning_Platform.application.mapper;

import com.example.Skill_Hub_Learning_Platform.application.dto.request.SectionRequest;
import com.example.Skill_Hub_Learning_Platform.application.dto.response.SectionResponse;
import com.example.Skill_Hub_Learning_Platform.domain.models.Section;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SectionMapper {

    private final LessonMapper lessonMapper;
    public Section toEntity(SectionRequest request)
    {
        return  Section.builder()
                .title(request.getTitle())
                .orderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0)
                .build();
    }

    public SectionResponse toResponse(Section section) {
        return new SectionResponse(
                section.getId(),
                section.getTitle(),
                section.getOrderIndex(),
                section.getLessons() == null
                        ? List.of()
                        : section.getLessons().stream()
                        .map(lessonMapper::toResponse)
                        .toList()
        );
    }

}
