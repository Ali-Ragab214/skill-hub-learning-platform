package com.example.Skill_Hub_Learning_Platform.application.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LessonRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters")
    private String title;

    @NotBlank(message = "Video URL is required")
    @Pattern(
            regexp = "^(https?://).*",
            message = "Video URL must start with http:// or https://"
    )
    private String videoUrl;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 300, message = "Duration cannot exceed 300 minutes")
    private Integer duration;

    @NotNull(message = "isPreview is required")
    @Builder.Default
    private Boolean isPreview = false;

    @Min(value = 0, message = "Order index cannot be negative")
    @Builder.Default
    private Integer orderIndex = 0;
}