package com.vku.job.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vku.job.dtos.statistics.TaskForUserResponse;
import com.vku.job.dtos.statistics.TaskSummaryResponse;
import com.vku.job.dtos.statistics.WeeklyReportData;
import com.vku.job.enums.TaskStatus;
import com.vku.job.repositories.TaskJpaRepository;
import com.vku.job.repositories.UserJpaRepository;
import com.vku.job.repositories.projection.TaskForUserProjection;
import com.vku.job.repositories.projection.TaskStatusUserProjection;
import com.vku.job.repositories.projection.TaskSummaryProjection;
import com.vku.job.services.MailService;
import com.vku.job.services.StatisticsService;
import com.vku.job.services.TaskPdfService;

@ExtendWith(MockitoExtension.class)
public class TaskStatisticsServiceTest {

    @Spy
    @InjectMocks
    private StatisticsService taskStatisticsService;

    @Mock
    private TaskJpaRepository taskJpaRepository;

    @Mock
    private TaskPdfService taskPdfService;

    @Mock
    private MailService mailService;

    @Mock
    private UserJpaRepository userJpaRepository;

    // ====== TASK SUMMARY DTO MAPPING TESTS ======
    @Test
    void toTaskSummaryDto_success() {
        // ===== Arrange =====
        TaskSummaryProjection projection = Mockito.mock(TaskSummaryProjection.class);
        Mockito.when(projection.getStatus()).thenReturn("OPEN");
        Mockito.when(projection.getCount()).thenReturn(10L);

        // ===== Act =====
        TaskSummaryResponse response = taskStatisticsService.toTaskSummaryDto(projection);

        // ===== Assert =====
        assertNotNull(response);
        assertEquals("OPEN", response.getStatus());
        assertEquals(10L, response.getValue());
    }
    // ====== END OF TESTS ======

    // ====== TASK FOR USER DTO MAPPING TESTS ======
    @Test
    void toTaskForUserDto_success() {
        // Arrange
        TaskForUserProjection projection = Mockito.mock(TaskForUserProjection.class);
        Mockito.when(projection.getFullName()).thenReturn("Nguyen Van A");
        Mockito.when(projection.getCount()).thenReturn(7L);

        // Act
        TaskForUserResponse dto = taskStatisticsService.toTaskForUserDto(projection);

        // Assert
        assertNotNull(dto);
        assertEquals("Nguyen Van A", dto.getUser());
        assertEquals(7L, dto.getTotal());
    }
    // ====== END OF TESTS ======

    // ===== DATE PARSING TESTS ======

    // Valid date string
    @Test
    void parseDate_validDate_success() {
        LocalDate result = taskStatisticsService.parseDate("2026-01-22", "deadline");
        assertEquals(LocalDate.of(2026, 1, 22), result);
    }

    // Null date string
    @Test
    void parseDate_null_returnNull() {
        LocalDate result = taskStatisticsService.parseDate(null, "deadline");
        assertNull(result);
    }

    // Blank date string
    @Test
    void parseDate_blank_returnNull() {
        LocalDate result = taskStatisticsService.parseDate("   ", "deadline");
        assertNull(result);
    }

