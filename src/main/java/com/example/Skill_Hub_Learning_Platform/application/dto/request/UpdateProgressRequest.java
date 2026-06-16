package com.example.Skill_Hub_Learning_Platform.application.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProgressRequest {
    @NotNull(message = "Progress percentage is required")
    @Min(value = 0, message = "Progress percentage cannot be negative")
    @Max(value = 100, message = "Progress percentage cannot exceed 100")
    private Integer progressPercentage;
}