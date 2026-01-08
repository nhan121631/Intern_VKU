package com.vku.job.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vku.job.dtos.PaginatedResponseDto;
import com.vku.job.dtos.task.CreateTastRequestDto;
import com.vku.job.dtos.task.TaskResponseDto;
import com.vku.job.entities.Task;
import com.vku.job.services.TaskService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(@RequestBody @Valid CreateTastRequestDto createTaskRequestDto) {

        TaskResponseDto taskResponseDto = taskService.addTask(createTaskRequestDto);
        return ResponseEntity.status(201).body(taskResponseDto);
    }

    @GetMapping
    public ResponseEntity<PaginatedResponseDto<TaskResponseDto>> getTasks(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "7") int size) {
        return ResponseEntity.ok(taskService.getAllTasks(page, size));

    }

}
