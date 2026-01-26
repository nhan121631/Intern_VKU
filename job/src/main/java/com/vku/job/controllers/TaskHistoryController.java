package com.vku.job.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vku.job.dtos.task_history.TaskHistoryDetailResponseDto;
import com.vku.job.dtos.task_history.TaskHistoryResponse;
import com.vku.job.services.TaskHistoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/task-histories")
@Tag(name = "Task Histories", description = "APIs for managing task histories")
@SecurityRequirement(name = "bearerAuth")
public class TaskHistoryController {
    @Autowired
    private TaskHistoryService taskHistoryService;

    // Get simple task histories by task ID
    @GetMapping("by-task-id")
    @Operation(summary = "Get Task Histories by Task ID", description = "Retrieve simple task histories for a specific task")
    public ResponseEntity<List<TaskHistoryResponse>> getTaskHistoriesByTaskId(@RequestParam("taskId") Long taskId) {
        List<TaskHistoryResponse> histories = taskHistoryService.getSimpleTaskHistoriesByTaskId(taskId);
        System.out.println(histories);
        return ResponseEntity.ok(histories);
    }

    // Get task history detail by its ID
    @GetMapping("detail-by-id")
    @Operation(summary = "Get Task History Detail by ID", description = "Retrieve detailed information of a specific task history entry")
    public ResponseEntity<TaskHistoryDetailResponseDto> getTaskHistoryDetailById(@RequestParam("id") Long id) {
        TaskHistoryDetailResponseDto historyDetail = taskHistoryService.getTaskHistoriesDetailById(id);
        return ResponseEntity.ok(historyDetail);
    }
}
