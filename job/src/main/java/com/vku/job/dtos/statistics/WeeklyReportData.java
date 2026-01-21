package com.vku.job.dtos.statistics;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class WeeklyReportData {
    LocalDate weekStart;
    LocalDate weekEnd;

    List<TaskSummaryResponse> summaryByStatus;
    List<TaskForUserResponse> summaryByUser;
    List<TaskStatutesUserResponse> summaryByUserId;
}
