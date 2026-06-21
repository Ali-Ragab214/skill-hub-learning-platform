package com.example.Skill_Hub_Learning_Platform.application.services.review;

import com.example.Skill_Hub_Learning_Platform.application.dto.request.ReviewRequest;
import com.example.Skill_Hub_Learning_Platform.application.dto.request.UpdateReviewRequest;
import com.example.Skill_Hub_Learning_Platform.application.dto.response.ReviewResponse;
import com.example.Skill_Hub_Learning_Platform.application.exceptions.BusinessException;
import com.example.Skill_Hub_Learning_Platform.application.exceptions.DuplicateResourceException;
import com.example.Skill_Hub_Learning_Platform.application.exceptions.ResourceNotFoundException;
import com.example.Skill_Hub_Learning_Platform.application.exceptions.UnauthorizedException;
import com.example.Skill_Hub_Learning_Platform.application.mapper.ReviewMapper;
import com.example.Skill_Hub_Learning_Platform.application.responses.PaginationResponse;
import com.example.Skill_Hub_Learning_Platform.domain.enums.Role;
import com.example.Skill_Hub_Learning_Platform.domain.models.Course;
import com.example.Skill_Hub_Learning_Platform.domain.models.Review;
import com.example.Skill_Hub_Learning_Platform.domain.models.User;
import com.example.Skill_Hub_Learning_Platform.infrastructure.repository.CourseRepository;
import com.example.Skill_Hub_Learning_Platform.infrastructure.repository.EnrollmentRepository;
import com.example.Skill_Hub_Learning_Platform.infrastructure.repository.ReviewRepository;
import com.example.Skill_Hub_Learning_Platform.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

    @Override
    public ReviewResponse createReview(Long courseId, ReviewRequest request, String studentEmail) {
        User student = getStudentByEmail(studentEmail);

        if (student.getRole() != Role.Student) {
            throw new UnauthorizedException("Only students can leave a review");
        }

        Course course = getCourseById(courseId);

        if (!enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            throw new BusinessException("You must be enrolled in this course to leave a review");
        }

        if (reviewRepository.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            throw new DuplicateResourceException("You have already reviewed this course");
        }

        Review review = Review.builder()
                .rating(request.getRating())
                .comment(request.getComment())
                .student(student)
                .course(course)
                .build();

        Review saved = reviewRepository.save(review);
        recalculateAverageRating(courseId);

        return reviewMapper.toResponse(saved);
    }

    @Override
    public ReviewResponse updateReview(Long courseId, UpdateReviewRequest request, String studentEmail) {
        User student = getStudentByEmail(studentEmail);

        Review review = reviewRepository.findByStudentIdAndCourseId(student.getId(), courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found for this course"));

        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        if (request.getComment() != null) {
            review.setComment(request.getComment());
        }

        Review saved = reviewRepository.save(review);
        recalculateAverageRating(courseId);

        return reviewMapper.toResponse(saved);
    }

    @Override
    public void deleteReview(Long courseId, String studentEmail) {
        User student = getStudentByEmail(studentEmail);

        Review review = reviewRepository.findByStudentIdAndCourseId(student.getId(), courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found for this course"));

        reviewRepository.delete(review);
        recalculateAverageRating(courseId);
    }

    @Transactional(readOnly = true)
    @Override
    public ReviewResponse getMyReviewForCourse(Long courseId, String studentEmail) {
        User student = getStudentByEmail(studentEmail);

        Review review = reviewRepository.findByStudentIdAndCourseId(student.getId(), courseId)
                .orElseThrow(() -> new ResourceNotFoundException("You have not reviewed this course yet"));

        return reviewMapper.toResponse(review);
    }

    @Transactional(readOnly = true)
    @Override
    public PaginationResponse<ReviewResponse> getReviewsByCourse(Long courseId, int page, int size) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", courseId);
        }

        Page<ReviewResponse> mappedPage = reviewRepository
                .findByCourseId(courseId, PageRequest.of(page, size))
                .map(reviewMapper::toResponse);

        return new PaginationResponse<>(
                mappedPage.getContent(),
                mappedPage.getNumber(),
                mappedPage.getSize(),
                mappedPage.getTotalElements(),
                mappedPage.getTotalPages(),
                mappedPage.isLast(),
                mappedPage.isFirst()
        );
    }

    @Transactional(readOnly = true)
    @Override
    public Double getAverageRating(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", courseId);
        }
        Double avg = reviewRepository.findAverageRatingByCourseId(courseId);
        return avg != null ? avg : 0.0;
    }

    private void recalculateAverageRating(Long courseId) {
        Double avg = reviewRepository.findAverageRatingByCourseId(courseId);
        Course course = getCourseById(courseId);
        course.setAverageRating(avg != null ? avg : 0.0);
        courseRepository.save(course);
    }



    private User getStudentByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Course getCourseById(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));
    }
}
