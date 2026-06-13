package com.example.Skill_Hub_Learning_Platform.application.mapper;

import com.example.Skill_Hub_Learning_Platform.application.dto.response.EnrollmentResponse;
import com.example.Skill_Hub_Learning_Platform.domain.models.Enrollment;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentMapper {

    public EnrollmentResponse toResponse(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getCourse().getId(),
                enrollment.getCourse().getTitle(),
                enrollment.getProgressPercentage(),
                enrollment.getCreatedAt()
        );
    }
}
