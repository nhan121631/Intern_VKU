package com.vku.job.dtos.task_history;

import java.time.LocalDate;

import com.vku.job.enums.TaskStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTaskHistoryResponseDto {
    private Long id;
    private String title;
    private String description;
    private boolean allowUserUpdate;
    private TaskStatus status;
    private LocalDate deadline;
    private Long assignedUserId;
}
