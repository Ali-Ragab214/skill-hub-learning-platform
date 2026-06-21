package com.example.Skill_Hub_Learning_Platform.api.controller;

import com.example.Skill_Hub_Learning_Platform.application.dto.request.ReviewRequest;
import com.example.Skill_Hub_Learning_Platform.application.dto.request.UpdateReviewRequest;
import com.example.Skill_Hub_Learning_Platform.application.dto.response.ReviewResponse;
import com.example.Skill_Hub_Learning_Platform.application.responses.ApiResponse;
import com.example.Skill_Hub_Learning_Platform.application.responses.PaginationResponse;
import com.example.Skill_Hub_Learning_Platform.application.services.review.ReviewService;
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

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/courses/{courseId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @PathVariable Long courseId,
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication
    ) {
        ReviewResponse response = reviewService.createReview(courseId, request, authentication.getName());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Review submitted successfully", HttpStatus.CREATED));
    }

    @PutMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable Long courseId,
            @Valid @RequestBody UpdateReviewRequest request,
            Authentication authentication
    ) {
        ReviewResponse response = reviewService.updateReview(courseId, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(response, "Review updated successfully"));
    }

    @DeleteMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long courseId,
            Authentication authentication
    ) {
        reviewService.deleteReview(courseId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(null, "Review deleted successfully"));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<ReviewResponse>> getMyReview(
            @PathVariable Long courseId,
            Authentication authentication
    ) {
        ReviewResponse response = reviewService.getMyReviewForCourse(courseId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(response, "Review retrieved successfully"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<PaginationResponse<ReviewResponse>>> getReviewsByCourse(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
    ) {
        PaginationResponse<ReviewResponse> response = reviewService.getReviewsByCourse(courseId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response, "Reviews retrieved successfully"));
    }

    @GetMapping("/average-rating")
    public ResponseEntity<ApiResponse<Double>> getAverageRating(
            @PathVariable Long courseId
    ) {
        Double avg = reviewService.getAverageRating(courseId);
        return ResponseEntity.ok(ApiResponse.success(avg, "Average rating retrieved successfully"));
    }
}
