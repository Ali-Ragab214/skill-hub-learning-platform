package com.example.Skill_Hub_Learning_Platform.application.services.course;
import com.example.Skill_Hub_Learning_Platform.application.dto.request.CourseRequest;
import com.example.Skill_Hub_Learning_Platform.application.dto.request.UpdateCourseRequest;
import com.example.Skill_Hub_Learning_Platform.application.dto.response.CourseResponse;
import com.example.Skill_Hub_Learning_Platform.domain.enums.CourseLevel;
import com.example.Skill_Hub_Learning_Platform.domain.enums.CourseStatus;
import java.util.List;

public interface CourseService {

    CourseResponse createCourse(CourseRequest request, String instructorEmail);

    CourseResponse getCourseById(Long id, String userEmail);

    List<CourseResponse> getAllCourses();

    // Get only published courses (for public access)
    List<CourseResponse> getAllPublishedCourses();

    //List<CourseResponse> getCoursesByInstructorId(Long instructorId);

    // Get only published courses by instructor (for public access)
    List<CourseResponse> getPublishedCoursesByInstructorId(Long instructorId);

    List<CourseResponse> getCoursesByLevel(CourseLevel level);

    // Get only published courses by level (for public access)
    List<CourseResponse> getPublishedCoursesByLevel(CourseLevel level);

    List<CourseResponse> getCoursesByStatus(CourseStatus status,String email);

    CourseResponse updateCourse(Long id, UpdateCourseRequest request, String instructorEmail);
    List<CourseResponse> getMyCourses(String instructorEmail);

    CourseResponse publishCourse(Long id, String instructorEmail);
    void deleteCourse(Long id, String instructorEmail);

    // Check if user is instructor of a course
    boolean isInstructor(Long courseId, String userEmail);

   // List<CourseResponse> getCoursesByInstructorEmail(String email);
}