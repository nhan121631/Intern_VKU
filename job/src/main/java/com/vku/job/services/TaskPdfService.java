package com.vku.job.services;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import java.io.InputStream;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.vku.job.dtos.statistics.TaskForUserResponse;
import com.vku.job.dtos.statistics.TaskStatutesUserResponse;
import com.vku.job.dtos.statistics.TaskSummaryResponse;
import com.vku.job.dtos.statistics.WeeklyReportData;

@Service
public class TaskPdfService {

    // report task to pdf weekly
    public byte[] exportWeeklyTaskReportPdf(WeeklyReportData data) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Load a Unicode font from resources to support Vietnamese characters.
            PdfFont font;
            try (InputStream fontStream = TaskPdfService.class.getResourceAsStream("/fonts/NotoSans-Regular.ttf")) {
                if (fontStream != null) {
                    byte[] fontBytes = fontStream.readAllBytes();
                    font = PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H,
                            PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                } else {
                    // Fallback to a standard font if custom font not found (may not support
                    // vietnamese)
                    font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
                }
            }

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            document.setFont(font);

            // ===== TITLE =====
            document.add(new Paragraph("WEEKLY TASK REPORT")
                    .setBold()
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph(
                    "From " + data.getWeekStart() + " to " + data.getWeekEnd())
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            // ===== SUMMARY BY STATUS =====
            document.add(new Paragraph("Task Summary by Status")
                    .setBold()
                    .setFontSize(14));

            Table statusTable = new Table(2).useAllAvailableWidth();
            statusTable.addHeaderCell("Status");
            statusTable.addHeaderCell("Total");

            for (TaskSummaryResponse s : data.getSummaryByStatus()) {
                statusTable.addCell(s.getStatus());
                statusTable.addCell(String.valueOf(s.getValue()));
            }

            document.add(statusTable.setMarginBottom(20));

            // ===== SUMMARY BY USER =====
            document.add(new Paragraph("Task Summary by User")
                    .setBold()
                    .setFontSize(14));

            Table userTable = new Table(2).useAllAvailableWidth();
            userTable.addHeaderCell("User");
            userTable.addHeaderCell("Total Tasks");

            for (TaskForUserResponse u : data.getSummaryByUser()) {
                userTable.addCell(u.getUser());
                userTable.addCell(String.valueOf(u.getTotal()));
            }

            document.add(userTable.setMarginBottom(20));

            // ======= User STATUSES =======
            List<TaskStatutesUserResponse> raw = data.getSummaryByUserId();

            // List<String> ORDER = List.of("OPEN", "IN_PROGRESS", "DONE", "CANCELED");

            // group theo fullName
            Map<String, List<TaskStatutesUserResponse>> grouped = raw.stream()
                    .collect(Collectors.groupingBy(TaskStatutesUserResponse::getFullName));

            // ===== Title =====
            document.add(new Paragraph("Task Statistics By User")
                    .setBold()
                    .setFontSize(16)
                    .setMarginBottom(10));

            // ===== Table =====
            Table table = new Table(5).useAllAvailableWidth();

            // Header (FIXED TEXT – không lấy từ DB)
            table.addHeaderCell("User");
            table.addHeaderCell("Open");
            table.addHeaderCell("In Progress");
            table.addHeaderCell("Done");
            table.addHeaderCell("Canceled");

            // ===== Rows =====
            for (var entry : grouped.entrySet()) {

                String fullName = entry.getKey();
                List<TaskStatutesUserResponse> statuses = entry.getValue();

                // map status -> value
                Map<String, Long> statusMap = statuses.stream()
                        .collect(Collectors.toMap(
                                TaskStatutesUserResponse::getStatus,
                                TaskStatutesUserResponse::getValue,
                                Long::sum));

                table.addCell(fullName);
                table.addCell(String.valueOf(statusMap.getOrDefault("OPEN", 0L)));
                table.addCell(String.valueOf(statusMap.getOrDefault("IN_PROGRESS", 0L)));
                table.addCell(String.valueOf(statusMap.getOrDefault("DONE", 0L)));
                table.addCell(String.valueOf(statusMap.getOrDefault("CANCELED", 0L)));
            }

            document.add(table);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to export PDF", e);
        }
    }

}
