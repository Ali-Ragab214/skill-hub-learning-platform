package com.example.Skill_Hub_Learning_Platform.infrastructure.repository;

import com.example.Skill_Hub_Learning_Platform.domain.models.Lesson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findBySectionIdAndIsPreview(Long sectionId, boolean isPreview);
    void deleteBySectionId(Long sectionId);
    void deleteBySectionCourseId(Long courseId);
    Optional<Lesson> findByIdAndSectionIdAndSectionCourseId(
            Long lessonId,
            Long sectionId,
            Long courseId
    );
    Page<Lesson> findBySectionIdAndSectionCourseId(Long sectionId, Long courseId, Pageable pageable);
    boolean existsByTitleAndSectionId(String title, Long sectionId);
    boolean existsByTitleAndSectionIdAndIdNot(String title, Long sectionId, Long excludeId);

    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.section.course.id = :courseId")
    long countByCourseId(@Param("courseId") Long courseId);
}
