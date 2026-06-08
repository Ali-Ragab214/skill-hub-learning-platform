package com.example.Skill_Hub_Learning_Platform.application.services.section;

import com.example.Skill_Hub_Learning_Platform.application.dto.request.SectionRequest;
import com.example.Skill_Hub_Learning_Platform.application.dto.response.SectionResponse;
import com.example.Skill_Hub_Learning_Platform.application.responses.PaginationResponse;

import java.util.List;

public interface SectionService {
    SectionResponse createSection(Long courseId, SectionRequest request, String instructorEmail);
    SectionResponse getSectionById(Long courseId, Long id);
    SectionResponse getSectionByTitleAndCourseId(String name, Long courseId);
    PaginationResponse<SectionResponse> getSectionsByCourse(Long courseId,int page , int size);
    SectionResponse updateSection(Long courseId,Long id, SectionRequest request, String instructorEmail);
    void deleteSection(Long courseId ,Long id, String instructorEmail);
}
