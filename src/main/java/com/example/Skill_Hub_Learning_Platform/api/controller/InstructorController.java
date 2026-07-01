package com.example.Skill_Hub_Learning_Platform.api.controller;

import com.example.Skill_Hub_Learning_Platform.application.dto.response.InstructorDashboardResponse;
import com.example.Skill_Hub_Learning_Platform.application.responses.ApiResponse;
import com.example.Skill_Hub_Learning_Platform.application.services.course.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/instructor")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
public class InstructorController {

    private final CourseService courseService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<InstructorDashboardResponse>> getDashboard(Authentication authentication) {
        InstructorDashboardResponse dashboard = courseService.getDashboard(authentication.getName());
        return ResponseEntity.ok(
                ApiResponse.success(dashboard, "Dashboard data retrieved successfully")
        );
    }
}
