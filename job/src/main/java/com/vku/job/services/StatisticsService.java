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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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

@Service
public class StatisticsService {

        @Autowired
        private TaskJpaRepository taskJpaRepository;
        @Autowired
        private TaskPdfService taskPdfService;
        @Autowired
        private MailService mailService;
        @Autowired
        private UserJpaRepository userJpaRepository;

        private TaskSummaryResponse toTaskSummaryDto(TaskSummaryProjection projection) {
                return new TaskSummaryResponse(projection.getStatus(), projection.getCount());
        }

        private TaskForUserResponse toTaskForUserDto(TaskForUserProjection projection) {
                TaskForUserResponse dto = new TaskForUserResponse();
                dto.setUser(projection.getFullName());
                dto.setTotal(projection.getCount());
                return dto;
        }

        private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        private LocalDate parseDate(String dateStr, String fieldName) {
                if (dateStr == null || dateStr.isBlank()) {
                        return null;
                }

                try {
                        return LocalDate.parse(dateStr, DATE_FORMAT);
                } catch (DateTimeParseException e) {
                        throw new IllegalArgumentException(
                                        fieldName + " must be in format yyyy-MM-dd");
                }
        }

        public List<TaskSummaryResponse> getTaskSummaryByStatus(String createdAtFrom, String createdAtTo) {
                if (createdAtFrom != null && createdAtTo != null && !createdAtFrom.isBlank()
                                && !createdAtTo.isBlank()) {
                        LocalDate fromDate = parseDate(createdAtFrom, "createdAtFrom");
                        LocalDate toDate = parseDate(createdAtTo, "createdAtTo");
                        if (fromDate == null || toDate == null) {
                                throw new IllegalArgumentException(
                                                "createdAtFrom and createdAtTo must be valid dates in format yyyy-MM-dd");
                        }
                        if (fromDate.isAfter(toDate)) {
                                throw new IllegalArgumentException(
                                                "createdAtFrom cannot be after createdAtTo");
                        }
                        LocalDateTime from = fromDate.atStartOfDay();
                        LocalDateTime to = toDate.plusDays(1).atStartOfDay().minusNanos(1);
                        List<TaskSummaryProjection> projections = taskJpaRepository
                                        .countTasksByStatusBetween(from, to);
                        return projections.stream()
                                        .map(this::toTaskSummaryDto)
                                        .toList();
                }

                List<TaskSummaryProjection> projections = taskJpaRepository.countTasksByStatus();
                return projections.stream()
                                .map(this::toTaskSummaryDto)
                                .toList();

        }

        public List<TaskForUserResponse> getTaskSummaryByUser(String createdAtFrom, String createdAtTo) {
                if (createdAtFrom != null && createdAtTo != null && !createdAtFrom.isBlank()
                                && !createdAtTo.isBlank()) {
                        LocalDate fromDate = parseDate(createdAtFrom, "createdAtFrom");
                        LocalDate toDate = parseDate(createdAtTo, "createdAtTo");
                        if (fromDate == null || toDate == null) {
                                throw new IllegalArgumentException(
                                                "createdAtFrom and createdAtTo must be valid dates in format yyyy-MM-dd");
                        }
                        if (fromDate.isAfter(toDate)) {
                                throw new IllegalArgumentException(
                                                "createdAtFrom cannot be after createdAtTo");
                        }
                        LocalDateTime from = fromDate.atStartOfDay();
                        LocalDateTime to = toDate.plusDays(1).atStartOfDay().minusNanos(1);
                        List<TaskForUserProjection> projections = taskJpaRepository
                                        .countTasksByUserBetween(from, to);
                        return projections.stream()
                                        .map(this::toTaskForUserDto)
                                        .toList();
                }

                List<TaskForUserProjection> projections = taskJpaRepository.countTasksByUser();
                return projections.stream()
                                .map(this::toTaskForUserDto)
                                .toList();
        }

