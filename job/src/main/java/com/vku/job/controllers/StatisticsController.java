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

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private JwtService jwtService;

    @GetMapping("/summary-by-status")
    public List<TaskSummaryResponse> getTaskSummaryByStatus(
            @RequestParam(value = "createdAtFrom", required = false) String createdAtFrom,
            @RequestParam(value = "createdAtTo", required = false) String createdAtTo) {
        return statisticsService.getTaskSummaryByStatus(createdAtFrom, createdAtTo);
    }

    @GetMapping("/summary-by-user")
    public List<TaskForUserResponse> getTaskSummaryByUser(
            @RequestParam(value = "createdAtFrom", required = false) String createdAtFrom,
            @RequestParam(value = "createdAtTo", required = false) String createdAtTo) {
        return statisticsService.getTaskSummaryByUser(createdAtFrom, createdAtTo);
    }

    @GetMapping("/summary-by-user-id")
    public List<TaskSummaryResponse> getTaskStatisticsByUser(@RequestParam("userId") Long userId,
            @RequestParam(value = "createdAtFrom", required = false) String createdAtFrom,
            @RequestParam(value = "createdAtTo", required = false) String createdAtTo) {
        return statisticsService.getTaskStatisticsByUser(userId, createdAtFrom, createdAtTo);
    }

    @GetMapping("/summary-me")
    public List<TaskSummaryResponse> getMyTaskStatistics(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(value = "createdAtFrom", required = false) String createdAtFrom,
            @RequestParam(value = "createdAtTo", required = false) String createdAtTo) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtService.extractUserIdFromToken(token);
        return statisticsService.getTaskStatisticsByUser(userId, createdAtFrom, createdAtTo);
    }

    @GetMapping("/test-email")
    public String testEmail() {
        statisticsService.exportWeeklyTaskReportPdf();
        return "Email sent";
    }
}
