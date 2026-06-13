package com.example.Skill_Hub_Learning_Platform.infrastructure.repository;

import com.example.Skill_Hub_Learning_Platform.domain.models.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment,Long > {
   Page<Enrollment> findByStudentId(
           Long studentId,
           Pageable pageable
   );
    boolean existsByStudentIdAndCourseId(
              Long studentId,
              Long courseId
    );

    Optional<Enrollment> findByStudentIdAndCourseId(
            Long studentId,
            Long courseId
    );

    Long countByCourseId(Long courseId);
}
