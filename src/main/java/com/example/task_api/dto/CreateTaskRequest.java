package com.example.task_api.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;

public record CreateTaskRequest(
        @NotBlank(message = "Title is required")
        String title,

        String description
) {
    @AssertTrue(message = "Title must contain between 1 and 120 characters after trim")
    public boolean isTitleLengthValid() {
        return title != null && !title.strip().isEmpty() && title.strip().length() <= 120;
    }
}
