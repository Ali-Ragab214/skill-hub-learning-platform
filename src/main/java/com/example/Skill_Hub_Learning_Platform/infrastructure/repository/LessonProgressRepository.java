package com.example.Skill_Hub_Learning_Platform.infrastructure.repository;

import com.example.Skill_Hub_Learning_Platform.domain.models.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {

    Optional<LessonProgress> findByStudentIdAndLessonId(Long studentId, Long lessonId);

    List<LessonProgress> findByStudentId(Long studentId);

    @Query("SELECT lp FROM LessonProgress lp " +
           "JOIN lp.lesson l " +
           "WHERE l.section.course.id = :courseId " +
           "AND lp.student.id = :studentId")
    List<LessonProgress> findByStudentIdAndLessonSectionCourseId(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    @Query("SELECT COUNT(lp) FROM LessonProgress lp " +
           "JOIN lp.lesson l " +
           "WHERE l.section.course.id = :courseId " +
           "AND lp.student.id = :studentId " +
           "AND lp.isCompleted = true")
    long countCompletedByStudentIdAndCourseId(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    @Query("SELECT COUNT(lp) FROM LessonProgress lp " +
           "WHERE lp.lesson.section.id = :sectionId " +
           "AND lp.student.id = :studentId " +
           "AND lp.isCompleted = true")
    long countCompletedByStudentIdAndSectionId(@Param("studentId") Long studentId, @Param("sectionId") Long sectionId);

    @Modifying
    @Query("DELETE FROM LessonProgress lp " +
           "WHERE lp.student.id = :studentId " +
           "AND lp.lesson.id IN (" +
           "  SELECT l.id FROM Lesson l WHERE l.section.course.id = :courseId" +
           ")")
    void deleteByStudentIdAndLessonSectionCourseId(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    @Modifying
    @Query("DELETE FROM LessonProgress lp WHERE lp.student.id = :studentId")
    void deleteByStudentId(@Param("studentId") Long studentId);

    @Modifying
    @Query("DELETE FROM LessonProgress lp " +
           "WHERE lp.lesson.id IN (" +
           "  SELECT l.id FROM Lesson l WHERE l.section.course.instructor.id = :instructorId" +
           ")")
    void deleteByInstructorId(@Param("instructorId") Long instructorId);
}

