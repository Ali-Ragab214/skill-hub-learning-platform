package com.example.Skill_Hub_Learning_Platform.application.services.enrollment;

import com.example.Skill_Hub_Learning_Platform.application.dto.response.EnrollmentResponse;
import com.example.Skill_Hub_Learning_Platform.application.responses.PaginationResponse;
import com.example.Skill_Hub_Learning_Platform.domain.models.User;
import org.springframework.data.domain.Pageable;

public interface EnrollmentService {
    void enroll(Long courseId, String studentEmail);
    void unenroll(Long courseId, String studentEmail);
    PaginationResponse<EnrollmentResponse> getMyEnrollments(String studentEmail, Pageable pageable);
    EnrollmentResponse getEnrollmentDetails(Long courseId, String studentEmail);
    boolean isEnrolled(Long courseId, String studentEmail);
    Long getEnrollmentCount(Long courseId);
}
