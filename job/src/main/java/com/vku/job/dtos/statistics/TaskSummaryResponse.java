package com.vku.job.dtos.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TaskSummaryResponse {
    private String status;
    private Long value;
}
