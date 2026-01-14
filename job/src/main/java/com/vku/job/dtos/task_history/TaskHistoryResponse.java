package com.vku.job.dtos.task_history;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TaskHistoryResponse {
    private Long id;
    private String updateBy;
    private LocalDateTime updatedAt;
}
