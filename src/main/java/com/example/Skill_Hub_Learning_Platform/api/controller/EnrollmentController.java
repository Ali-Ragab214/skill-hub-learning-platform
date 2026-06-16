package com.example.Skill_Hub_Learning_Platform.api.controller;

import com.example.Skill_Hub_Learning_Platform.application.dto.request.EnrollmentRequest;
import com.example.Skill_Hub_Learning_Platform.application.dto.request.UpdateProgressRequest;
import com.example.Skill_Hub_Learning_Platform.application.dto.response.EnrollmentResponse;
import com.example.Skill_Hub_Learning_Platform.application.responses.ApiResponse;
import com.example.Skill_Hub_Learning_Platform.application.responses.PaginationResponse;
import com.example.Skill_Hub_Learning_Platform.application.services.enrollment.EnrollmentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Validated
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping
    public ResponseEntity<Void> enrollInCourse(
            @Valid @RequestBody EnrollmentRequest request,
            Authentication authentication
    ) {
        enrollmentService.enroll(request.getCourseId(), authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PreAuthorize("hasRole('STUDENT')")
    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> unenrollFromCourse(
            @PathVariable Long courseId,
            Authentication authentication
    ) {
        enrollmentService.unenroll(courseId, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PaginationResponse<EnrollmentResponse>>> getMyEnrollments(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            Authentication authentication
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                ApiResponse.success(
                        enrollmentService.getMyEnrollments(authentication.getName(), pageable),
                        "Enrollments retrieved successfully"
                )
        );
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> getEnrollmentDetails(
            @PathVariable Long courseId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        enrollmentService.getEnrollmentDetails(courseId, authentication.getName()),
                        "Enrollment details retrieved successfully"
                )
        );
    }

    //  دا هيتحسب من عدد ال lessons المكتمله وعدد ال lessons الكلي في الكورس
    // يعني مش مسؤوليه لا الطالب ولا الانستراكتور دا المفروض يتحسب تلقائي
    @PatchMapping("/{enrollmentId}/progress")
    public  ResponseEntity<ApiResponse<EnrollmentResponse>> updateProgress(
            @PathVariable Long enrollmentId,
            @RequestBody @Valid UpdateProgressRequest request
    )
    {
     return ResponseEntity.ok(
             ApiResponse.success(
                     enrollmentService.updateProgress(enrollmentId,request),
                     "Progress updated successfully"
             )
     );
    }


    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/{courseId}/status")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkEnrollmentStatus(
            @PathVariable Long courseId,
            Authentication authentication
    ) {
        boolean enrolled = enrollmentService.isEnrolled(courseId, authentication.getName());
        return ResponseEntity.ok(
                ApiResponse.success(Map.of("enrolled", enrolled), "Enrollment status retrieved successfully")
        );
    }
}

