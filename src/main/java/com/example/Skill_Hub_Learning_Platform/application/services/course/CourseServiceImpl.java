package com.example.Skill_Hub_Learning_Platform.application.services.course;
import com.example.Skill_Hub_Learning_Platform.application.dto.request.CourseRequest;
import com.example.Skill_Hub_Learning_Platform.application.dto.request.UpdateCourseRequest;
import com.example.Skill_Hub_Learning_Platform.application.dto.response.CourseResponse;
import com.example.Skill_Hub_Learning_Platform.application.exceptions.BadRequestException;
import com.example.Skill_Hub_Learning_Platform.application.exceptions.ResourceNotFoundException;
import com.example.Skill_Hub_Learning_Platform.application.mapper.CourseMapper;
import com.example.Skill_Hub_Learning_Platform.domain.enums.CourseLevel;
import com.example.Skill_Hub_Learning_Platform.domain.enums.CourseStatus;
import com.example.Skill_Hub_Learning_Platform.domain.enums.Role;
import com.example.Skill_Hub_Learning_Platform.domain.models.Course;
import com.example.Skill_Hub_Learning_Platform.domain.models.User;
import com.example.Skill_Hub_Learning_Platform.infrastructure.repository.CourseRepository;
import com.example.Skill_Hub_Learning_Platform.infrastructure.repository.LessonRepository;
import com.example.Skill_Hub_Learning_Platform.infrastructure.repository.SectionRepository;
import com.example.Skill_Hub_Learning_Platform.infrastructure.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.LinkedTransferQueue;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;
    private  final CourseMapper courseMapper;
    private  final UserRepository userRepository;

    @Override
    public CourseResponse createCourse(CourseRequest request, String instructorEmail) {
        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + instructorEmail));
        Course course = courseMapper.toEntity(request);
        course.setInstructor(instructor);
        return courseMapper.toCreationResponse(courseRepository.save(course));
    }

    @Transactional(readOnly = true)
    @Override
    public CourseResponse getCourseById(Long id, String userEmail) {

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));

        if (course.getStatus() != CourseStatus.PUBLISHED) {

            boolean isOwner =
                    userEmail != null
                            && course.getInstructor() != null
                            && userEmail.equals(course.getInstructor().getEmail());

            if (!isOwner) {
                throw new ResourceNotFoundException("Course", id);
            }
        }

        return courseMapper.toResponse(course);
    }


    @Transactional(readOnly = true)
    @Override
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll()
                .stream()
                .map(courseMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<CourseResponse> getMyCourses(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        return courseRepository.findByInstructorId(user.getId())
                .stream()
                .map(courseMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<CourseResponse> getAllPublishedCourses() {
        return courseRepository.findByStatusEquals(CourseStatus.PUBLISHED)
                .stream()
                .map(courseMapper::toResponse)
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
        return  courseRepository.findByLevel(level)
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
    public List<CourseResponse> getCoursesByStatus(
            CourseStatus status,
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        List<Course> courses;

        // 1. Public case tht any one can see them
        if (status == CourseStatus.PUBLISHED) {
            courses = courseRepository.findByStatus(status);
        }

        // 2. Private statuses (DRAFT / ARCHIVED)
        else {

            if (user.getRole() == Role.Admin) {
                // Admin sees all
                courses = courseRepository.findByStatus(status);
            } else {
                // Instructor sees only his own
                courses = courseRepository.findByStatusAndInstructorId(
                        status,
                        user.getId()
                );
            }
        }

        return courses.stream()
                .map(courseMapper::toResponse)
                .toList();
    }


    @Override
    public CourseResponse updateCourse(Long id, UpdateCourseRequest request, String instructorEmail) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));

        // Validate ownership
        validateOwnership(course, instructorEmail);

        applyUpdates(course, request);

        return courseMapper.toResponse(courseRepository.save(course));
    }


    @Override
    public CourseResponse publishCourse(Long id, String instructorEmail) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));

        // Validate ownership
        validateOwnership(course, instructorEmail);

        if (course.getStatus() == CourseStatus.PUBLISHED) {
            throw new BadRequestException("Course is already published");
        }

        course.setStatus(CourseStatus.PUBLISHED);
        return courseMapper.toResponse(courseRepository.save(course));
    }

    @Override
    public void deleteCourse(Long id, String instructorEmail) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));

        // Validate ownership
        validateOwnership(course, instructorEmail);
        courseRepository.delete(course);
    }


    private void applyUpdates(Course course, UpdateCourseRequest request) {
        if (request.getTitle() != null) course.setTitle(request.getTitle());
        if (request.getDescription() != null) course.setDescription(request.getDescription());
        if (request.getPrice() != null) course.setPrice(request.getPrice());
        if (request.getLevel() != null) course.setLevel(request.getLevel());
        if (request.getStatus() != null) {
            // Status transition rules: only allow certain transitions
            CourseStatus currentStatus = course.getStatus();
            CourseStatus newStatus = request.getStatus();

            // Allow transitions from DRAFT to other statuses via the publish endpoint
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

    public boolean isInstructor(Long courseId, String userEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));
        return course.getInstructor().getEmail().equals(userEmail);
    }

    private boolean isAdmin(User user) {
        return user.getRole() == Role.Admin;
    }

    private boolean isOwner(User user, Course course) {
        return course.getInstructor().getId().equals(user.getId());
    }
}
