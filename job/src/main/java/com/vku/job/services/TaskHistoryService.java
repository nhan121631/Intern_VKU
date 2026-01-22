package com.vku.job.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vku.job.dtos.task_history.TaskHistoryDetailResponseDto;
import com.vku.job.dtos.task_history.TaskHistoryResponse;
import com.vku.job.entities.TaskHistory;
import com.vku.job.repositories.TaskHistoryJpaRepository;

@Service
public class TaskHistoryService {

    @Autowired
    private TaskHistoryJpaRepository taskHistoryJpaRepository;

    // Get task history detail by id
    public TaskHistoryDetailResponseDto getTaskHistoriesDetailById(Long id) {
        TaskHistory taskHistory = taskHistoryJpaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task history not found"));

        TaskHistoryDetailResponseDto dto = new TaskHistoryDetailResponseDto();
        dto.setId(taskHistory.getId());

        if (taskHistory.getUpdatedBy() != null) {
            var updater = taskHistory.getUpdatedBy();
            dto.setUpdatedByName(
                    updater.getProfile() != null
                            ? updater.getProfile().getFullName()
                            : null);
            dto.setRoles(
                    updater.getRoles() != null
                            ? updater.getRoles()
                                    .stream()
                                    .map(role -> role.getName())
                                    .toList()
                            : List.of());
        } else {
            dto.setUpdatedByName(null);
            dto.setRoles(List.of());
        }

        dto.setOldData(taskHistory.getOldData());
        dto.setNewData(taskHistory.getNewData());
        dto.setUpdatedAt(taskHistory.getUpdatedAt());

        return dto;
    }

    public List<TaskHistoryResponse> getSimpleTaskHistoriesByTaskId(Long taskId) {
        return taskHistoryJpaRepository.findByTaskId(taskId).stream().map(taskHistory -> {
            TaskHistoryResponse dto = new TaskHistoryResponse();
            dto.setId(taskHistory.getId());
            if (taskHistory.getUpdatedBy() != null) {
                var updater = taskHistory.getUpdatedBy();
                dto.setUpdateBy(updater.getProfile() != null ? updater.getProfile().getFullName() : null);
            } else {
                dto.setUpdateBy(null);
            }
            dto.setUpdatedAt(taskHistory.getUpdatedAt());
            return dto;
        }).toList();
    }
}
