package com.vku.job.dtos.statistics;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeeklyReportData {
    LocalDate weekStart;
    LocalDate weekEnd;

    List<TaskSummaryResponse> summaryByStatus;
    List<TaskForUserResponse> summaryByUser;
    List<TaskStatutesUserResponse> summaryByUserId;
}
