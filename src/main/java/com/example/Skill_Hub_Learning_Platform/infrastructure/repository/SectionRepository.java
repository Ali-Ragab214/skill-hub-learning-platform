package com.example.Skill_Hub_Learning_Platform.infrastructure.repository;

import com.example.Skill_Hub_Learning_Platform.domain.models.Section;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface
SectionRepository extends JpaRepository<Section,Long> {

    @EntityGraph(attributePaths = "lessons")
    List<Section> findByCourseIdOrderByOrderIndexAsc(Long courseId);

    @Query("""
            SELECT s.id, s.title,
                   COUNT(l.id),
                   COUNT(CASE WHEN lp.isCompleted = true AND lp.student.id = :studentId THEN 1 END)
            FROM Section s
            LEFT JOIN s.lessons l
            LEFT JOIN LessonProgress lp ON lp.lesson.section.id = s.id AND lp.student.id = :studentId
            WHERE s.course.id = :courseId
            GROUP BY s.id, s.title, s.orderIndex
            ORDER BY s.orderIndex
            """)
    List<Object[]> findSectionProgressByCourseId(@Param("courseId") Long courseId, @Param("studentId") Long studentId);

    Optional<Section> findByTitleAndCourseId(String title, Long courseId);

    void deleteByCourseId(Long courseId);

    Page<Section> findByCourseId(Long courseId, Pageable pageable);

    Optional<Section> findByIdAndCourseId(Long sectionId, Long courseId);

    Optional<Integer> findMaxOrderIndexByCourseId(Long courseId);

    boolean existsByCourseIdAndOrderIndexAndIdNot(Long courseId, Integer orderIndex, Long id);
}
