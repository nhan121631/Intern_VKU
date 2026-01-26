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
import com.vku.job.dtos.task.FilterTaskRequestDto;
import com.vku.job.dtos.task.TaskResponseDto;
import com.vku.job.dtos.task.UpdateTaskByUserRequestDto;
import com.vku.job.dtos.task.UpdateTaskRequestDto;
import com.vku.job.services.TaskExportService;
import com.vku.job.services.TaskService;
import com.vku.job.services.auth.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "APIs for task management")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskExportService taskExportService;

    @Autowired
    private JwtService jwtService;

    @PostMapping
    @Operation(summary = "Create Task", description = "Create a new task with the provided details")
    public ResponseEntity<TaskResponseDto> createTask(@RequestBody @Valid CreateTaskRequestDto createTaskRequestDto) {

        TaskResponseDto taskResponseDto = taskService.addTask(createTaskRequestDto);
        return ResponseEntity.status(201).body(taskResponseDto);
    }

    @GetMapping
    @Operation(summary = "Get All Tasks", description = "Retrieve a paginated list of all tasks with optional sorting")
    public ResponseEntity<PaginatedResponseDto<TaskResponseDto>> getTasks(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "order", defaultValue = "asc") String order) {
        return ResponseEntity.ok(taskService.getAllTasks(page, size, sortBy, order));

    }

    @GetMapping("/get-by-id")
    @Operation(summary = "Get Task by ID", description = "Retrieve task details by its ID")
    public ResponseEntity<TaskResponseDto> getTaskById(@RequestParam("id") Long id) {
        TaskResponseDto task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @DeleteMapping
    @Operation(summary = "Delete Task", description = "Delete a task by its ID")
    public ResponseEntity<Void> deleteTask(@RequestParam("id") Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping
    @Operation(summary = "Update Task", description = "Update task details by its ID")
    public ResponseEntity<TaskResponseDto> updateTask(@RequestBody @Valid UpdateTaskRequestDto updateTaskRequestDto,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtService.extractUserIdFromToken(token);
        TaskResponseDto updatedTask = taskService.updateTask(updateTaskRequestDto, userId);
        return ResponseEntity.ok(updatedTask);
    }

    @GetMapping("/by-user")
    @Operation(summary = "Get Tasks by User", description = "Retrieve a paginated list of tasks assigned to a specific user with optional sorting")
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
    @Operation(summary = "Update Task by User", description = "Allow a user to update their own task details")
    public ResponseEntity<TaskResponseDto> updateTaskByUser(
            @RequestBody @Valid UpdateTaskByUserRequestDto updateTaskRequestDto,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtService.extractUserIdFromToken(token);
        TaskResponseDto updatedTask = taskService.updateTaskByUser(updateTaskRequestDto, userId);
        return ResponseEntity.ok(updatedTask);
    }

    @GetMapping("/by-user/search-title")
    @Operation(summary = "Get Tasks by User and Title", description = "Retrieve a paginated list of tasks for a specific user filtered by title with optional sorting")
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

    @PostMapping("/by-user/filter-status")
    @Operation(summary = "Get Tasks by User and Status", description = "Retrieve a paginated list of tasks for the authenticated user filtered by status and other criteria")
    public ResponseEntity<PaginatedResponseDto<TaskResponseDto>> getTasksByUserAndStatus(
            @RequestBody FilterTaskRequestDto filterTaskRequestDto,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtService.extractUserIdFromToken(token);
        filterTaskRequestDto.setUserId(userId);
        PaginatedResponseDto<TaskResponseDto> tasksByUserAndStatus = taskService.getTasksByUserAndStatus(
                filterTaskRequestDto);
        return ResponseEntity.ok(tasksByUserAndStatus);
    }

    @GetMapping("/search-by-title")
    @Operation(summary = "Search Tasks by Title", description = "Search for tasks by title with pagination and sorting")
    public ResponseEntity<PaginatedResponseDto<TaskResponseDto>> searchTasksByTitle(
            @RequestParam("title") String title,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "order", defaultValue = "asc") String order) {
        PaginatedResponseDto<TaskResponseDto> tasks = taskService.searchTasksByTitle(title, page, size, sortBy, order);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping("/filter-by-status")
    @Operation(summary = "Filter Tasks by Status", description = "Filter tasks by status and other criteria with pagination")
    public ResponseEntity<PaginatedResponseDto<TaskResponseDto>> filterTasks(
            @RequestBody FilterTaskRequestDto filterTaskRequestDto) {

        System.out.println(filterTaskRequestDto);

        PaginatedResponseDto<TaskResponseDto> tasks = taskService.filterTasks(filterTaskRequestDto);
        return ResponseEntity.ok(tasks);
    }

    // export tasks to excel
    @GetMapping("/export")
    @Operation(summary = "Export Tasks to Excel", description = "Export all tasks to an Excel file")
    public ResponseEntity<byte[]> exportTasks() {
        List<TaskResponseDto> tasks = taskService.getTasksForExport();

        byte[] excelStream = taskExportService.exportTasksToExcel(tasks);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tasks.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelStream);

    }

    @GetMapping("/export-by-user")
    @Operation(summary = "Export User's Tasks to Excel", description = "Export tasks assigned to the authenticated user to an Excel file")
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