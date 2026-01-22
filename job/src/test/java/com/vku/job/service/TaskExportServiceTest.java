package com.vku.job.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vku.job.dtos.task.TaskResponseDto;
import com.vku.job.services.TaskExportService;

@ExtendWith(MockitoExtension.class)
public class TaskExportServiceTest {
    @InjectMocks
    private TaskExportService taskExportService;

    // ====== TASK EXPORT TESTS ======

    // Task export to Excel - success case
    @Test
    void exportTasksToExcel_success() throws Exception {
        TaskResponseDto task = TaskResponseDto.builder()
                .id(1L)
                .title("Test Task")
                .status("OPEN")
                .deadline(LocalDate.of(2025, 1, 1))
                .assignedFullName("Nguyen Van A")
                .createdAt(LocalDateTime.of(2025, 1, 1, 10, 0))
                .build();

        List<TaskResponseDto> tasks = List.of(task);

        byte[] excelBytes = taskExportService.exportTasksToExcel(tasks);

        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 0);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelBytes))) {
            Sheet sheet = workbook.getSheet("Tasks");
            assertNotNull(sheet);

            // HEADER
            Row header = sheet.getRow(0);
            assertEquals("ID", header.getCell(0).getStringCellValue());
            assertEquals("Title", header.getCell(1).getStringCellValue());

            // DATA ROW
            Row dataRow = sheet.getRow(1);
            assertEquals(1L, (long) dataRow.getCell(0).getNumericCellValue());
            assertEquals("Test Task", dataRow.getCell(1).getStringCellValue());
            assertEquals("OPEN", dataRow.getCell(2).getStringCellValue());
            assertEquals("2025-01-01", dataRow.getCell(3).getStringCellValue());
            assertEquals("Nguyen Van A", dataRow.getCell(4).getStringCellValue());
            assertEquals("2025-01-01 10:00:00", dataRow.getCell(5).getStringCellValue());
        }
    }

    // Task export to Excel - empty list
    @Test
    void exportTasksToExcel_nullFields() throws Exception {
        TaskResponseDto task = TaskResponseDto.builder()
                .id(2L)
                .title("No Assign")
                .status("DONE")
                .deadline(null)
                .assignedFullName(null)
                .createdAt(null)
                .build();

        byte[] bytes = taskExportService.exportTasksToExcel(List.of(task));

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Row row = workbook.getSheet("Tasks").getRow(1);

            assertEquals("", row.getCell(3).getStringCellValue()); // deadline
            assertEquals("", row.getCell(4).getStringCellValue()); // assigned user
            assertEquals("", row.getCell(5).getStringCellValue()); // created at
        }
    }

}
