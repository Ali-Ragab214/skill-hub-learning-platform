package com.example.Skill_Hub_Learning_Platform.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record InstructorDashboardResponse(
        Long totalStudentsEnrolled,
        Long totalCourses,
        Long totalEnrollments,
        Long totalPublishedCourses,
        BigDecimal totalRevenue,
        Double averageCourseRating,
        Double weightedAverageRating,
        List<RecentEnrollment> recentEnrollments,
        List<CourseResponse> topPerformingCourses,
        List<MonthlyEnrollmentTrend> enrollmentTrend
) {
    public record RecentEnrollment(String studentName, String courseTitle, LocalDateTime enrolledAt) {}

    public record MonthlyEnrollmentTrend(int year, int month, long count) {}
}
