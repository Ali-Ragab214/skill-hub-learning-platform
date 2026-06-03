package com.example.Skill_Hub_Learning_Platform.infrastructure.repository;

import com.example.Skill_Hub_Learning_Platform.domain.enums.CourseLevel;
import com.example.Skill_Hub_Learning_Platform.domain.enums.CourseStatus;
import com.example.Skill_Hub_Learning_Platform.domain.models.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository  extends JpaRepository<Course,Long> {
    List<Course> findByInstructorId(Long instructorId);

    List<Course> findByStatus(CourseStatus status);

    List<Course> findByLevel(CourseLevel level);

    List<Course> findByStatusEquals(CourseStatus status);

    List<Course> findByStatusEqualsAndLevel(CourseStatus status, CourseLevel level);

    List<Course> findByStatusEqualsAndInstructorId(CourseStatus status, Long instructorId);
    List<Course> findByStatusAndInstructorEmail(
            CourseStatus status,
            String email
    );

    List<Course> findByStatusAndInstructorId(
            CourseStatus status,
            Long instructorId
    );
}
