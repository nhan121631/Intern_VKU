package com.vku.job.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.vku.job.dtos.PaginatedResponseDto;
import com.vku.job.dtos.task.CreateTastRequestDto;
import com.vku.job.dtos.task.TaskResponseDto;
import com.vku.job.entities.Task;
import com.vku.job.entities.User;
import com.vku.job.repositories.TaskJpaRepository;
import com.vku.job.repositories.UserJpaRepository;

@Service
public class TaskService {

    @Autowired
    private TaskJpaRepository taskRepository;

    @Autowired
    private UserJpaRepository userRepository;

    private TaskResponseDto convertToDto(Task task) {
        return TaskResponseDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .deadline(task.getDeadline())
                .status(task.getStatus().name())
                .assignedUserId(task.getAssignedUser() != null ? task.getAssignedUser().getId() : null)
                .createdAt(task.getCreatedAt())
                .build();
    }

    // Add task
    public TaskResponseDto addTask(CreateTastRequestDto createTastRequestDto) {

        Task task = new Task();
        task.setTitle(createTastRequestDto.getTitle());
        task.setDescription(createTastRequestDto.getDescription());
        task.setDeadline(createTastRequestDto.getDeadline());
        // task.setStatus(TaskStatus.valueOf(createTastRequestDto.getStatus()));
        if (createTastRequestDto.getAssignedUserId() != null) {
            User user = userRepository.findById(createTastRequestDto.getAssignedUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            task.setAssignedUser(user);
        }

        Task savedTask = taskRepository.save(task);
        return convertToDto(savedTask);
    }

    // get all tasks with pagination
    public PaginatedResponseDto<TaskResponseDto> getAllTasks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Task> tasks = taskRepository.findAll(pageable);
        PaginatedResponseDto<TaskResponseDto> response = new PaginatedResponseDto<>(
                tasks.map(this::convertToDto).getContent(),
                tasks.getNumber(),
                tasks.getSize(),
                tasks.getTotalElements(),
                tasks.getTotalPages(),
                tasks.hasNext(),
                tasks.hasPrevious());
        return response;
    }
}
