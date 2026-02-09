package com.vku.job.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vku.job.dtos.statistics.TaskForUserResponse;
import com.vku.job.dtos.statistics.TaskStatutesUserResponse;
import com.vku.job.dtos.statistics.TaskSummaryResponse;
import com.vku.job.dtos.statistics.WeeklyReportData;
import com.vku.job.enums.TaskStatus;
import com.vku.job.repositories.TaskJpaRepository;
import com.vku.job.repositories.UserJpaRepository;
import com.vku.job.repositories.projection.TaskForUserProjection;
import com.vku.job.repositories.projection.TaskStatusUserProjection;
import com.vku.job.repositories.projection.TaskSummaryProjection;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for generating task statistics and reports.
 * Provides statistics by status, user, and date ranges.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {
        private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        private final TaskJpaRepository taskJpaRepository;
        private final TaskPdfService taskPdfService;
        private final MailService mailService;
        private final UserJpaRepository userJpaRepository;

        /**
         * Validates and parses date range parameters.
         * 
         * @param createdAtFrom start date string
         * @param createdAtTo   end date string
         * @return DateRange object with validated start and end datetime, or null if
         *         dates not provided
         * @throws IllegalArgumentException if date format is invalid or range is
         *                                  invalid
         */
        private DateRange validateAndParseDateRange(String createdAtFrom, String createdAtTo) {
                if (createdAtFrom == null || createdAtTo == null ||
                                createdAtFrom.isBlank() || createdAtTo.isBlank()) {
                        return null;
                }

                LocalDate fromDate = parseDate(createdAtFrom, "createdAtFrom");
                LocalDate toDate = parseDate(createdAtTo, "createdAtTo");

                if (fromDate.isAfter(toDate)) {
                        throw new IllegalArgumentException(
                                        "createdAtFrom cannot be after createdAtTo");
                }

                LocalDateTime startDateTime = fromDate.atStartOfDay();
                LocalDateTime endDateTime = toDate.plusDays(1).atStartOfDay().minusNanos(1);

                log.debug("Validated date range: {} to {}", startDateTime, endDateTime);
                return new DateRange(startDateTime, endDateTime);
        }

        /**
         * Parses a date string in yyyy-MM-dd format.
         */
        private LocalDate parseDate(String dateStr, String fieldName) {
                if (dateStr == null || dateStr.isBlank()) {
                        throw new IllegalArgumentException(fieldName + " cannot be blank");
                }

                try {
                        return LocalDate.parse(dateStr, DATE_FORMAT);
                } catch (DateTimeParseException e) {
                        log.warn("Invalid date format for {}: {}", fieldName, dateStr);
                        throw new IllegalArgumentException(
                                        fieldName + " must be in format yyyy-MM-dd", e);
                }
        }

        /**
         * Maps projections to response DTOs with all task statuses included.
         */
        private List<TaskSummaryResponse> mapToTaskSummaryWithAllStatuses(
                        List<TaskSummaryProjection> projections) {
                Map<TaskStatus, Long> countMap = projections.stream()
                                .collect(Collectors.toMap(
                                                p -> TaskStatus.valueOf(p.getStatus()),
                                                TaskSummaryProjection::getCount));

                return Arrays.stream(TaskStatus.values())
                                .map(status -> new TaskSummaryResponse(
                                                status.name(),
                                                countMap.getOrDefault(status, 0L)))
                                .toList();
        }

        /**
         * Maps user projection to response DTO.
         */
        private TaskForUserResponse mapToTaskForUserResponse(TaskForUserProjection projection) {
                return new TaskForUserResponse(
                                projection.getFullName(),
                                projection.getCount());
        }

        /**
         * Inner record for holding validated date range.
         */
        private record DateRange(LocalDateTime start, LocalDateTime end) {
        }

        /**
         * Get task statistics grouped by status.
         * Optionally filter by date range.
         *
         * @param createdAtFrom optional start date (yyyy-MM-dd)
         * @param createdAtTo   optional end date (yyyy-MM-dd)
         * @return list of task counts by status
         */
        public List<TaskSummaryResponse> getTaskSummaryByStatus(String createdAtFrom, String createdAtTo) {
                log.debug("Getting task summary by status. Date range: {} to {}", createdAtFrom, createdAtTo);

                DateRange dateRange = validateAndParseDateRange(createdAtFrom, createdAtTo);

                if (dateRange != null) {
                        List<TaskSummaryProjection> projections = taskJpaRepository
                                        .countTasksByStatusBetween(dateRange.start(), dateRange.end());
                        return projections.stream()
                                        .map(p -> new TaskSummaryResponse(p.getStatus(), p.getCount()))
                                        .toList();
                }

                List<TaskSummaryProjection> projections = taskJpaRepository.countTasksByStatus();
                return projections.stream()
                                .map(p -> new TaskSummaryResponse(p.getStatus(), p.getCount()))
                                .toList();
        }

        /**
         * Get task statistics grouped by user.
         * Optionally filter by date range.
         *
         * @param createdAtFrom optional start date (yyyy-MM-dd)
         * @param createdAtTo   optional end date (yyyy-MM-dd)
         * @return list of task counts by user
         */
        public List<TaskForUserResponse> getTaskSummaryByUser(String createdAtFrom, String createdAtTo) {
                log.debug("Getting task summary by user. Date range: {} to {}", createdAtFrom, createdAtTo);

                DateRange dateRange = validateAndParseDateRange(createdAtFrom, createdAtTo);

                if (dateRange != null) {
                        List<TaskForUserProjection> projections = taskJpaRepository
                                        .countTasksByUserBetween(dateRange.start(), dateRange.end());
                        return projections.stream()
                                        .map(this::mapToTaskForUserResponse)
                                        .toList();
                }

                List<TaskForUserProjection> projections = taskJpaRepository.countTasksByUser();
                return projections.stream()
                                .map(this::mapToTaskForUserResponse)
                                .toList();
        }

        /**
         * Get task statistics for a specific user, grouped by status.
         * Optionally filter by date range.
         *
         * @param userId        user ID (required)
         * @param createdAtFrom optional start date (yyyy-MM-dd)
         * @param createdAtTo   optional end date (yyyy-MM-dd)
         * @return list of task counts by status for the user
         * @throws IllegalArgumentException if userId is null
         */
        public List<TaskSummaryResponse> getTaskStatisticsByUser(
                        Long userId, String createdAtFrom, String createdAtTo) {
                if (userId == null) {
                        throw new IllegalArgumentException("userId is required");
                }

                log.debug("Getting task statistics for user {}. Date range: {} to {}",
                                userId, createdAtFrom, createdAtTo);

                DateRange dateRange = validateAndParseDateRange(createdAtFrom, createdAtTo);

                if (dateRange != null) {
                        List<TaskStatusUserProjection> projections = taskJpaRepository
                                        .countTasksByStatusByUserBetween(userId, dateRange.start(), dateRange.end());

                        Map<TaskStatus, Long> countMap = projections.stream()
                                        .collect(Collectors.toMap(
                                                        p -> TaskStatus.valueOf(p.getStatus()),
                                                        TaskStatusUserProjection::getValue));

                        return Arrays.stream(TaskStatus.values())
                                        .map(status -> new TaskSummaryResponse(
                                                        status.name(),
                                                        countMap.getOrDefault(status, 0L)))
                                        .toList();
                }

                List<TaskSummaryProjection> projections = taskJpaRepository
                                .countTasksByStatusByUser(userId);
                return mapToTaskSummaryWithAllStatuses(projections);
        }

        /**
         * Scheduled job to export and email weekly task reports.
         * Runs every Monday at 8 AM.
         */
        @Scheduled(cron = "0 0 8 ? * MON")
        public void exportWeeklyTaskReportPdf() {
                LocalDate today = LocalDate.now();
                LocalDate weekEnd = today.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY));
                LocalDate weekStart = weekEnd.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));

                log.info("Starting weekly task report export for week: {} to {}", weekStart, weekEnd);

                try {
                        LocalDateTime startDateTime = weekStart.atStartOfDay();
                        LocalDateTime endDateTime = weekEnd.plusDays(1).atStartOfDay().minusNanos(1);

                        WeeklyReportData reportData = buildWeeklyReportData(
                                        weekStart, weekEnd, startDateTime, endDateTime);

                        byte[] pdfBytes = taskPdfService.exportWeeklyTaskReportPdf(reportData);
                        sendReportToAdmins(pdfBytes, weekStart, weekEnd);

                        log.info("Successfully completed weekly task report export");
                } catch (Exception e) {
                        log.error("Failed to export weekly task report", e);
                        throw e;
                }
        }

        /**
         * Builds weekly report data from repository projections.
         */
        private WeeklyReportData buildWeeklyReportData(
                        LocalDate weekStart, LocalDate weekEnd,
                        LocalDateTime startDateTime, LocalDateTime endDateTime) {

                List<TaskSummaryResponse> summaryByStatus = getTaskSummaryByStatusBetween(startDateTime, endDateTime);

                List<TaskForUserResponse> summaryByUser = getTaskSummaryByUserBetween(startDateTime, endDateTime);

                List<TaskStatutesUserResponse> statusByUser = taskJpaRepository
                                .countTasksByStatusByAllUserBetween(startDateTime, endDateTime)
                                .stream()
                                .map(this::mapToTaskStatusUserResponse)
                                .toList();

                WeeklyReportData reportData = new WeeklyReportData();
                reportData.setWeekStart(weekStart);
                reportData.setWeekEnd(weekEnd);
                reportData.setSummaryByStatus(summaryByStatus);
                reportData.setSummaryByUser(summaryByUser);
                reportData.setSummaryByUserId(statusByUser);

                return reportData;
        }

        /**
         * Sends PDF report to all admin users.
         */
        private void sendReportToAdmins(byte[] pdfBytes, LocalDate weekStart, LocalDate weekEnd) {
                List<String> adminEmails = userJpaRepository.findAdminEmails();
                String subject = String.format("Weekly Task Report (%s → %s)", weekStart, weekEnd);
                String message = "Please find attached the weekly task report.";

                log.info("Sending weekly report to {} admin(s)", adminEmails.size());

                for (String email : adminEmails) {
                        try {
                                mailService.sendPdfReport(email, pdfBytes, subject, message);
                                log.debug("Report sent to: {}", email);
                        } catch (Exception e) {
                                log.error("Failed to send report to: {}", email, e);
                        }
                }
        }

        /**
         * Maps projection to TaskStatutesUserResponse DTO.
         */
        private TaskStatutesUserResponse mapToTaskStatusUserResponse(TaskStatusUserProjection projection) {
                TaskStatutesUserResponse dto = new TaskStatutesUserResponse();
                dto.setFullName(projection.getFullName());
                dto.setStatus(projection.getStatus());
                dto.setValue(projection.getValue());
                return dto;
        }

        /**
         * Get task statistics grouped by status for a specific date range.
         * Returns all task statuses with zeros for missing statuses.
         *
         * @param start start of date range
         * @param end   end of date range
         * @return list of task counts by status
         */
        public List<TaskSummaryResponse> getTaskSummaryByStatusBetween(
                        LocalDateTime start, LocalDateTime end) {
                log.debug("Getting task summary by status between {} and {}", start, end);

                List<TaskSummaryProjection> projections = taskJpaRepository
                                .countTasksByStatusBetween(start, end);
                return mapToTaskSummaryWithAllStatuses(projections);
        }

        /**
         * Get task statistics grouped by user for a specific date range.
         *
         * @param start start of date range
         * @param end   end of date range
         * @return list of task counts by user
         */
        public List<TaskForUserResponse> getTaskSummaryByUserBetween(
                        LocalDateTime start, LocalDateTime end) {
                log.debug("Getting task summary by user between {} and {}", start, end);

                return taskJpaRepository.countTasksByUserBetween(start, end)
                                .stream()
                                .map(this::mapToTaskForUserResponse)
                                .toList();
        }

}
