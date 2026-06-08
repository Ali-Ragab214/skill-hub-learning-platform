package com.example.Skill_Hub_Learning_Platform.domain.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
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
    //@Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters")
    private String title;

    @Column(nullable = false)
    //@Min(value = 0, message = "Order index cannot be negative")
    private Integer orderIndex = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<Lesson> lessons = new ArrayList<>();
}