package com.example.Skill_Hub_Learning_Platform.api.controller;

import com.example.Skill_Hub_Learning_Platform.application.dto.request.SectionRequest;
import com.example.Skill_Hub_Learning_Platform.application.dto.response.SectionResponse;
import com.example.Skill_Hub_Learning_Platform.application.responses.ApiResponse;
import com.example.Skill_Hub_Learning_Platform.application.responses.PaginationResponse;
import com.example.Skill_Hub_Learning_Platform.application.services.section.SectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses/{courseId}/sections")
public class SectionController {
    private final SectionService sectionService;

    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<SectionResponse>> createSection(
            @PathVariable Long courseId,
           @Valid @RequestBody SectionRequest request,
            Authentication authentication)
    {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        sectionService.createSection(courseId, request, authentication.getName()),
                        "Section created successfully",
                        HttpStatus.CREATED
                ));
    }


   @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SectionResponse>> getSectionById(
            @PathVariable Long courseId,
            @PathVariable Long id) {
       return ResponseEntity.ok(
               ApiResponse.success(
                       sectionService.getSectionById(courseId, id),
                       "Section retrieved successfully"
               )
       );
   }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<SectionResponse>> getSectionByTitleAndCourseId(
            @RequestParam String title,
            @PathVariable Long courseId) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        sectionService.getSectionByTitleAndCourseId(title, courseId),
                        "Section retrieved successfully"
                )
        );
    }


    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<SectionResponse>>> getSectionsByCourse(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        sectionService.getSectionsByCourse(courseId, page, size),
                        "Sections retrieved successfully"
                )
        );
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<SectionResponse>> updateSection(
            @PathVariable Long courseId,
            @PathVariable Long id,
            @Valid @RequestBody SectionRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        sectionService.updateSection(courseId, id, request, authentication.getName()),
                        "Section updated successfully"
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSection(
            @PathVariable Long courseId,
            @PathVariable Long id,
            Authentication authentication) {
        sectionService.deleteSection(courseId, id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(null, "Section deleted successfully"));
    }

}
