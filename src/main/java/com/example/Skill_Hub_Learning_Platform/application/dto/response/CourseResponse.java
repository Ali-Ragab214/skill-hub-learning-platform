package com.example.Skill_Hub_Learning_Platform.application.dto.response;

import com.example.Skill_Hub_Learning_Platform.domain.enums.CourseLevel;
import com.example.Skill_Hub_Learning_Platform.domain.enums.CourseStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {

    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private CourseLevel level;
    private CourseStatus status;
    private UserResponse instructor;
    List<SectionResponse> sections;
    private int totalEnrollments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}