        public List<TaskSummaryResponse> getTaskStatisticsByUser(Long userId, String createdAtFrom,
                        String createdAtTo) {
                if (createdAtFrom != null && createdAtTo != null && !createdAtFrom.isBlank()
                                && !createdAtTo.isBlank()) {
                        LocalDate fromDate = parseDate(createdAtFrom, "createdAtFrom");
                        LocalDate toDate = parseDate(createdAtTo, "createdAtTo");
                        if (fromDate == null || toDate == null) {
                                throw new IllegalArgumentException(
                                                "createdAtFrom and createdAtTo must be valid dates in format yyyy-MM-dd");
                        }
                        if (fromDate.isAfter(toDate)) {
                                throw new IllegalArgumentException(
                                                "createdAtFrom cannot be after createdAtTo");
                        }
                        LocalDateTime from = fromDate.atStartOfDay();
                        LocalDateTime to = toDate.plusDays(1).atStartOfDay().minusNanos(1);
                        List<TaskStatusUserProjection> raw = taskJpaRepository
                                        .countTasksByStatusByUserBetween(userId, from, to);

                        Map<TaskStatus, Long> countMap = raw.stream()
                                        .collect(Collectors.toMap(
                                                        p -> TaskStatus.valueOf(p.getStatus()),
                                                        TaskStatusUserProjection::getValue));

                        return Arrays.stream(TaskStatus.values())
                                        .map(status -> new TaskSummaryResponse(
                                                        status.name(),
                                                        countMap.getOrDefault(status, 0L)))
                                        .toList();
                }

                if (userId == null) {
                        throw new IllegalArgumentException("userId is required");
                }

                List<TaskSummaryProjection> raw = taskJpaRepository.countTasksByStatusByUser(userId);

                Map<TaskStatus, Long> countMap = raw.stream()
                                .collect(Collectors.toMap(
                                                p -> TaskStatus.valueOf(p.getStatus()),
                                                TaskSummaryProjection::getCount));

                return Arrays.stream(TaskStatus.values())
                                .map(status -> new TaskSummaryResponse(
                                                status.name(),
                                                countMap.getOrDefault(status, 0L)))
                                .toList();
        }

        // export task statistics to pdf for weekly
        @Scheduled(cron = "0 0 8 ? * MON") // every Monday at 8 AM
        public void exportWeeklyTaskReportPdf() {
                LocalDate today = LocalDate.now();

                LocalDate weekEnd = today.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY));
                LocalDate weekStart = weekEnd.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));

                LocalDateTime startDateTime = weekStart.atStartOfDay();
                LocalDateTime endDateTime = weekEnd.plusDays(1).atStartOfDay().minusNanos(1);

                List<TaskSummaryResponse> summaryByStatus = getTaskSummaryByStatusBetween(startDateTime, endDateTime);

                List<TaskForUserResponse> summaryByUser = getTaskSummaryByUserBetween(startDateTime, endDateTime);

                List<TaskStatutesUserResponse> rawStatusByUser = taskJpaRepository
                                .countTasksByStatusByAllUserBetween(startDateTime, endDateTime)
                                .stream()
                                .map(p -> {
                                        TaskStatutesUserResponse dto = new TaskStatutesUserResponse();
                                        dto.setFullName(p.getFullName());
                                        dto.setStatus(p.getStatus());
                                        dto.setValue(p.getValue());
                                        return dto;
                                })
                                .toList();

                WeeklyReportData reportData = new WeeklyReportData();
                reportData.setWeekStart(weekStart);
                reportData.setWeekEnd(weekEnd);
                reportData.setSummaryByStatus(summaryByStatus);
                reportData.setSummaryByUser(summaryByUser);
                reportData.setSummaryByUserId(rawStatusByUser);
                byte[] pdfBytes = taskPdfService.exportWeeklyTaskReportPdf(reportData);

                List<String> adminEmails = userJpaRepository.findAdminEmails();

                for (String email : adminEmails) {
                        mailService.sendPdfReport(
                                        email,
                                        pdfBytes,
                                        "Weekly Task Report (" + weekStart + " → " + weekEnd + ")",
                                        "Please find attached the weekly task report.");
                }

        }

        private List<TaskSummaryResponse> getTaskSummaryByStatusBetween(
                        LocalDateTime start, LocalDateTime end) {

                List<TaskSummaryProjection> raw = taskJpaRepository.countTasksByStatusBetween(start, end);

                Map<TaskStatus, Long> map = raw.stream()
                                .collect(Collectors.toMap(
                                                p -> TaskStatus.valueOf(p.getStatus()),
                                                TaskSummaryProjection::getCount));
                return Arrays.stream(TaskStatus.values())
                                .map(status -> new TaskSummaryResponse(
                                                status.name(),
                                                map.getOrDefault(status, 0L)))
                                .toList();
        }

        private List<TaskForUserResponse> getTaskSummaryByUserBetween(
                        LocalDateTime start, LocalDateTime end) {

                return taskJpaRepository.countTasksByUserBetween(start, end)
                                .stream()
                                .map(p -> new TaskForUserResponse(
                                                p.getFullName(),
                                                p.getCount()))
                                .toList();
        }

}
