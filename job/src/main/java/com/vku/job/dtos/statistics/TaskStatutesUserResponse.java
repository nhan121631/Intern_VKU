package com.vku.job.dtos.statistics;

import lombok.Data;

@Data
public class TaskStatutesUserResponse {
    private String fullName;
    private String status;
    private Long value;
}
