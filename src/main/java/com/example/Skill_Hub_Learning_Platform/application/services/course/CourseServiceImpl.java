package com.example.Skill_Hub_Learning_Platform.application.services.course;

import com.example.Skill_Hub_Learning_Platform.application.dto.request.CourseRequest;
import com.example.Skill_Hub_Learning_Platform.application.dto.request.UpdateCourseRequest;
import com.example.Skill_Hub_Learning_Platform.application.dto.response.CourseResponse;
import com.example.Skill_Hub_Learning_Platform.application.dto.response.InstructorDashboardResponse;
import com.example.Skill_Hub_Learning_Platform.application.dto.response.InstructorDashboardResponse.RecentEnrollment;
import com.example.Skill_Hub_Learning_Platform.application.dto.response.InstructorDashboardResponse.MonthlyEnrollmentTrend;
import com.example.Skill_Hub_Learning_Platform.application.exceptions.BadRequestException;
import com.example.Skill_Hub_Learning_Platform.application.exceptions.ResourceNotFoundException;
import com.example.Skill_Hub_Learning_Platform.application.mapper.CourseMapper;
import com.example.Skill_Hub_Learning_Platform.domain.enums.CourseLevel;
import com.example.Skill_Hub_Learning_Platform.domain.enums.CourseStatus;
import com.example.Skill_Hub_Learning_Platform.domain.enums.Role;
import com.example.Skill_Hub_Learning_Platform.domain.models.Course;
import com.example.Skill_Hub_Learning_Platform.domain.models.Enrollment;
import com.example.Skill_Hub_Learning_Platform.domain.models.User;
import com.example.Skill_Hub_Learning_Platform.infrastructure.repository.CourseRepository;
import com.example.Skill_Hub_Learning_Platform.infrastructure.repository.EnrollmentRepository;
import com.example.Skill_Hub_Learning_Platform.infrastructure.repository.ReviewRepository;
import com.example.Skill_Hub_Learning_Platform.infrastructure.repository.UserRepository;
import com.example.Skill_Hub_Learning_Platform.application.cache.CacheConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = {CacheConstants.ALL, CacheConstants.PUBLISHED}, allEntries = true),
            @CacheEvict(cacheNames = CacheConstants.INSTRUCTOR_DASHBOARD, key = "#instructorEmail")
    })
    public CourseResponse createCourse(CourseRequest request, String instructorEmail) {
        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + instructorEmail));
        Course course = courseMapper.toEntity(request);
        course.setInstructor(instructor);
        return courseMapper.toCreationResponse(courseRepository.save(course));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConstants.COURSE, key = "#id"),
            @CacheEvict(cacheNames = {CacheConstants.ALL, CacheConstants.PUBLISHED}, allEntries = true),
            @CacheEvict(cacheNames = CacheConstants.INSTRUCTOR_DASHBOARD, key = "#instructorEmail")
    })
    public CourseResponse updateCourse(Long id, UpdateCourseRequest request, String instructorEmail) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));

        validateOwnership(course, instructorEmail);
        applyUpdates(course, request);

        User user = userRepository.findByEmail(instructorEmail).orElse(null);
        Long currentUserId = user != null ? user.getId() : null;
        return courseMapper.toResponse(courseRepository.save(course), currentUserId);
    }

    @Override
    @Caching(
            put  = { @CachePut(cacheNames = CacheConstants.COURSE, key = "#id") },
            evict = {
                    @CacheEvict(cacheNames = {CacheConstants.ALL, CacheConstants.PUBLISHED}, allEntries = true),
                    @CacheEvict(cacheNames = CacheConstants.INSTRUCTOR_DASHBOARD, key = "#instructorEmail")
            }
    )
    public CourseResponse publishCourse(Long id, String instructorEmail) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));

        validateOwnership(course, instructorEmail);

        if (course.getStatus() == CourseStatus.PUBLISHED) {
            throw new BadRequestException("Course is already published");
        }

        course.setStatus(CourseStatus.PUBLISHED);
        User user = userRepository.findByEmail(instructorEmail).orElse(null);
        Long currentUserId = user != null ? user.getId() : null;
        return courseMapper.toResponse(courseRepository.save(course), currentUserId);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConstants.COURSE, key = "#id"),
            @CacheEvict(cacheNames = {CacheConstants.ALL, CacheConstants.PUBLISHED}, allEntries = true),
            @CacheEvict(cacheNames = CacheConstants.INSTRUCTOR_DASHBOARD, key = "#instructorEmail")
    })
    public void deleteCourse(Long id, String instructorEmail) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));

        validateOwnership(course, instructorEmail);
        courseRepository.delete(course);
    }


    @Transactional(readOnly = true)
    @Override
    @Cacheable(cacheNames = CacheConstants.COURSE, key = "#id + (#userEmail != null ? ':' + #userEmail : '')",
               unless = "#result.status.name() != 'PUBLISHED'")
    public CourseResponse getCourseById(Long id, String userEmail) {
        log.info("CACHE_MISS — fetching course {} from database", id);
        Course course = courseRepository.findCourseWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));

        // Security check: Only the course instructor can view unpublished courses
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            boolean isOwner =
                    userEmail != null
                            && course.getInstructor() != null
                            && userEmail.equals(course.getInstructor().getEmail());
            if (!isOwner) {
                throw new ResourceNotFoundException("Course", id);
            }
        }

        Long currentUserId = null;
        if (userEmail != null) {
            currentUserId = userRepository.findByEmail(userEmail)
                    .map(User::getId)
                    .orElse(null);
        }
        return courseMapper.toResponse(course, currentUserId);
    }

    @Transactional(readOnly = true)
    @Override
    @Cacheable(cacheNames = CacheConstants.ALL)
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAllWithSectionsAndLessons()
                .stream()
                .map(courseMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    @Cacheable(cacheNames = CacheConstants.PUBLISHED)
    public List<CourseResponse> getAllPublishedCourses() {
        return courseRepository.findWithSectionsAndLessonsByStatusEquals(CourseStatus.PUBLISHED)
                .stream()
                .map(courseMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<CourseResponse> getMyCourses(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return courseRepository.findByInstructorId(user.getId())
                .stream()
                .map(course -> courseMapper.toResponse(course, user.getId()))
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<CourseResponse> getPublishedCoursesByInstructorId(Long instructorId) {
        if (!userRepository.existsById(instructorId)) {
            throw new ResourceNotFoundException("User", instructorId);
        }

        return courseRepository.findByStatusEqualsAndInstructorId(CourseStatus.PUBLISHED, instructorId)
                .stream()
                .map(courseMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<CourseResponse> getCoursesByLevel(CourseLevel level) {
        return courseRepository.findByLevel(level)
                .stream()
                .map(courseMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<CourseResponse> getPublishedCoursesByLevel(CourseLevel level) {
        return courseRepository.findByStatusEqualsAndLevel(CourseStatus.PUBLISHED, level)
                .stream()
                .map(courseMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<CourseResponse> getCoursesByStatus(CourseStatus status, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Course> courses;

        if (status == CourseStatus.PUBLISHED) {
            courses = courseRepository.findByStatus(status);
        } else {
            if (user.getRole() == Role.Admin) {
                courses = courseRepository.findByStatus(status);
            } else {
                courses = courseRepository.findByStatusAndInstructorId(status, user.getId());
            }
        }

        return courses.stream()
                .map(course -> courseMapper.toResponse(course, user.getId()))
                .toList();
    }


    @Transactional(readOnly = true)
    @Override
    @Cacheable(cacheNames = CacheConstants.INSTRUCTOR_DASHBOARD, key = "#instructorEmail")
    public InstructorDashboardResponse getDashboard(String instructorEmail) {
        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + instructorEmail));

        Long instructorId = instructor.getId();
        List<Course> courses = courseRepository.findByInstructorId(instructorId);

        // Aggregate: review counts per course (1 query instead of N)
        List<Long> courseIds = courses.stream().map(Course::getId).toList();
        Map<Long, Long> reviewCountMap = courseIds.isEmpty()
                ? Map.of()
                : reviewRepository.countByCourseIds(courseIds).stream()
                        .collect(Collectors.toMap(
                                row -> (Long) row[0],
                                row -> (Long) row[1]
                        ));

        // Aggregate: enrollment stats (2 aggregate queries)
        long totalEnrollments = enrollmentRepository.countByInstructorId(instructorId);
        long totalStudents = enrollmentRepository.countDistinctStudentsByInstructorId(instructorId);

        // Course-level aggregations (computed in Java from already-fetched courses)
        long totalCourses = courses.size();
        long publishedCourses = courses.stream()
                .filter(c -> c.getStatus() == CourseStatus.PUBLISHED)
                .count();

        BigDecimal totalRevenue = courses.stream()
                .map(c -> c.getPrice().multiply(
                        BigDecimal.valueOf(c.getTotalEnrollments())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double avgRating = courses.stream()
                .mapToDouble(c -> c.getAverageRating() != null ? c.getAverageRating() : 0.0)
                .average()
                .orElse(0.0);

        double totalWeightedRating = 0.0;
        long totalReviewCount = 0;
        for (Course course : courses) {
            double r = course.getAverageRating() != null ? course.getAverageRating() : 0.0;
            long rc = reviewCountMap.getOrDefault(course.getId(), 0L);
            totalWeightedRating += r * rc;
            totalReviewCount += rc;
        }
        double weightedAverageRating = totalReviewCount > 0
                ? totalWeightedRating / totalReviewCount
                : 0.0;

        // Recent enrollments (1 query, limited)
        List<RecentEnrollment> recentEnrollments = enrollmentRepository
                .findRecentByInstructorId(instructorId, PageRequest.of(0, 5))
                .stream()
                .map(e -> new RecentEnrollment(
                        e.getStudent().getName(),
                        e.getCourse().getTitle(),
                        e.getCreatedAt()))
                .toList();

        // Top performing courses (sorted by rating, limited)
        List<CourseResponse> topPerformingCourses = courses.stream()
                .sorted(Comparator.comparingDouble(
                        (Course c) -> c.getAverageRating() != null ? -c.getAverageRating() : 0.0)
                        .thenComparingLong(Course::getTotalEnrollments))
                .limit(5)
                .map(c -> courseMapper.toResponse(c, instructorId))
                .toList();

        // Enrollment trend (1 aggregate query)
        List<MonthlyEnrollmentTrend> enrollmentTrend = enrollmentRepository
                .findEnrollmentTrendByInstructorId(instructorId)
                .stream()
                .map(row -> new MonthlyEnrollmentTrend(
                        ((Number) row[0]).intValue(),
                        ((Number) row[1]).intValue(),
                        ((Number) row[2]).longValue()))
                .toList();

        return new InstructorDashboardResponse(
                totalStudents, totalCourses, totalEnrollments, publishedCourses,
                totalRevenue, avgRating, weightedAverageRating,
                recentEnrollments, topPerformingCourses, enrollmentTrend
        );
    }

    public boolean isInstructor(Long courseId, String userEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));
        return course.getInstructor().getEmail().equals(userEmail);
    }



    private void applyUpdates(Course course, UpdateCourseRequest request) {
        if (request.getTitle() != null)       course.setTitle(request.getTitle());
        if (request.getDescription() != null) course.setDescription(request.getDescription());
        if (request.getPrice() != null)       course.setPrice(request.getPrice());
        if (request.getLevel() != null)       course.setLevel(request.getLevel());
        if (request.getStatus() != null) {
            CourseStatus currentStatus = course.getStatus();
            CourseStatus newStatus     = request.getStatus();
            if (currentStatus == CourseStatus.PUBLISHED && newStatus != CourseStatus.PUBLISHED) {
                throw new BadRequestException(
                        "Cannot change status of published course. Only DRAFT courses can change status."
                );
            }
            course.setStatus(newStatus);
        }
    }

    private void validateOwnership(Course course, String instructorEmail) {
        User user = userRepository.findByEmail(instructorEmail).orElse(null);
        if (user != null && user.getRole() == Role.Admin) {
            return;
        }
        if (course.getInstructor() == null || !course.getInstructor().getEmail().equals(instructorEmail)) {
            throw new BadRequestException("You do not have permission to modify this course");
        }
    }

    private boolean isAdmin(User user) {
        return user.getRole() == Role.Admin;
    }

    private boolean isOwner(User user, Course course) {
        return course.getInstructor().getId().equals(user.getId());
    }
}
