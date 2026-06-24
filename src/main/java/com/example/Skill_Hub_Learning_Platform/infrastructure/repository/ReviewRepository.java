package com.example.Skill_Hub_Learning_Platform.infrastructure.repository;

import com.example.Skill_Hub_Learning_Platform.domain.models.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review,Long> {

    @EntityGraph(attributePaths = {"course", "student"})
    List<Review> findByCourseId(Long courseId);

    @EntityGraph(attributePaths = {"course", "student"})
    Page<Review> findByCourseId(Long courseId, Pageable pageable);

    @EntityGraph(attributePaths = {"course", "student"})
    Optional<Review> findByStudentIdAndCourseId(Long studentId, Long courseId);

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.course.id = :courseId")
    Double findAverageRatingByCourseId(@Param("courseId") Long courseId);
}
