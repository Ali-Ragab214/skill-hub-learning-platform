package com.example.Skill_Hub_Learning_Platform.domain.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "lessons")
public class Lesson extends BaseEntity {

    @Column(nullable = false)
    //@Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters")
    private String title;

    @Column(nullable = false)
    private String videoUrl;

    @Column(nullable = false)
    @Min(value = 0, message = "Duration cannot be negative")
    private Integer duration;

    @Column(nullable = false)
    private Boolean isPreview = false;

    @Column(nullable = false)
    @Min(value = 0, message = "Order index cannot be negative")
    private Integer orderIndex = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;
}