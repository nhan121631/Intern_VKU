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

@RestController
@RequestMapping("/api/task-histories")
public class TaskHistoryController {
    @Autowired
    private TaskHistoryService taskHistoryService;

    // Get simple task histories by task ID
    @GetMapping("by-task-id")
    public ResponseEntity<List<TaskHistoryResponse>> getTaskHistoriesByTaskId(@RequestParam("taskId") Long taskId) {
        List<TaskHistoryResponse> histories = taskHistoryService.getSimpleTaskHistoriesByTaskId(taskId);
        System.out.println(histories);
        return ResponseEntity.ok(histories);
    }

    // Get task history detail by its ID
    @GetMapping("detail-by-id")
    public ResponseEntity<TaskHistoryDetailResponseDto> getTaskHistoryDetailById(@RequestParam("id") Long id) {
        TaskHistoryDetailResponseDto historyDetail = taskHistoryService.getTaskHistoriesDetailById(id);
        return ResponseEntity.ok(historyDetail);
    }
}
