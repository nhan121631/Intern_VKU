package com.vku.job.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vku.job.dtos.statistics.TaskForUserResponse;
import com.vku.job.dtos.statistics.TaskSummaryResponse;
import com.vku.job.services.StatisticsService;
import com.vku.job.services.auth.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/statistics")
@Tag(name = "Statistics", description = "APIs for task statistics and reports")
@SecurityRequirement(name = "bearerAuth")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private JwtService jwtService;

    @GetMapping("/summary-by-status")
    @Operation(summary = "Get Task Summary by Status", description = "Retrieve task summary grouped by status within an optional date range")
    public List<TaskSummaryResponse> getTaskSummaryByStatus(
            @RequestParam(value = "createdAtFrom", required = false) String createdAtFrom,
            @RequestParam(value = "createdAtTo", required = false) String createdAtTo) {
        return statisticsService.getTaskSummaryByStatus(createdAtFrom, createdAtTo);
    }

    @GetMapping("/summary-by-user")
    @Operation(summary = "Get Task Summary by User", description = "Retrieve task summary grouped by user within an optional date range")
    public List<TaskForUserResponse> getTaskSummaryByUser(
            @RequestParam(value = "createdAtFrom", required = false) String createdAtFrom,
            @RequestParam(value = "createdAtTo", required = false) String createdAtTo) {
        return statisticsService.getTaskSummaryByUser(createdAtFrom, createdAtTo);
    }

    @GetMapping("/summary-by-user-id")
    @Operation(summary = "Get Task Summary for a User", description = "Retrieve task summary for a specific user within an optional date range")
    public List<TaskSummaryResponse> getTaskStatisticsByUser(@RequestParam("userId") Long userId,
            @RequestParam(value = "createdAtFrom", required = false) String createdAtFrom,
            @RequestParam(value = "createdAtTo", required = false) String createdAtTo) {
        return statisticsService.getTaskStatisticsByUser(userId, createdAtFrom, createdAtTo);
    }

    @GetMapping("/summary-me")
    @Operation(summary = "Get My Task Summary", description = "Retrieve task summary for the authenticated user within an optional date range")
    public List<TaskSummaryResponse> getMyTaskStatistics(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(value = "createdAtFrom", required = false) String createdAtFrom,
            @RequestParam(value = "createdAtTo", required = false) String createdAtTo) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtService.extractUserIdFromToken(token);
        return statisticsService.getTaskStatisticsByUser(userId, createdAtFrom, createdAtTo);
    }

    @GetMapping("/test-email")
    @Operation(summary = "Test Email Sending", description = "Trigger a test email sending for weekly task report")
    public String testEmail() {
        statisticsService.exportWeeklyTaskReportPdf();
        return "Email sent";
    }
}
