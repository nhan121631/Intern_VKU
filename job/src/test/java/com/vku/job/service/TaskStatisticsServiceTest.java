package com.vku.job.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vku.job.dtos.statistics.TaskForUserResponse;
import com.vku.job.dtos.statistics.TaskSummaryResponse;
import com.vku.job.enums.TaskStatus;
import com.vku.job.repositories.TaskJpaRepository;
import com.vku.job.repositories.UserJpaRepository;
import com.vku.job.repositories.projection.TaskForUserProjection;
import com.vku.job.repositories.projection.TaskStatusUserProjection;
import com.vku.job.repositories.projection.TaskSummaryProjection;
import com.vku.job.services.MailService;
import com.vku.job.services.StatisticsService;
import com.vku.job.services.TaskPdfService;

/**
 * Unit tests for StatisticsService.
 * Tests business logic in isolation using mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StatisticsService Unit Tests")
class TaskStatisticsServiceTest {

        @InjectMocks
        private StatisticsService statisticsService;

        @Mock
        private TaskJpaRepository taskJpaRepository;

        @Mock
        private TaskPdfService taskPdfService;

        @Mock
        private MailService mailService;

        @Mock
        private UserJpaRepository userJpaRepository;

        // ====== GET TASKS SUMMARY BY STATUS TESTS ======

        @Nested
        @DisplayName("getTaskSummaryByStatus Tests")
        class GetTaskSummaryByStatusTests {

                @Test
                @DisplayName("Should return task summary when valid date range provided")
                void shouldReturnTaskSummaryWithValidDateRange() {
                        // Given
                        TaskSummaryProjection projection = mock(TaskSummaryProjection.class);
                        when(projection.getStatus()).thenReturn("OPEN");
                        when(projection.getCount()).thenReturn(5L);

                        when(taskJpaRepository.countTasksByStatusBetween(any(), any()))
                                        .thenReturn(List.of(projection));

                        // When
                        List<TaskSummaryResponse> result = statisticsService.getTaskSummaryByStatus(
                                        "2026-01-01", "2026-01-31");

                        // Then
                        assertThat(result).hasSize(1);
                        assertThat(result.get(0).getStatus()).isEqualTo("OPEN");
                        assertThat(result.get(0).getValue()).isEqualTo(5L);
                        verify(taskJpaRepository).countTasksByStatusBetween(any(), any());
                }

                @Test
                @DisplayName("Should throw exception when fromDate is after toDate")
                void shouldThrowExceptionWhenFromDateAfterToDate() {
                        // When/Then
                        assertThatThrownBy(() -> statisticsService.getTaskSummaryByStatus("2026-02-01", "2026-01-01"))
                                        .isInstanceOf(IllegalArgumentException.class)
                                        .hasMessageContaining("createdAtFrom cannot be after createdAtTo");
                }

                @Test
                @DisplayName("Should throw exception when date format is invalid")
                void shouldThrowExceptionWhenDateFormatInvalid() {
                        // When/Then
                        assertThatThrownBy(() -> statisticsService.getTaskSummaryByStatus("01-01-2026", "2026-01-31"))
                                        .isInstanceOf(IllegalArgumentException.class)
                                        .hasMessageContaining("must be in format yyyy-MM-dd");
                }

                @Test
                @DisplayName("Should return all task summary when no date range provided")
                void shouldReturnAllTaskSummaryWithoutDateRange() {
                        // Given
                        TaskSummaryProjection projection = mock(TaskSummaryProjection.class);
                        when(projection.getStatus()).thenReturn("DONE");
                        when(projection.getCount()).thenReturn(10L);

                        when(taskJpaRepository.countTasksByStatus())
                                        .thenReturn(List.of(projection));

                        // When
                        List<TaskSummaryResponse> result = statisticsService.getTaskSummaryByStatus(null, null);

                        // Then
                        assertThat(result)
                                        .hasSize(1)
                                        .first()
                                        .satisfies(response -> {
                                                assertThat(response.getStatus()).isEqualTo("DONE");
                                                assertThat(response.getValue()).isEqualTo(10L);
                                        });
                        verify(taskJpaRepository).countTasksByStatus();
                }
        }

        @Nested
        @DisplayName("getTaskSummaryByUser Tests")
        class GetTaskSummaryByUserTests {

                @Test
                @DisplayName("Should return task summary by user when valid date range provided")
                void shouldReturnTaskSummaryByUserWithValidDateRange() {
                        // Given
                        TaskForUserProjection projection = mock(TaskForUserProjection.class);
                        when(projection.getFullName()).thenReturn("Nguyen Van A");
                        when(projection.getCount()).thenReturn(3L);

                        when(taskJpaRepository.countTasksByUserBetween(any(), any()))
                                        .thenReturn(List.of(projection));

                        // When
                        List<TaskForUserResponse> result = statisticsService.getTaskSummaryByUser(
                                        "2026-01-01", "2026-01-31");

                        // Then
                        assertThat(result)
                                        .hasSize(1)
                                        .first()
                                        .satisfies(response -> {
                                                assertThat(response.getUser()).isEqualTo("Nguyen Van A");
                                                assertThat(response.getTotal()).isEqualTo(3L);
                                        });
                        verify(taskJpaRepository).countTasksByUserBetween(any(), any());
                }

                @Test
                @DisplayName("Should throw exception when fromDate is after toDate")
                void shouldThrowExceptionWhenFromDateAfterToDate() {
                        // When/Then
                        assertThatThrownBy(() -> statisticsService.getTaskSummaryByUser("2026-02-01", "2026-01-01"))
                                        .isInstanceOf(IllegalArgumentException.class)
                                        .hasMessageContaining("createdAtFrom cannot be after createdAtTo");
                }

                @Test
                @DisplayName("Should throw exception when date format is invalid")
                void shouldThrowExceptionWhenDateFormatInvalid() {
                        // When/Then
                        assertThatThrownBy(() -> statisticsService.getTaskSummaryByUser("01-01-2026", "2026-01-31"))
                                        .isInstanceOf(IllegalArgumentException.class)
                                        .hasMessageContaining("must be in format yyyy-MM-dd");
                }

                @Test
                @DisplayName("Should return all task summary when no date range provided")
                void shouldReturnAllTaskSummaryWithoutDateRange() {
                        // Given
                        TaskForUserProjection projection = mock(TaskForUserProjection.class);
                        when(projection.getFullName()).thenReturn("Tran Thi B");
                        when(projection.getCount()).thenReturn(7L);

                        when(taskJpaRepository.countTasksByUser())
                                        .thenReturn(List.of(projection));

                        // When
                        List<TaskForUserResponse> result = statisticsService.getTaskSummaryByUser(null, null);

                        // Then
                        assertThat(result)
                                        .hasSize(1)
                                        .first()
                                        .satisfies(response -> {
                                                assertThat(response.getUser()).isEqualTo("Tran Thi B");
                                                assertThat(response.getTotal()).isEqualTo(7L);
                                        });
                        verify(taskJpaRepository).countTasksByUser();
                }
        }

        @Nested
        @DisplayName("getTaskStatisticsByUser Tests")
        class GetTaskStatisticsByUserTests {

                @Test
                @DisplayName("Should return task statistics for user when valid date range provided")
                void shouldReturnTaskStatisticsWithValidDateRange() {
                        // Given
                        TaskStatusUserProjection projection = mock(TaskStatusUserProjection.class);
                        when(projection.getStatus()).thenReturn("OPEN");
                        when(projection.getValue()).thenReturn(2L);

                        when(taskJpaRepository.countTasksByStatusByUserBetween(eq(1L), any(), any()))
                                        .thenReturn(List.of(projection));

                        // When
                        List<TaskSummaryResponse> result = statisticsService.getTaskStatisticsByUser(
                                        1L, "2026-01-01", "2026-01-31");

                        // Then
                        assertThat(result).hasSize(TaskStatus.values().length);

                        TaskSummaryResponse openStatus = result.stream()
                                        .filter(r -> r.getStatus().equals("OPEN"))
                                        .findFirst()
                                        .orElseThrow();

                        assertThat(openStatus.getValue()).isEqualTo(2L);
                        verify(taskJpaRepository).countTasksByStatusByUserBetween(eq(1L), any(), any());
                }

                @Test
                @DisplayName("Should throw exception when fromDate is after toDate")
                void shouldThrowExceptionWhenFromDateAfterToDate() {
                        // When/Then
                        assertThatThrownBy(
                                        () -> statisticsService.getTaskStatisticsByUser(1L, "2026-02-01", "2026-01-01"))
                                        .isInstanceOf(IllegalArgumentException.class)
                                        .hasMessageContaining("createdAtFrom cannot be after createdAtTo");
                }

                @Test
                @DisplayName("Should throw exception when date format is invalid")
                void shouldThrowExceptionWhenDateFormatInvalid() {
                        // When/Then
                        assertThatThrownBy(
                                        () -> statisticsService.getTaskStatisticsByUser(1L, "01-01-2026", "2026-01-31"))
                                        .isInstanceOf(IllegalArgumentException.class)
                                        .hasMessageContaining("must be in format yyyy-MM-dd");
                }

                @Test
                @DisplayName("Should return task statistics when no date range provided")
                void shouldReturnTaskStatisticsWithoutDateRange() {
                        // Given
                        TaskSummaryProjection projection = mock(TaskSummaryProjection.class);
                        when(projection.getStatus()).thenReturn("DONE");
                        when(projection.getCount()).thenReturn(5L);

                        when(taskJpaRepository.countTasksByStatusByUser(1L))
                                        .thenReturn(List.of(projection));

                        // When
                        List<TaskSummaryResponse> result = statisticsService.getTaskStatisticsByUser(
                                        1L, null, null);

                        // Then
                        assertThat(result).hasSize(TaskStatus.values().length);

                        TaskSummaryResponse doneStatus = result.stream()
                                        .filter(r -> r.getStatus().equals("DONE"))
                                        .findFirst()
                                        .orElseThrow();

                        assertThat(doneStatus.getValue()).isEqualTo(5L);
                        verify(taskJpaRepository).countTasksByStatusByUser(1L);
                }

                @Test
                @DisplayName("Should throw exception when userId is null")
                void shouldThrowExceptionWhenUserIdIsNull() {
                        // When/Then
                        assertThatThrownBy(() -> statisticsService.getTaskStatisticsByUser(null, null, null))
                                        .isInstanceOf(IllegalArgumentException.class)
                                        .hasMessageContaining("userId is required");
                }
        }

        @Nested
        @DisplayName("exportWeeklyTaskReportPdf Tests")
        class ExportWeeklyTaskReportPdfTests {

                @Test
                @DisplayName("Should generate and send weekly report successfully")
                void shouldGenerateAndSendWeeklyReportSuccessfully() {
                        // Given
                        TaskSummaryProjection statusProjection = mock(TaskSummaryProjection.class);
                        when(statusProjection.getStatus()).thenReturn("OPEN");
                        when(statusProjection.getCount()).thenReturn(3L);

                        TaskForUserProjection userProjection = mock(TaskForUserProjection.class);
                        when(userProjection.getFullName()).thenReturn("User A");
                        when(userProjection.getCount()).thenReturn(5L);

                        TaskStatusUserProjection statusUserProjection = mock(TaskStatusUserProjection.class);
                        when(statusUserProjection.getFullName()).thenReturn("User A");
                        when(statusUserProjection.getStatus()).thenReturn("OPEN");
                        when(statusUserProjection.getValue()).thenReturn(3L);

                        when(taskJpaRepository.countTasksByStatusBetween(any(), any()))
                                        .thenReturn(List.of(statusProjection));
                        when(taskJpaRepository.countTasksByUserBetween(any(), any()))
                                        .thenReturn(List.of(userProjection));
                        when(taskJpaRepository.countTasksByStatusByAllUserBetween(any(), any()))
                                        .thenReturn(List.of(statusUserProjection));

                        byte[] pdfBytes = new byte[] { 1, 2, 3 };
                        when(taskPdfService.exportWeeklyTaskReportPdf(any()))
                                        .thenReturn(pdfBytes);

                        when(userJpaRepository.findAdminEmails())
                                        .thenReturn(List.of("admin1@test.com", "admin2@test.com"));

                        // When
                        statisticsService.exportWeeklyTaskReportPdf();

                        // Then
                        verify(taskJpaRepository).countTasksByStatusBetween(any(), any());
                        verify(taskJpaRepository).countTasksByUserBetween(any(), any());
                        verify(taskJpaRepository).countTasksByStatusByAllUserBetween(any(), any());
                        verify(taskPdfService).exportWeeklyTaskReportPdf(any());
                        verify(userJpaRepository).findAdminEmails();
                        verify(mailService, times(2)).sendPdfReport(
                                        anyString(),
                                        eq(pdfBytes),
                                        contains("Weekly Task Report"),
                                        anyString());
                }
        }

        @Nested
        @DisplayName("getTaskSummaryByStatusBetween Tests")
        class GetTaskSummaryByStatusBetweenTests {

                @Test
                @DisplayName("Should return task summary for date range with all statuses")
                void shouldReturnTaskSummaryForDateRangeWithAllStatuses() {
                        // Given
                        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
                        LocalDateTime end = LocalDateTime.of(2026, 1, 7, 23, 59);

                        TaskSummaryProjection openProjection = mock(TaskSummaryProjection.class);
                        when(openProjection.getStatus()).thenReturn("OPEN");
                        when(openProjection.getCount()).thenReturn(3L);

                        TaskSummaryProjection doneProjection = mock(TaskSummaryProjection.class);
                        when(doneProjection.getStatus()).thenReturn("DONE");
                        when(doneProjection.getCount()).thenReturn(5L);

                        when(taskJpaRepository.countTasksByStatusBetween(start, end))
                                        .thenReturn(List.of(openProjection, doneProjection));

                        // When
                        List<TaskSummaryResponse> result = statisticsService
                                        .getTaskSummaryByStatusBetween(start, end);

                        // Then
                        assertThat(result).hasSize(TaskStatus.values().length);

                        assertThat(result.stream()
                                        .filter(r -> r.getStatus().equals("OPEN"))
                                        .findFirst()
                                        .orElseThrow()
                                        .getValue())
                                        .isEqualTo(3L);

                        assertThat(result.stream()
                                        .filter(r -> r.getStatus().equals("DONE"))
                                        .findFirst()
                                        .orElseThrow()
                                        .getValue())
                                        .isEqualTo(5L);

                        assertThat(result.stream()
                                        .filter(r -> r.getStatus().equals("IN_PROGRESS"))
                                        .findFirst()
                                        .orElseThrow()
                                        .getValue())
                                        .isEqualTo(0L);

                        verify(taskJpaRepository).countTasksByStatusBetween(start, end);
                }
        }

        @Nested
        @DisplayName("getTaskSummaryByUserBetween Tests")
        class GetTaskSummaryByUserBetweenTests {

                @Test
                @DisplayName("Should return task summary by user for date range")
                void shouldReturnTaskSummaryByUserForDateRange() {
                        // Given
                        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
                        LocalDateTime end = LocalDateTime.of(2025, 1, 7, 23, 59);

                        TaskForUserProjection projection1 = mock(TaskForUserProjection.class);
                        when(projection1.getFullName()).thenReturn("User A");
                        when(projection1.getCount()).thenReturn(5L);

                        TaskForUserProjection projection2 = mock(TaskForUserProjection.class);
                        when(projection2.getFullName()).thenReturn("User B");
                        when(projection2.getCount()).thenReturn(2L);

                        when(taskJpaRepository.countTasksByUserBetween(start, end))
                                        .thenReturn(List.of(projection1, projection2));

                        // When
                        List<TaskForUserResponse> result = statisticsService
                                        .getTaskSummaryByUserBetween(start, end);

                        // Then
                        assertThat(result)
                                        .hasSize(2)
                                        .satisfies(responses -> {
                                                assertThat(responses.get(0).getUser()).isEqualTo("User A");
                                                assertThat(responses.get(0).getTotal()).isEqualTo(5L);
                                                assertThat(responses.get(1).getUser()).isEqualTo("User B");
                                                assertThat(responses.get(1).getTotal()).isEqualTo(2L);
                                        });

                        verify(taskJpaRepository).countTasksByUserBetween(start, end);
                }
        }
}
