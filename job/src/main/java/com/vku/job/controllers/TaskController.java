package com.vku.job.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vku.job.dtos.PaginatedResponseDto;
import com.vku.job.dtos.task.CreateTaskRequestDto;
import com.vku.job.dtos.task.TaskResponseDto;
import com.vku.job.dtos.task.UpdateTaskRequestDto;
import com.vku.job.services.TaskService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(@RequestBody @Valid CreateTaskRequestDto createTaskRequestDto) {

        TaskResponseDto taskResponseDto = taskService.addTask(createTaskRequestDto);
        return ResponseEntity.status(201).body(taskResponseDto);
    }

    @GetMapping
    public ResponseEntity<PaginatedResponseDto<TaskResponseDto>> getTasks(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(taskService.getAllTasks(page, size));

    }

    @DeleteMapping
    public ResponseEntity<Void> deleteTask(@RequestParam("id") Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping
    public ResponseEntity<TaskResponseDto> updateTask(@RequestBody @Valid UpdateTaskRequestDto updateTaskRequestDto) {
        TaskResponseDto updatedTask = taskService.updateTask(updateTaskRequestDto);
        return ResponseEntity.ok(updatedTask);
    }

    @GetMapping("/by-user")
    public ResponseEntity<PaginatedResponseDto<TaskResponseDto>> getTasksByUser(
            @RequestParam("userId") Long userId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        PaginatedResponseDto<TaskResponseDto> tasksByUser = taskService.getTasksByUserId(userId, page, size);
        return ResponseEntity.ok(tasksByUser);
    }

    @GetMapping("/by-user/search-title")
    public ResponseEntity<PaginatedResponseDto<TaskResponseDto>> getTasksByUserAndTitle(
            @RequestParam("userId") Long userId,
            @RequestParam("title") String title,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        PaginatedResponseDto<TaskResponseDto> tasksByUserAndTitle = taskService.getTasksByUserAndTitle(userId, title, page, size);
        return ResponseEntity.ok(tasksByUserAndTitle);
    }

    @GetMapping("/by-user/filter-status")
    public ResponseEntity<PaginatedResponseDto<TaskResponseDto>> getTasksByUserAndStatus
            (@RequestParam("userId") Long userId,
            @RequestParam("status") String status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        PaginatedResponseDto<TaskResponseDto> tasksByUserAndStatus = taskService.getTasksByUserAndStatus(userId, status, page, size);
        return ResponseEntity.ok(tasksByUserAndStatus);
    }

    @GetMapping("/search-by-title")
    public ResponseEntity<PaginatedResponseDto<TaskResponseDto>> searchTasksByTitle(
            @RequestParam("title") String title,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        PaginatedResponseDto<TaskResponseDto> tasks = taskService.searchTasksByTitle(title, page, size);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/filter-by-status")
    public ResponseEntity<PaginatedResponseDto<TaskResponseDto>> filterTasksByStatus(
            @RequestParam("status") String status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        PaginatedResponseDto<TaskResponseDto> tasks = taskService.filterTasksByStatus(status, page, size);
        return ResponseEntity.ok(tasks);
    }

}
