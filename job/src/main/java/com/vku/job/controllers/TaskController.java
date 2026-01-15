package com.vku.job.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vku.job.dtos.PaginatedResponseDto;
import com.vku.job.dtos.task.CreateTaskRequestDto;
import com.vku.job.dtos.task.TaskResponseDto;
import com.vku.job.dtos.task.UpdateTaskByUserRequestDto;
import com.vku.job.dtos.task.UpdateTaskRequestDto;
import com.vku.job.services.TaskExportService;
import com.vku.job.services.TaskService;
import com.vku.job.services.auth.JwtService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskExportService taskExportService;

    @Autowired
    private JwtService jwtService;

    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(@RequestBody @Valid CreateTaskRequestDto createTaskRequestDto) {

        TaskResponseDto taskResponseDto = taskService.addTask(createTaskRequestDto);
        return ResponseEntity.status(201).body(taskResponseDto);
    }

    @GetMapping
    public ResponseEntity<PaginatedResponseDto<TaskResponseDto>> getTasks(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "order", defaultValue = "asc") String order) {
        return ResponseEntity.ok(taskService.getAllTasks(page, size, sortBy, order));

    }

    @GetMapping("/get-by-id")
    public ResponseEntity<TaskResponseDto> getTaskById(@RequestParam("id") Long id) {
        TaskResponseDto task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteTask(@RequestParam("id") Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping
    public ResponseEntity<TaskResponseDto> updateTask(@RequestBody @Valid UpdateTaskRequestDto updateTaskRequestDto,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtService.extractUserIdFromToken(token);
        TaskResponseDto updatedTask = taskService.updateTask(updateTaskRequestDto, userId);
        return ResponseEntity.ok(updatedTask);
    }

    @GetMapping("/by-user")
    public ResponseEntity<PaginatedResponseDto<TaskResponseDto>> getTasksByUser(
            @RequestParam("userId") Long userId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "order", defaultValue = "asc") String order) {
        PaginatedResponseDto<TaskResponseDto> tasksByUser = taskService.getTasksByUserId(userId, page, size, sortBy,
                order);
        return ResponseEntity.ok(tasksByUser);
    }

    @PatchMapping("/by-user/update")
    public ResponseEntity<TaskResponseDto> updateTaskByUser(
            @RequestBody @Valid UpdateTaskByUserRequestDto updateTaskRequestDto,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtService.extractUserIdFromToken(token);
        TaskResponseDto updatedTask = taskService.updateTaskByUser(updateTaskRequestDto, userId);
        return ResponseEntity.ok(updatedTask);
    }

    @GetMapping("/by-user/search-title")
    public ResponseEntity<PaginatedResponseDto<TaskResponseDto>> getTasksByUserAndTitle(
            @RequestParam("userId") Long userId,
            @RequestParam("title") String title,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "order", defaultValue = "asc") String order) {
        PaginatedResponseDto<TaskResponseDto> tasksByUserAndTitle = taskService.getTasksByUserAndTitle(userId, title,
                page, size, sortBy, order);
        return ResponseEntity.ok(tasksByUserAndTitle);
    }

    @GetMapping("/by-user/filter-status")
    public ResponseEntity<PaginatedResponseDto<TaskResponseDto>> getTasksByUserAndStatus(
            @RequestParam("userId") Long userId,
            @RequestParam("status") String status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "order", defaultValue = "asc") String order) {
        PaginatedResponseDto<TaskResponseDto> tasksByUserAndStatus = taskService.getTasksByUserAndStatus(userId, status,
                page, size, sortBy, order);
        return ResponseEntity.ok(tasksByUserAndStatus);
    }

    @GetMapping("/search-by-title")
    public ResponseEntity<PaginatedResponseDto<TaskResponseDto>> searchTasksByTitle(
            @RequestParam("title") String title,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "order", defaultValue = "asc") String order) {
        PaginatedResponseDto<TaskResponseDto> tasks = taskService.searchTasksByTitle(title, page, size, sortBy, order);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/filter-by-status")
    public ResponseEntity<PaginatedResponseDto<TaskResponseDto>> filterTasks(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "order", defaultValue = "asc") String order) {

        System.out.println("Filtering tasks with status: " + status + " and userId: " + userId);
        PaginatedResponseDto<TaskResponseDto> tasks = taskService.filterTasks(userId, status, page, size,
                sortBy,
                order);
        return ResponseEntity.ok(tasks);
    }

    // export tasks to excel
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportTasks() {
        List<TaskResponseDto> tasks = taskService.getTasksForExport();

        byte[] excelStream = taskExportService.exportTasksToExcel(tasks);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tasks.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelStream);

    }

    @GetMapping("/export-by-user")
    public ResponseEntity<byte[]> exportTasksByUser(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtService.extractUserIdFromToken(token);
        List<TaskResponseDto> tasks = taskService.getTasksForExportByUserId(userId);

        byte[] excelStream = taskExportService.exportTasksToExcel(tasks);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tasks_by_user.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelStream);

    }
}