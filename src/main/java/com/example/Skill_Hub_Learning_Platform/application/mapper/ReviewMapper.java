package com.example.Skill_Hub_Learning_Platform.application.mapper;

import com.example.Skill_Hub_Learning_Platform.application.dto.response.ReviewResponse;
import com.example.Skill_Hub_Learning_Platform.domain.models.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getCourse().getId(),
                review.getCourse().getTitle(),
                review.getStudent().getId(),
                review.getStudent().getName(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
