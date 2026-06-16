package com.example.Skill_Hub_Learning_Platform.infrastructure.repository;

import com.example.Skill_Hub_Learning_Platform.domain.models.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    @EntityGraph(attributePaths = "course")
    Page<Enrollment> findByStudentId(Long studentId, Pageable pageable);

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    @EntityGraph(attributePaths = "course")
    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

    Long countByCourseId(Long courseId);
}
