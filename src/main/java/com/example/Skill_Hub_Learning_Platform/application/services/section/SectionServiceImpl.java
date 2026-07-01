package com.example.Skill_Hub_Learning_Platform.application.services.section;

import com.example.Skill_Hub_Learning_Platform.application.dto.request.SectionRequest;
import com.example.Skill_Hub_Learning_Platform.application.dto.response.SectionResponse;
import com.example.Skill_Hub_Learning_Platform.application.exceptions.ResourceNotFoundException;
import com.example.Skill_Hub_Learning_Platform.application.exceptions.UnauthorizedException;
import com.example.Skill_Hub_Learning_Platform.application.mapper.SectionMapper;
import com.example.Skill_Hub_Learning_Platform.application.responses.PaginationResponse;
import com.example.Skill_Hub_Learning_Platform.domain.models.Course;
import com.example.Skill_Hub_Learning_Platform.domain.models.Section;
import com.example.Skill_Hub_Learning_Platform.infrastructure.repository.CourseRepository;
import com.example.Skill_Hub_Learning_Platform.infrastructure.repository.LessonRepository;
import com.example.Skill_Hub_Learning_Platform.infrastructure.repository.SectionRepository;
import com.example.Skill_Hub_Learning_Platform.application.cache.CacheConstants;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SectionServiceImpl implements SectionService {
    private  final SectionRepository sectionRepository;
    private  final CourseRepository courseRepository;
    private final SectionMapper sectionMapper;

     /** Integer maxOrder =
      sectionRepository.findMaxOrderIndexByCourseId(courseId);

      section.setOrderIndex(
      maxOrder == null ? 0 : maxOrder + 1
      );*/

//dont forget to make sure that section tittle is unique in each course
     //also make sure that the order index is unique in each course => tomorrow
     @Override
     @Caching(evict = {
             @CacheEvict(cacheNames = CacheConstants.COURSE, key = "#courseId"),
             @CacheEvict(cacheNames = {CacheConstants.ALL, CacheConstants.PUBLISHED}, allEntries = true)
     })
     public SectionResponse createSection(Long courseId, SectionRequest request, String instructorEmail) {
         var course = courseRepository.findById(courseId)
                 .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
         validateOwnership(course, instructorEmail);
         var section = sectionMapper.toEntity(request);
         section.setCourse(course);

         if (request.getOrderIndex() == null) {
             int nextIndex = sectionRepository.findMaxOrderIndexByCourseId(courseId)
                     .map(max -> max + 1)
                     .orElse(0);
             section.setOrderIndex(nextIndex);
         }

         return sectionMapper.toResponse(sectionRepository.save(section));
     }

    @Transactional(readOnly = true)
    @Override
    public SectionResponse getSectionById(Long courseId, Long id) {
        var section = sectionRepository.findByIdAndCourseId(id, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + id));
        return sectionMapper.toResponse(section);
    }


    @Transactional(readOnly = true)
    @Override
    public SectionResponse getSectionByTitleAndCourseId(String title, Long courseId) {
        return sectionMapper.toResponse(
                sectionRepository.findByTitleAndCourseId(title, courseId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Section not found with title: " + title + " and courseId: " + courseId
                        ))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<SectionResponse> getSectionsByCourse(
            Long courseId,
            int page,
            int size
    ) {
        courseRepository.findById(courseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Course not found with id: " + courseId
                        ));

        Page<SectionResponse> sectionPage =
                sectionRepository
                        .findByCourseId(
                                courseId,
                                PageRequest.of(page, size)
                        )
                        .map(sectionMapper::toResponse);

        return new PaginationResponse<>(
                sectionPage.getContent(),
                sectionPage.getNumber(),
                sectionPage.getSize(),
                sectionPage.getTotalElements(),
                sectionPage.getTotalPages(),
                sectionPage.isLast(),
                sectionPage.isFirst()
        );
    }


     @Override
     @Caching(evict = {
             @CacheEvict(cacheNames = CacheConstants.COURSE, key = "#courseId"),
             @CacheEvict(cacheNames = {CacheConstants.ALL, CacheConstants.PUBLISHED}, allEntries = true)
     })
    public SectionResponse updateSection(Long courseId, Long id, SectionRequest request, String instructorEmail) {
        var section = sectionRepository
                .findByIdAndCourseId(id, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + id));
        validateOwnership(section, instructorEmail);
        section.setTitle(request.getTitle());

        if (request.getOrderIndex() != null) {
            boolean conflict = sectionRepository.existsByCourseIdAndOrderIndexAndIdNot(
                    courseId, request.getOrderIndex(), id
            );
            if (conflict) {
                throw new IllegalArgumentException(
                        "Order index " + request.getOrderIndex() + " is already taken"
                );
            }
            section.setOrderIndex(request.getOrderIndex());
        }

        return sectionMapper.toResponse(sectionRepository.save(section));
    }


     @Override
     @Caching(evict = {
             @CacheEvict(cacheNames = CacheConstants.COURSE, key = "#courseId"),
             @CacheEvict(cacheNames = {CacheConstants.ALL, CacheConstants.PUBLISHED}, allEntries = true)
     })
    public void deleteSection(Long courseId, Long id, String instructorEmail) {
        var section = sectionRepository
                .findByIdAndCourseId(id, courseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Section not found with id: " + id
                        ));

        validateOwnership(section, instructorEmail);
        sectionRepository.delete(section);
    }
    //I did it for creation
    private void validateOwnership(Course course, String instructorEmail) {
        if (!course.getInstructor().getEmail().equals(instructorEmail)) {
            throw new UnauthorizedException("You don't own this course");
        }
    }

    //  update and delete
    private void validateOwnership(Section section, String instructorEmail) {
        if (!section.getCourse().getInstructor().getEmail().equals(instructorEmail)) {
            throw new UnauthorizedException("You don't own this section");
        }
    }
}
