package com.vku.job.dtos.task;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTaskRequestDto {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Deadline is required")
    @FutureOrPresent(message = "Deadline must be today or in the future")
    private LocalDate deadline;

    // @Pattern(regexp = "^(OPEN|IN_PROGRESS|DONE|CANCELED)$", message = "Status
    // must be one of: OPEN, IN_PROGRESS, DONE, CANCELED")
    // private String status; // OPEN, IN_PROGRESS, DONE, CANCELED

    @NotNull(message = "Assigned user ID is required")
    private Long assignedUserId;

}
