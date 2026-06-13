package com.example.Skill_Hub_Learning_Platform.domain.models;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "lesson_progress",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_lesson_progress_student_lesson",
                        columnNames = {"student_id", "lesson_id"}
                )
        }
)
public class LessonProgress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;
}