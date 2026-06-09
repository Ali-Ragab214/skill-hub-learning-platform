package com.example.Skill_Hub_Learning_Platform.application.services.lesson;

import com.example.Skill_Hub_Learning_Platform.application.dto.request.LessonRequest;
import com.example.Skill_Hub_Learning_Platform.application.dto.response.LessonResponse;
import com.example.Skill_Hub_Learning_Platform.application.responses.PaginationResponse;

import java.util.List;

public interface LessonService {

    LessonResponse createLesson(Long courseId, Long sectionId, LessonRequest request, String instructorEmail);
    LessonResponse getLessonById(Long courseId, Long sectionId, Long id);
    PaginationResponse<LessonResponse> getLessonsBySection(Long courseId, Long sectionId, int page, int size);
    List<LessonResponse> getPreviewLessonsBySection(Long courseId, Long sectionId);
    LessonResponse updateLesson(Long courseId, Long sectionId, Long id, LessonRequest request, String instructorEmail);
    void deleteLesson(Long courseId, Long sectionId, Long id, String instructorEmail);
}
