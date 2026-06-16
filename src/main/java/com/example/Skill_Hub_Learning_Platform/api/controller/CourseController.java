package com.example.Skill_Hub_Learning_Platform.api.controller;

import com.example.Skill_Hub_Learning_Platform.application.dto.request.CourseRequest;
import com.example.Skill_Hub_Learning_Platform.application.dto.request.UpdateCourseRequest;
import com.example.Skill_Hub_Learning_Platform.application.dto.response.CourseResponse;
import com.example.Skill_Hub_Learning_Platform.application.responses.ApiResponse;
import com.example.Skill_Hub_Learning_Platform.application.services.course.CourseService;
import com.example.Skill_Hub_Learning_Platform.application.services.enrollment.EnrollmentService;
import com.example.Skill_Hub_Learning_Platform.domain.enums.CourseLevel;
import com.example.Skill_Hub_Learning_Platform.domain.enums.CourseStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private  final CourseService courseService;
    private  final EnrollmentService enrollmentService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseById(
            @PathVariable Long id,
            Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(
                ApiResponse.success(courseService.getCourseById(id, email), "Course retrieved successfully")
        );
    }


    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getCoursesByInstructorId(@PathVariable Long instructorId) {
        // Public endpoint - returns only published courses by instructor
        return ResponseEntity.ok(
                ApiResponse.success(courseService.getPublishedCoursesByInstructorId(instructorId), "Courses retrieved successfully")
        );
    }

    @GetMapping("/level")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getCoursesByLevel(@RequestParam CourseLevel level) {
        // Public endpoint - returns only published courses by level
        return ResponseEntity.ok(
                ApiResponse.success(courseService.getPublishedCoursesByLevel(level), "Courses retrieved successfully")
        );
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getCoursesByStatus(
            @RequestParam CourseStatus status,
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                ApiResponse.success(
                        courseService.getCoursesByStatus(status, email),
                        "Courses retrieved successfully"
                )
        );
    }

    //temporary endpoint to get all courses, we will use pagination and filtering later
//    @GetMapping
//    public ResponseEntity<ApiResponse<List<CourseResponse>>> getAllCourses()
//    {
//        // Public endpoint - returns only published courses
//        return ResponseEntity.ok(
//                ApiResponse.success(courseService.getAllPublishedCourses(), "Courses retrieved successfully")
//        );
//    }

//    @GetMapping("/admin/all")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')")
//    public ResponseEntity<ApiResponse<List<CourseResponse>>> getAllCoursesAdminAndInstructor()
//    {
//        // Admin endpoint - returns all courses (including draft)
//        return ResponseEntity.ok(
//                ApiResponse.success(courseService.getAllCourses(), "All courses retrieved successfully")
//        );
//    }


    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(
            @RequestBody @Valid CourseRequest request,
            Authentication authentication) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        courseService.createCourse(request, authentication.getName()),
                        "Course created successfully"
                ));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            @PathVariable Long id,
            @RequestBody @Valid UpdateCourseRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                courseService.updateCourse(id, request, authentication.getName()),
                "Course updated successfully"
        ));
    }



    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponse>> publishCourse(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                courseService.publishCourse(id, authentication.getName()),
                "Course published successfully"
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(
            @PathVariable Long id,
            Authentication authentication) {
        courseService.deleteCourse(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(null, "Course deleted successfully"));
    }

    @GetMapping("/my-courses")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getMyCourses(Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                ApiResponse.success(
                        courseService.getMyCourses(email),
                        "Courses retrieved successfully"
                )
        );
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getAllCoursesAdmin() {
        return ResponseEntity.ok(
                ApiResponse.success(courseService.getAllCourses(), "All courses retrieved successfully")
        );
    }

    //to calculate total enrolls vai each course
    @GetMapping("/{courseId}/enrollments/count")
    public ResponseEntity<ApiResponse<Long>> getEnrollmentCount(
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        enrollmentService.getEnrollmentCount(courseId),
                        "Enrollment count retrieved successfully"
                )
        );
    }

}
