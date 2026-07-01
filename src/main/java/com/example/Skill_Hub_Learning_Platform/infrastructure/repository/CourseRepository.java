package com.example.Skill_Hub_Learning_Platform.infrastructure.repository;

import com.example.Skill_Hub_Learning_Platform.domain.enums.CourseLevel;
import com.example.Skill_Hub_Learning_Platform.domain.enums.CourseStatus;
import com.example.Skill_Hub_Learning_Platform.domain.models.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    @EntityGraph("Course.details")
    List<Course> findByInstructorId(Long instructorId);

    @EntityGraph("Course.details")
    List<Course> findByStatus(CourseStatus status);

    @EntityGraph("Course.details")
    List<Course> findByLevel(CourseLevel level);

    @EntityGraph("Course.details")
    List<Course> findByStatusEquals(CourseStatus status);

    @EntityGraph("Course.details")
    List<Course> findByStatusEqualsAndLevel(CourseStatus status, CourseLevel level);

    @EntityGraph("Course.details")
    List<Course> findByStatusEqualsAndInstructorId(CourseStatus status, Long instructorId);

    @EntityGraph("Course.details")
    List<Course> findByStatusAndInstructorEmail(CourseStatus status, String email);

    @EntityGraph("Course.details")
    List<Course> findByStatusAndInstructorId(CourseStatus status, Long instructorId);

    @EntityGraph("Course.details")
    @Query("SELECT c FROM Course c WHERE c.id = :id")
    Optional<Course> findCourseWithDetailsById(@Param("id") Long id);

    @EntityGraph("Course.details")
    @Query("select c from Course c")
    List<Course> findAllWithSectionsAndLessons();

    @EntityGraph("Course.details")
    List<Course> findWithSectionsAndLessonsByStatusEquals(CourseStatus status);
}
