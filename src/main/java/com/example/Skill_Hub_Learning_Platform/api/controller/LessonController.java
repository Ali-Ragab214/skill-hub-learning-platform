package com.example.Skill_Hub_Learning_Platform.api.controller;

import com.example.Skill_Hub_Learning_Platform.application.dto.request.LessonRequest;
import com.example.Skill_Hub_Learning_Platform.application.dto.response.LessonResponse;
import com.example.Skill_Hub_Learning_Platform.application.responses.ApiResponse;
import com.example.Skill_Hub_Learning_Platform.application.responses.PaginationResponse;
import com.example.Skill_Hub_Learning_Platform.application.services.lesson.LessonService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/courses/{courseId}/sections/{sectionId}/lessons")
@RequiredArgsConstructor
public class LessonController {
    private  final LessonService lessonService;

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping
    public ResponseEntity<ApiResponse<LessonResponse>> createLesson(
            @PathVariable Long courseId,
            @PathVariable Long sectionId,
            @Valid @RequestBody LessonRequest request,
            Authentication authentication
            )
    {
        return  ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse
                        .success(lessonService.createLesson(courseId,sectionId,request, authentication.getName())
                                ,"Lesson created successfully"));
    }


    @PreAuthorize("permitAll()")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LessonResponse>> getLessonById(
            @PathVariable Long courseId,
            @PathVariable Long sectionId,
            @PathVariable Long id
    )
    {
        return  ResponseEntity.ok(ApiResponse.success(lessonService.getLessonById(courseId,sectionId,id),"Lesson retrieved successfully"));
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<LessonResponse>>> getLessonsInSection(
            @PathVariable Long courseId,
            @PathVariable Long sectionId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10")@Min(1) @Max(100) int size
    )
    {
        return ResponseEntity
                .ok(ApiResponse.success
                        (lessonService.getLessonsBySection(courseId,sectionId,page,size),
                                "Lessons retrieved successfully"));
    }


    @GetMapping("/preview")
    public  ResponseEntity<ApiResponse<List<LessonResponse>>> getPreviewedLessons
            (
                    @PathVariable Long courseId ,
                    @PathVariable Long sectionId
            )
    {
        return  ResponseEntity
                .ok(ApiResponse.success
                        (lessonService.getPreviewLessonsBySection(courseId,sectionId),
                                "Preview lessons retrieved successfully"
                        ));

    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<LessonResponse>> updateLesson(
            @PathVariable Long courseId,
            @PathVariable Long sectionId,
            @PathVariable Long id,
            @Valid @RequestBody LessonRequest request,
            Authentication authentication
    )
    {        return ResponseEntity.ok
            (ApiResponse.success
                    (lessonService.updateLesson(courseId,sectionId,id,request, authentication.getName()),
                            "Lesson updated successfully"));
    }


        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('INSTRUCTOR')")
        public ResponseEntity<ApiResponse<Void>> deleteLesson(
                @PathVariable Long courseId,
                @PathVariable Long sectionId,
                @PathVariable Long id,
                Authentication authentication
        ) {
            lessonService.deleteLesson(courseId,sectionId,id, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success(null, "Lesson deleted successfully"));
        }

}
