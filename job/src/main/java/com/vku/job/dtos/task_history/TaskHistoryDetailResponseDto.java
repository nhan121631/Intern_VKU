package com.vku.job.dtos.task_history;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class TaskHistoryDetailResponseDto {

    private Long id;
    private String updatedByName;
    private List<String> roles;
    private String oldData;
    private String newData;
    private LocalDateTime updatedAt;
}