    // Invalid date format
    @Test
    void parseDate_invalidFormat_throwException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> taskStatisticsService.parseDate("22-01-2026", "deadline"));
        assertEquals("deadline must be in format yyyy-MM-dd", ex.getMessage());
    }

    // ===== END OF TESTS ======

    // ====== GET TASKS SUMMARY BY STATUS TESTS ======

    // get task summary by status with valid date range - success
    @Test
    void getTaskSummaryByStatus_withValidDateRange_success() {
        TaskSummaryProjection p1 = mock(TaskSummaryProjection.class);
        when(p1.getStatus()).thenReturn("OPEN");
        when(p1.getCount()).thenReturn(5L);

        when(taskJpaRepository.countTasksByStatusBetween(any(), any()))
                .thenReturn(List.of(p1));

        List<TaskSummaryResponse> result = taskStatisticsService.getTaskSummaryByStatus("2026-01-01", "2026-01-31");

        assertEquals(1, result.size());
        assertEquals("OPEN", result.get(0).getStatus());
        assertEquals(5L, result.get(0).getValue());
    }

    // get task summary by status with createdAtFrom after createdAtTo - throw
    // exception
    @Test
    void getTaskSummaryByStatus_fromAfterTo_throwException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> taskStatisticsService.getTaskSummaryByStatus("2026-02-01", "2026-01-01"));
        assertEquals("createdAtFrom cannot be after createdAtTo", ex.getMessage());
    }

    // get task summary by status with invalid date format - throw exception
    @Test
    void getTaskSummaryByStatus_invalidDate_throwException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> taskStatisticsService.getTaskSummaryByStatus("01-01-2026", "2026-01-31"));
        assertEquals("createdAtFrom must be in format yyyy-MM-dd", ex.getMessage());
    }

    // get task summary by status without date range - success
    @Test
    void getTaskSummaryByStatus_withoutDateRange_success() {
        TaskSummaryProjection p1 = mock(TaskSummaryProjection.class);
        when(p1.getStatus()).thenReturn("DONE");
        when(p1.getCount()).thenReturn(10L);

        when(taskJpaRepository.countTasksByStatus())
                .thenReturn(List.of(p1));

        List<TaskSummaryResponse> result = taskStatisticsService.getTaskSummaryByStatus(null, null);

        assertEquals(1, result.size());
        assertEquals("DONE", result.get(0).getStatus());
        assertEquals(10L, result.get(0).getValue());
    }

    // ====== END OF TESTS ======

    // ====== GET TASKS SUMMARY BY USER TESTS ======

    // get task summary by user with valid date range - success
    @Test
    void getTaskSummaryByUser_withValidDateRange_success() {
        TaskForUserProjection p1 = mock(TaskForUserProjection.class);
        when(p1.getFullName()).thenReturn("Nguyen Van A");
        when(p1.getCount()).thenReturn(3L);

        when(taskJpaRepository.countTasksByUserBetween(any(), any()))
                .thenReturn(List.of(p1));

        List<TaskForUserResponse> result = taskStatisticsService.getTaskSummaryByUser("2026-01-01", "2026-01-31");

        assertEquals(1, result.size());
        assertEquals("Nguyen Van A", result.get(0).getUser());
        assertEquals(3L, result.get(0).getTotal());
    }

    // get task summary by user with createdAtFrom after createdAtTo - throw
    // exception
    @Test
    void getTaskSummaryByUser_fromAfterTo_throwException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> taskStatisticsService.getTaskSummaryByUser("2026-02-01", "2026-01-01"));
        assertEquals("createdAtFrom cannot be after createdAtTo", ex.getMessage());
    }

    // get task summary by user with invalid date format - throw exception
    @Test
    void getTaskSummaryByUser_invalidDate_throwException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> taskStatisticsService.getTaskSummaryByUser("01-01-2026", "2026-01-31"));
        assertEquals("createdAtFrom must be in format yyyy-MM-dd", ex.getMessage());
    }

    // get task summary by user without date range - success
    @Test
    void getTaskSummaryByUser_withoutDateRange_success() {
        TaskForUserProjection p1 = mock(TaskForUserProjection.class);
        when(p1.getFullName()).thenReturn("Tran Thi B");
        when(p1.getCount()).thenReturn(7L);

        when(taskJpaRepository.countTasksByUser())
                .thenReturn(List.of(p1));

        List<TaskForUserResponse> result = taskStatisticsService.getTaskSummaryByUser(null, null);

        assertEquals(1, result.size());
        assertEquals("Tran Thi B", result.get(0).getUser());
        assertEquals(7L, result.get(0).getTotal());
    }

    // ====== END OF TESTS ======

    // ====== GET TASK STATISTICS BY USER TESTS ======

    // get task statistics by user with valid date range - success
    @Test
    void getTaskStatisticsByUser_withDateRange_success() {
        TaskStatusUserProjection p1 = mock(TaskStatusUserProjection.class);
        when(p1.getStatus()).thenReturn("OPEN");
        when(p1.getValue()).thenReturn(2L);

        when(taskJpaRepository.countTasksByStatusByUserBetween(anyLong(), any(), any()))
                .thenReturn(List.of(p1));

        List<TaskSummaryResponse> result = taskStatisticsService.getTaskStatisticsByUser(1L, "2026-01-01",
                "2026-01-31");

        assertEquals(TaskStatus.values().length, result.size());

        TaskSummaryResponse open = result.stream()
                .filter(r -> r.getStatus().equals("OPEN"))
                .findFirst()
                .orElseThrow();

        assertEquals(2L, open.getValue());
    }

    // get task statistics by user with createdAtFrom after createdAtTo - throw
    // exception
    @Test
    void getTaskStatisticsByUser_fromAfterTo_throwException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> taskStatisticsService.getTaskStatisticsByUser(1L, "2026-02-01", "2026-01-01"));
        assertEquals("createdAtFrom cannot be after createdAtTo", ex.getMessage());
    }

    // get task statistics by user with invalid date format - throw exception
    @Test
    void getTaskStatisticsByUser_invalidDate_throwException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> taskStatisticsService.getTaskStatisticsByUser(1L, "01-01-2026", "2026-01-31"));
        assertEquals("createdAtFrom must be in format yyyy-MM-dd", ex.getMessage());
    }

    // get task statistics by user without date range - success
    @Test
    void getTaskStatisticsByUser_withoutDateRange_success() {
        TaskSummaryProjection p1 = mock(TaskSummaryProjection.class);
        when(p1.getStatus()).thenReturn("DONE");
        when(p1.getCount()).thenReturn(5L);

        when(taskJpaRepository.countTasksByStatusByUser(1L))
                .thenReturn(List.of(p1));

        List<TaskSummaryResponse> result = taskStatisticsService.getTaskStatisticsByUser(1L, null, null);

        assertEquals(TaskStatus.values().length, result.size());

        TaskSummaryResponse done = result.stream()
                .filter(r -> r.getStatus().equals("DONE"))
                .findFirst()
                .orElseThrow();

        assertEquals(5L, done.getValue());
    }

    // get task statistics by user with null userId - throw exception
    @Test
    void getTaskStatisticsByUser_userIdNull_throwException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> taskStatisticsService.getTaskStatisticsByUser(null, null, null));
        assertEquals("userId is required", ex.getMessage());
    }

    // ====== END OF TESTS ======

    // ====== EXPORT WEEKLY TASK REPORT PDF TESTS ======

    // export weekly task report pdf - success
    @Test
    void exportWeeklyTaskReportPdf_success() {
        // given
        List<TaskSummaryResponse> summaryByStatus = List.of(
                new TaskSummaryResponse("OPEN", 3L));

        List<TaskForUserResponse> summaryByUser = List.of(
                new TaskForUserResponse("User A", 5L));

        TaskStatusUserProjection projection = mock(TaskStatusUserProjection.class);
        when(projection.getFullName()).thenReturn("User A");
        when(projection.getStatus()).thenReturn("OPEN");
        when(projection.getValue()).thenReturn(3L);

        when(taskJpaRepository.countTasksByStatusByAllUserBetween(any(), any()))
                .thenReturn(List.of(projection));

        doReturn(summaryByStatus)
                .when(taskStatisticsService)
                .getTaskSummaryByStatusBetween(any(), any());

        doReturn(summaryByUser)
                .when(taskStatisticsService)
                .getTaskSummaryByUserBetween(any(), any());

        byte[] pdfBytes = new byte[] { 1, 2, 3 };
        when(taskPdfService.exportWeeklyTaskReportPdf(any()))
                .thenReturn(pdfBytes);

        when(userJpaRepository.findAdminEmails())
                .thenReturn(List.of("admin1@test.com", "admin2@test.com"));

        // when
        taskStatisticsService.exportWeeklyTaskReportPdf();

        // then
        verify(taskPdfService, times(1))
                .exportWeeklyTaskReportPdf(any(WeeklyReportData.class));

        verify(userJpaRepository, times(1))
                .findAdminEmails();

        verify(mailService, times(2))
                .sendPdfReport(
                        anyString(),
                        eq(pdfBytes),
                        contains("Weekly Task Report"),
                        anyString());
    }
    // ====== END OF TESTS ======

    // ====== GET TASK SUMMARY BY STATUS BETWEEN TESTS ======

    // get task summary by status between - success
    @Test
    void getTaskSummaryByStatusBetween_success() {
        // given
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 1, 7, 23, 59);

        TaskSummaryProjection open = mock(TaskSummaryProjection.class);
        when(open.getStatus()).thenReturn("OPEN");
        when(open.getCount()).thenReturn(3L);

        TaskSummaryProjection done = mock(TaskSummaryProjection.class);
        when(done.getStatus()).thenReturn("DONE");
        when(done.getCount()).thenReturn(5L);

        when(taskJpaRepository.countTasksByStatusBetween(start, end))
                .thenReturn(List.of(open, done));

        // when
        List<TaskSummaryResponse> result = taskStatisticsService.getTaskSummaryByStatusBetween(start, end);

        // then
        assertEquals(TaskStatus.values().length, result.size());

        assertEquals(3L,
                result.stream()
                        .filter(r -> r.getStatus().equals("OPEN"))
                        .findFirst()
                        .orElseThrow()
                        .getValue());

        assertEquals(5L,
                result.stream()
                        .filter(r -> r.getStatus().equals("DONE"))
                        .findFirst()
                        .orElseThrow()
                        .getValue());

        assertEquals(0L,
                result.stream()
                        .filter(r -> r.getStatus().equals("IN_PROGRESS"))
                        .findFirst()
                        .orElseThrow()
                        .getValue());

        verify(taskJpaRepository).countTasksByStatusBetween(start, end);
    }

    // ====== END OF TESTS ======

    // ====== GET TASK SUMMARY BY USER BETWEEN TESTS ======

    // get task summary by user between - success
    @Test
    void getTaskSummaryByUserBetween_success() {
        // given
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 7, 23, 59);

        TaskForUserProjection p1 = mock(TaskForUserProjection.class);
        when(p1.getFullName()).thenReturn("User A");
        when(p1.getCount()).thenReturn(5L);

        TaskForUserProjection p2 = mock(TaskForUserProjection.class);
        when(p2.getFullName()).thenReturn("User B");
        when(p2.getCount()).thenReturn(2L);

        when(taskJpaRepository.countTasksByUserBetween(start, end))
                .thenReturn(List.of(p1, p2));

        // when
        List<TaskForUserResponse> result = taskStatisticsService.getTaskSummaryByUserBetween(start, end);

        // then
        assertEquals(2, result.size());

        assertEquals("User A", result.get(0).getUser());
        assertEquals(5L, result.get(0).getTotal());

        assertEquals("User B", result.get(1).getUser());
        assertEquals(2L, result.get(1).getTotal());

        verify(taskJpaRepository).countTasksByUserBetween(start, end);
    }
}
