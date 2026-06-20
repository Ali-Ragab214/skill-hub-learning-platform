package com.example.Skill_Hub_Learning_Platform.application.services.progress;

import com.example.Skill_Hub_Learning_Platform.application.dto.response.LessonProgressResponse;

import java.util.List;

public interface LessonProgressService {

    LessonProgressResponse markLessonCompleted(Long lessonId, String studentEmail);

    LessonProgressResponse markLessonIncomplete(Long lessonId, String studentEmail);

    List<LessonProgressResponse> getCourseProgress(Long courseId, String studentEmail);

    LessonProgressResponse getLessonProgress(Long lessonId, String studentEmail);
}
