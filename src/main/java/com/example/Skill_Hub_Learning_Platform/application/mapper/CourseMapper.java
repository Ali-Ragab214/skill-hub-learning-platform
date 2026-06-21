package com.example.Skill_Hub_Learning_Platform.application.mapper;

import com.example.Skill_Hub_Learning_Platform.application.dto.request.CourseRequest;
import com.example.Skill_Hub_Learning_Platform.application.dto.response.CourseResponse;
import com.example.Skill_Hub_Learning_Platform.application.dto.response.UserResponse;
import com.example.Skill_Hub_Learning_Platform.domain.models.Course;
import com.example.Skill_Hub_Learning_Platform.domain.enums.CourseStatus;
import lombok.RequiredArgsConstructor;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseMapper {

    private static  UserMapper userMapper = new UserMapper();
    private final SectionMapper sectionMapper;

    public  Course toEntity(CourseRequest request) {
        return Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .level(request.getLevel())
                .status(request.getStatus() != null ? request.getStatus() : CourseStatus.DRAFT)
                .build();
    }

    public CourseResponse toResponse(Course course) {
        UserResponse instructor =
                userMapper.toUserResponse(course.getInstructor());
        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .price(course.getPrice())
                .level(course.getLevel())
                .status(course.getStatus())
                .instructor(instructor)
                .sections(course.getSections().stream()
                        .map(sectionMapper::toResponse)
                        .toList())
                .totalEnrollments(
                        course.getEnrollments() != null
                                ? course.getEnrollments().size()
                                : 0
                )
                .averageRating(course.getAverageRating())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }

    public CourseResponse toCreationResponse(Course course) {
        UserResponse instructor =
                userMapper.toUserResponse(course.getInstructor());
        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .price(course.getPrice())
                .level(course.getLevel())
                .status(course.getStatus())
                .instructor(instructor)
                .sections(List.of())
                .totalEnrollments(0)
                .averageRating(0.0)
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}