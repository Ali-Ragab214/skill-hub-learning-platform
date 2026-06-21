package com.example.Skill_Hub_Learning_Platform.application.services.review;

import com.example.Skill_Hub_Learning_Platform.application.dto.request.ReviewRequest;
import com.example.Skill_Hub_Learning_Platform.application.dto.request.UpdateReviewRequest;
import com.example.Skill_Hub_Learning_Platform.application.dto.response.ReviewResponse;
import com.example.Skill_Hub_Learning_Platform.application.responses.PaginationResponse;

public interface ReviewService {

    ReviewResponse createReview(Long courseId, ReviewRequest request, String studentEmail);

    ReviewResponse updateReview(Long courseId, UpdateReviewRequest request, String studentEmail);

    void deleteReview(Long courseId, String studentEmail);

    ReviewResponse getMyReviewForCourse(Long courseId, String studentEmail);

    PaginationResponse<ReviewResponse> getReviewsByCourse(Long courseId, int page, int size);

    Double getAverageRating(Long courseId);
}
