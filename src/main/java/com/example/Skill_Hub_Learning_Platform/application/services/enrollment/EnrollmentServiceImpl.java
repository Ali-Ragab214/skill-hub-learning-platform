package com.example.Skill_Hub_Learning_Platform.application.services.enrollment;

import com.example.Skill_Hub_Learning_Platform.application.dto.request.UpdateProgressRequest;
import com.example.Skill_Hub_Learning_Platform.application.dto.response.EnrollmentResponse;
import com.example.Skill_Hub_Learning_Platform.application.exceptions.BadRequestException;
import com.example.Skill_Hub_Learning_Platform.application.exceptions.DuplicateResourceException;
import com.example.Skill_Hub_Learning_Platform.application.exceptions.ResourceNotFoundException;
import com.example.Skill_Hub_Learning_Platform.application.exceptions.UnauthorizedException;
import com.example.Skill_Hub_Learning_Platform.domain.enums.Role;
import com.example.Skill_Hub_Learning_Platform.application.mapper.EnrollmentMapper;
import com.example.Skill_Hub_Learning_Platform.application.responses.PaginationResponse;
import com.example.Skill_Hub_Learning_Platform.domain.enums.CourseStatus;
import com.example.Skill_Hub_Learning_Platform.domain.models.Course;
import com.example.Skill_Hub_Learning_Platform.domain.models.Enrollment;
import com.example.Skill_Hub_Learning_Platform.domain.models.User;
import com.example.Skill_Hub_Learning_Platform.infrastructure.repository.CourseRepository;
import com.example.Skill_Hub_Learning_Platform.infrastructure.repository.EnrollmentRepository;
import com.example.Skill_Hub_Learning_Platform.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements  EnrollmentService {
    private  final EnrollmentRepository enrollmentRepository;
    private final EnrollmentMapper enrollmentMapper;
    private  final UserRepository userRepository;
    private  final CourseRepository courseRepository;


    @Transactional
    @Override
    public EnrollmentResponse enroll(Long courseId, String studentEmail) {
        var student = getStudentByEmail(studentEmail);

        if (student.getRole() != Role.Student) {
            throw new UnauthorizedException("Only students can enroll in courses");
        }

        var course = getCourseById(courseId);

        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new BadRequestException("Cannot enroll in an unpublished course");
        }

        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            throw new DuplicateResourceException("Student is already enrolled in this course");
        }

        var enrollment = Enrollment.builder()
                .course(course)
                .student(student)
                .progressPercentage(0)
                .build();

        return enrollmentMapper.toResponse(enrollmentRepository.save(enrollment));
    }



    @Transactional
    @Override
    public void unenroll(Long courseId, String studentEmail) {
        var student = getStudentByEmail(studentEmail);
        var enrollment = getEnrollmentByCourseAndStudent(courseId, student.getId());
        enrollmentRepository.delete(enrollment);
    }


    @Transactional(readOnly = true)
    @Override
    public PaginationResponse<EnrollmentResponse> getMyEnrollments(String studentEmail, Pageable pageable) {
        var student = getStudentByEmail(studentEmail);

        Page<EnrollmentResponse> mappedPage = enrollmentRepository
                .findByStudentId(student.getId(), pageable)
                .map(enrollmentMapper::toResponse);

        return new PaginationResponse<>(
                mappedPage.getContent(),
                mappedPage.getNumber(),
                mappedPage.getSize(),
                mappedPage.getTotalElements(),
                mappedPage.getTotalPages(),
                mappedPage.isLast(),
                mappedPage.isFirst()
        );
    }


    @Transactional(readOnly = true)
    @Override
    public EnrollmentResponse getEnrollmentDetails(Long courseId, String studentEmail) {
        var student = getStudentByEmail(studentEmail);
        var enrollment = getEnrollmentByCourseAndStudent(courseId, student.getId());
        return enrollmentMapper.toResponse(enrollment);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean isEnrolled(Long courseId, String studentEmail) {
        var student = getStudentByEmail(studentEmail);
        return enrollmentRepository
                .existsByStudentIdAndCourseId(student.getId(), courseId);
    }

    @Transactional(readOnly = true)
    @Override
    public Long getEnrollmentCount(Long courseId) {
        getCourseById(courseId);
        return enrollmentRepository.countByCourseId(courseId);
    }

    @Transactional
    @Override
    public EnrollmentResponse updateProgress(Long enrollmentId, UpdateProgressRequest request) {

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + enrollmentId));

        enrollment.setProgressPercentage(request.getProgressPercentage());

        return enrollmentMapper.toResponse(enrollmentRepository.save(enrollment));
    }


    private User getStudentByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }

    private Course getCourseById(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
    }

    private Enrollment getEnrollmentByCourseAndStudent(Long courseId, Long studentId) {
        return enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));
    }
}
