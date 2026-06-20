package com.example.Skill_Hub_Learning_Platform.application.services.progress;

import com.example.Skill_Hub_Learning_Platform.application.dto.response.LessonProgressResponse;
import com.example.Skill_Hub_Learning_Platform.application.exceptions.BusinessException;
import com.example.Skill_Hub_Learning_Platform.application.exceptions.ResourceNotFoundException;
import com.example.Skill_Hub_Learning_Platform.domain.models.Enrollment;
import com.example.Skill_Hub_Learning_Platform.domain.models.Lesson;
import com.example.Skill_Hub_Learning_Platform.domain.models.LessonProgress;
import com.example.Skill_Hub_Learning_Platform.domain.models.User;
import com.example.Skill_Hub_Learning_Platform.infrastructure.repository.EnrollmentRepository;
import com.example.Skill_Hub_Learning_Platform.infrastructure.repository.LessonProgressRepository;
import com.example.Skill_Hub_Learning_Platform.infrastructure.repository.LessonRepository;
import com.example.Skill_Hub_Learning_Platform.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LessonProgressServiceImpl implements LessonProgressService {

    private final LessonProgressRepository lessonProgressRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    @Override
    public LessonProgressResponse markLessonCompleted(Long lessonId, String studentEmail) {
        User student = getStudentByEmail(studentEmail);
        Lesson lesson = getLessonById(lessonId);

        Long courseId = lesson.getSection().getCourse().getId();

        if (!enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            throw new BusinessException("Student is not enrolled in this course");
        }

        LessonProgress progress = lessonProgressRepository
                .findByStudentIdAndLessonId(student.getId(), lessonId)
                .orElse(LessonProgress.builder()
                        .student(student)
                        .lesson(lesson)
                        .build());

        if (progress.getIsCompleted()) {
            return toResponse(progress);
        }

        progress.setIsCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());

        lessonProgressRepository.save(progress);
        recalculateCourseProgress(student.getId(), courseId);

        return toResponse(progress);
    }

    @Override
    public LessonProgressResponse markLessonIncomplete(Long lessonId, String studentEmail) {
        User student = getStudentByEmail(studentEmail);
        Lesson lesson = getLessonById(lessonId);

        LessonProgress progress = lessonProgressRepository
                .findByStudentIdAndLessonId(student.getId(), lessonId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Progress not found for lesson " + lessonId));
        progress.setIsCompleted(false);
        progress.setCompletedAt(null);

        lessonProgressRepository.save(progress);
        recalculateCourseProgress(student.getId(), lesson.getSection().getCourse().getId());

        return toResponse(progress);
    }

    @Transactional(readOnly = true)
    @Override
    public List<LessonProgressResponse> getCourseProgress(Long courseId, String studentEmail) {
        User student = getStudentByEmail(studentEmail);
        return lessonProgressRepository
                .findByStudentIdAndLessonSectionCourseId(student.getId(), courseId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public LessonProgressResponse getLessonProgress(Long lessonId, String studentEmail) {
        User student = getStudentByEmail(studentEmail);
        Lesson lesson = getLessonById(lessonId);
        Long courseId = lesson.getSection().getCourse().getId();

        if (!enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            throw new BusinessException("You are not enrolled in the course that contains this lesson");
        }

        LessonProgress progress = lessonProgressRepository
                .findByStudentIdAndLessonId(student.getId(), lessonId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Progress not found for lesson " + lessonId));
        return toResponse(progress);
    }

    private void recalculateCourseProgress(Long studentId, Long courseId) {
        long totalLessons = lessonRepository.countByCourseId(courseId);
        if (totalLessons == 0) return;

        long completedLessons = lessonProgressRepository
                .countCompletedByStudentIdAndCourseId(studentId, courseId);

        int percentage = (int) Math.round((completedLessons * 100.0) / totalLessons);

        Enrollment enrollment = enrollmentRepository
                .findByStudentIdAndCourseId(studentId, courseId)
                .orElse(null);
        if (enrollment != null) {
            enrollment.setProgressPercentage(percentage);
            enrollmentRepository.save(enrollment);
        }
    }

    private LessonProgressResponse toResponse(LessonProgress progress) {
        return new LessonProgressResponse(
                progress.getId(),
                progress.getLesson().getId(),
                progress.getLesson().getTitle(),
                progress.getIsCompleted(),
                progress.getCompletedAt()
        );
    }

    private User getStudentByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Lesson getLessonById(Long lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));
    }
}
