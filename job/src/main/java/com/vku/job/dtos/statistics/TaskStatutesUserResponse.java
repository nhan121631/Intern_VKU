package com.vku.job.dtos.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskStatutesUserResponse {
    private String fullName;
    private String status;
    private Long value;
}
