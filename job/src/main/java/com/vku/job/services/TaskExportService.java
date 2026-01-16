package com.vku.job.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.vku.job.dtos.task.TaskResponseDto;

@Service
public class TaskExportService {
    // Export list of tasks to Excel file
    public byte[] exportTasksToExcel(List<TaskResponseDto> tasks) {
        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Tasks");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // ================= HEADER =================
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "ID", "Title", "Status", "Deadline",
                    "Assigned User", "Created At"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            int rowIdx = 1;
            for (TaskResponseDto task : tasks) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(task.getId());
                row.createCell(1).setCellValue(task.getTitle());
                row.createCell(2).setCellValue(task.getStatus());
                row.createCell(3).setCellValue(
                        task.getDeadline() != null ? task.getDeadline().format(dateFormatter) : "");
                row.createCell(4).setCellValue(
                        task.getAssignedFullName() != null ? task.getAssignedFullName()
                                : "");
                row.createCell(5)
                        .setCellValue(task.getCreatedAt() != null ? task.getCreatedAt().format(dateTimeFormatter) : "");
            }

            // Auto size
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            workbook.close();
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to export Excel file", e);
        }
    }
}
