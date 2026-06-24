package com.example.Skill_Hub_Learning_Platform.infrastructure.repository;

import com.example.Skill_Hub_Learning_Platform.domain.models.Section;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface
SectionRepository extends JpaRepository<Section,Long> {

    @EntityGraph(attributePaths = "lessons")
    List<Section> findByCourseIdOrderByOrderIndexAsc(Long courseId);

    Optional<Section> findByTitleAndCourseId(String title, Long courseId);

    void deleteByCourseId(Long courseId);

    Page<Section> findByCourseId(Long courseId, Pageable pageable);

    Optional<Section> findByIdAndCourseId(Long sectionId, Long courseId);

    Optional<Integer> findMaxOrderIndexByCourseId(Long courseId);

    boolean existsByCourseIdAndOrderIndexAndIdNot(Long courseId, Integer orderIndex, Long id);
}
