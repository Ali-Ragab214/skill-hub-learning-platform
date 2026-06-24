package com.example.Skill_Hub_Learning_Platform.domain.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.BatchSize;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "sections" ,
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"course_id", "title"}
                )
        }
)
public class Section extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Builder.Default
    private Integer orderIndex = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 25)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<Lesson> lessons = new ArrayList<>();
}