package com.example.Skill_Hub_Learning_Platform.infrastructure.repository;

import com.example.Skill_Hub_Learning_Platform.domain.models.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    @EntityGraph(attributePaths = "course")
    Page<Enrollment> findByStudentId(Long studentId, Pageable pageable);

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    @EntityGraph(attributePaths = "course")
    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

    Long countByCourseId(Long courseId);

    @Query("SELECT e FROM Enrollment e JOIN FETCH e.student JOIN FETCH e.course WHERE e.course.instructor.id = :instructorId ORDER BY e.createdAt DESC")
    List<Enrollment> findRecentByInstructorId(@Param("instructorId") Long instructorId, Pageable pageable);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.instructor.id = :instructorId")
    long countByInstructorId(@Param("instructorId") Long instructorId);

    @Query("SELECT COUNT(DISTINCT e.student.id) FROM Enrollment e WHERE e.course.instructor.id = :instructorId")
    long countDistinctStudentsByInstructorId(@Param("instructorId") Long instructorId);

    @Query("""
            SELECT EXTRACT(YEAR FROM e.createdAt) as year,
                   EXTRACT(MONTH FROM e.createdAt) as month,
                   COUNT(e) as cnt
            FROM Enrollment e
            WHERE e.course.instructor.id = :instructorId
            GROUP BY EXTRACT(YEAR FROM e.createdAt), EXTRACT(MONTH FROM e.createdAt)
            ORDER BY year, month
            """)
    List<Object[]> findEnrollmentTrendByInstructorId(@Param("instructorId") Long instructorId);
}
