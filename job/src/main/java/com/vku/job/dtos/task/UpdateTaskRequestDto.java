package com.vku.job.dtos.task;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateTaskRequestDto {
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Deadline is required")
    // @FutureOrPresent(message = "Deadline must be today or in the future")
    private LocalDate deadline;

    private boolean allowUserUpdate;

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(OPEN|IN_PROGRESS|DONE|CANCELED)$", message = "Status must be one of: OPEN, IN_PROGRESS, DONE, CANCELED")
    private String status;

    @NotNull(message = "Assigned user ID is required")
    private Long assignedUserId;

}
