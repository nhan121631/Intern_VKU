package com.vku.job.dtos.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskForUserResponse {
    private String user;
    private Long total;
}
