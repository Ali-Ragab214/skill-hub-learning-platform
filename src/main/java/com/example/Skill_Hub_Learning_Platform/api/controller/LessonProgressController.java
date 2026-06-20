package com.example.Skill_Hub_Learning_Platform.api.controller;

import com.example.Skill_Hub_Learning_Platform.application.dto.response.LessonProgressResponse;
import com.example.Skill_Hub_Learning_Platform.application.responses.ApiResponse;
import com.example.Skill_Hub_Learning_Platform.application.services.progress.LessonProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LessonProgressController {

    private final LessonProgressService lessonProgressService;

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/lessons/{lessonId}/progress")
    public ResponseEntity<ApiResponse<LessonProgressResponse>> markLessonCompleted(
            @PathVariable Long lessonId,
            Authentication authentication
    ) {
        LessonProgressResponse response = lessonProgressService
                .markLessonCompleted(lessonId, authentication.getName());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Lesson marked as completed"));
    }

    @PreAuthorize("hasRole('STUDENT')")
    @DeleteMapping("/lessons/{lessonId}/progress")
    public ResponseEntity<ApiResponse<LessonProgressResponse>> markLessonIncomplete(
            @PathVariable Long lessonId,
            Authentication authentication
    ) {
        LessonProgressResponse response = lessonProgressService
                .markLessonIncomplete(lessonId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(response, "Lesson marked as incomplete"));
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/courses/{courseId}/progress")
    public ResponseEntity<ApiResponse<List<LessonProgressResponse>>> getCourseProgress(
            @PathVariable Long courseId,
            Authentication authentication
    ) {
        List<LessonProgressResponse> responses = lessonProgressService
                .getCourseProgress(courseId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(responses, "Course progress retrieved successfully"));
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/lessons/{lessonId}/progress")
    public ResponseEntity<ApiResponse<LessonProgressResponse>> getLessonProgress(
            @PathVariable Long lessonId,
            Authentication authentication
    ) {
        LessonProgressResponse response = lessonProgressService
                .getLessonProgress(lessonId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(response, "Lesson progress retrieved successfully"));
    }
}
