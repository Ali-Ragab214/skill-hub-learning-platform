package com.example.Skill_Hub_Learning_Platform.application.services.lesson;

import com.example.Skill_Hub_Learning_Platform.application.dto.request.LessonRequest;
import com.example.Skill_Hub_Learning_Platform.application.dto.response.LessonResponse;
import com.example.Skill_Hub_Learning_Platform.application.exceptions.DuplicateResourceException;
import com.example.Skill_Hub_Learning_Platform.application.exceptions.ResourceNotFoundException;
import com.example.Skill_Hub_Learning_Platform.application.exceptions.UnauthorizedException;
import com.example.Skill_Hub_Learning_Platform.application.mapper.LessonMapper;
import com.example.Skill_Hub_Learning_Platform.application.responses.PaginationResponse;
import com.example.Skill_Hub_Learning_Platform.domain.models.Lesson;
import com.example.Skill_Hub_Learning_Platform.domain.models.Section;
import com.example.Skill_Hub_Learning_Platform.infrastructure.repository.LessonRepository;
import com.example.Skill_Hub_Learning_Platform.infrastructure.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LessonServiceImpl  implements  LessonService{
    private  final LessonRepository lessonRepository;
    private final LessonMapper lessonMapper;
    private final SectionRepository sectionRepository;


    @Override
    public LessonResponse createLesson(Long courseId, Long sectionId, LessonRequest request, String instructorEmail) {
        Section section = sectionRepository.findByIdAndCourseId(sectionId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found in this course"));
        validateOwnership(section,instructorEmail);
        validateUniqueTitleInSection(request.getTitle(), sectionId, null);
        Lesson lesson = lessonMapper.toEntity(request,section);
        lesson = lessonRepository.save(lesson);
        return lessonMapper.toResponse(lesson);
    }

    @Transactional(readOnly = true)
    @Override
    public LessonResponse getLessonById(Long courseId, Long sectionId, Long id) {
        Lesson lesson = lessonRepository.findByIdAndSectionIdAndSectionCourseId(id, sectionId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found in this section and course"));

        return lessonMapper.toResponse(lesson);
    }

    @Transactional(readOnly = true)
    @Override
    public PaginationResponse<LessonResponse> getLessonsBySection(Long courseId, Long sectionId, int page, int size) {
        Page<LessonResponse> lessonpage = lessonRepository.findBySectionIdAndSectionCourseId(sectionId, courseId, PageRequest.of(page, size))
                .map(lessonMapper::toResponse);

        return new PaginationResponse<>(
                lessonpage.getContent(),
                lessonpage.getNumber(),
                lessonpage.getSize(),
                lessonpage.getTotalElements(),
                lessonpage.getTotalPages(),
                lessonpage.isLast(),
                lessonpage.isFirst()
        );
    }

    @Transactional(readOnly = true)
    @Override
    public List<LessonResponse> getPreviewLessonsBySection(Long courseId, Long sectionId) {

        if (sectionRepository.findByIdAndCourseId(sectionId, courseId).isEmpty()) {
            throw new ResourceNotFoundException("Section not found in this course");
        }

        return lessonRepository.findBySectionIdAndIsPreview(sectionId, true)
                .stream()
                .map(lessonMapper::toResponse)
                .toList();
    }

    @Override
    public LessonResponse updateLesson(Long courseId, Long sectionId, Long id, LessonRequest request, String instructorEmail) {

        Lesson lesson = lessonRepository.findByIdAndSectionIdAndSectionCourseId(id, sectionId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found in this section and course"));

        validateOwnership(lesson.getSection(), instructorEmail);
        validateUniqueTitleInSection(request.getTitle(), sectionId, id);

        lesson.setTitle(request.getTitle());
        lesson.setVideoUrl(request.getVideoUrl());
        lesson.setDuration(request.getDuration());
        lesson.setIsPreview(request.getIsPreview() != null ? request.getIsPreview() : false);
        lesson.setOrderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0);

        return lessonMapper.toResponse(lessonRepository.save(lesson));
    }



    @Override
    public void deleteLesson(Long courseId, Long sectionId, Long id, String instructorEmail) {

        Lesson lesson = lessonRepository.findByIdAndSectionIdAndSectionCourseId(id, sectionId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found in this section and course"));
        validateOwnership(lesson.getSection(), instructorEmail);
        lessonRepository.delete(lesson);
    }




    //validate that the instructor owns the course that the section belongs to
     private void validateOwnership(Section section, String instructorEmail) {
        if (!section.getCourse().getInstructor().getEmail().equals(instructorEmail)) {
            throw new UnauthorizedException("You are not authorized to modify this lesson");
        }
    }

    private void validateUniqueTitleInSection(String title, Long sectionId, Long excludeLessonId) {
        boolean exists;

        if (excludeLessonId == null) {
            exists = lessonRepository.existsByTitleAndSectionId(title, sectionId);
        } else {
            exists = lessonRepository.existsByTitleAndSectionIdAndIdNot(title, sectionId, excludeLessonId);
        }

        if (exists) {
            throw new DuplicateResourceException("Lesson with this title already exists in this section");
        }
    }



}
