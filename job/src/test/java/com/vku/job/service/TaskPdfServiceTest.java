package com.vku.job.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vku.job.dtos.statistics.TaskForUserResponse;
import com.vku.job.dtos.statistics.TaskStatutesUserResponse;
import com.vku.job.dtos.statistics.TaskSummaryResponse;
import com.vku.job.dtos.statistics.WeeklyReportData;
import com.vku.job.services.TaskPdfService;

@ExtendWith(MockitoExtension.class)
public class TaskPdfServiceTest {

    @InjectMocks
    private TaskPdfService taskPdfService;

    // ====== EXPORT WEEKLY TASK REPORT TO PDF ======

    // success
    @Test
    void exportWeeklyTaskReportPdf_success() {
        // ===== Arrange =====
        WeeklyReportData data = new WeeklyReportData();
        data.setWeekStart(LocalDate.parse("2026-01-01"));
        data.setWeekEnd(LocalDate.parse("2026-01-07"));

        data.setSummaryByStatus(List.of(
                new TaskSummaryResponse("OPEN", 5L),
                new TaskSummaryResponse("DONE", 3L)));

        data.setSummaryByUser(List.of(
                new TaskForUserResponse("Nguyen Van A", 4L),
                new TaskForUserResponse("Tran Van B", 4L)));

        data.setSummaryByUserId(List.of(
                new TaskStatutesUserResponse("Nguyen Van A", "OPEN", 2L),
                new TaskStatutesUserResponse("Nguyen Van A", "DONE", 2L),
                new TaskStatutesUserResponse("Tran Van B", "OPEN", 1L),
                new TaskStatutesUserResponse("Tran Van B", "IN_PROGRESS", 3L)));

        // ===== Act =====
        byte[] pdfBytes = taskPdfService.exportWeeklyTaskReportPdf(data);

        // ===== Assert =====
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        // PDF header check (%PDF)
        String header = new String(pdfBytes, 0, 4);
        assertEquals("%PDF", header);
    }

    // export with empty data
    @Test
    void exportWeeklyTaskReportPdf_emptyData_success() {
        WeeklyReportData data = new WeeklyReportData();
        data.setWeekStart(LocalDate.parse("2026-01-01"));
        data.setWeekEnd(LocalDate.parse("2026-01-07"));
        data.setSummaryByStatus(List.of());
        data.setSummaryByUser(List.of());
        data.setSummaryByUserId(List.of());

        byte[] pdfBytes = taskPdfService.exportWeeklyTaskReportPdf(data);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    // export with null data - throw exception
    @Test
    void exportWeeklyTaskReportPdf_nullData_throwException() {
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> taskPdfService.exportWeeklyTaskReportPdf(null));

        assertEquals("Failed to export PDF", ex.getMessage());
    }

}
