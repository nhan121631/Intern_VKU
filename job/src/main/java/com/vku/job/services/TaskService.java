package com.vku.job.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.vku.job.dtos.PaginatedResponseDto;
import com.vku.job.dtos.task.CreateTaskRequestDto;
import com.vku.job.dtos.task.TaskResponseDto;
import com.vku.job.dtos.task.UpdateTaskByUserRequestDto;
import com.vku.job.dtos.task.UpdateTaskRequestDto;
import com.vku.job.entities.Task;
import com.vku.job.entities.User;
import com.vku.job.enums.TaskStatus;
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
                .assignedFullName(task.getAssignedUser() != null ? task.getAssignedUser().getFullName() : null)
                .assignedUserId(task.getAssignedUser() != null ? task.getAssignedUser().getId() : null)
                .createdAt(task.getCreatedAt())
                .build();
    }

    // Add task
    public TaskResponseDto addTask(CreateTaskRequestDto createTastRequestDto) {

        Task task = new Task();
        task.setTitle(createTastRequestDto.getTitle());
        task.setDescription(createTastRequestDto.getDescription());
        task.setDeadline(createTastRequestDto.getDeadline());
        task.setStatus(TaskStatus.valueOf(createTastRequestDto.getStatus()));
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

    // delete task by id
    public void deleteTask(Long id) {
        taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        taskRepository.deleteById(id);
    }

    // update task by id
    public TaskResponseDto updateTask(UpdateTaskRequestDto updateTaskRequestDto) {
        Task task = taskRepository.findById(updateTaskRequestDto.getId())
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setTitle(updateTaskRequestDto.getTitle());
        task.setDescription(updateTaskRequestDto.getDescription());
        task.setDeadline(updateTaskRequestDto.getDeadline());
        if (updateTaskRequestDto.getStatus() != null) {
            task.setStatus(TaskStatus.valueOf(updateTaskRequestDto.getStatus()));
        }
        if (updateTaskRequestDto.getDeadline() != null &&
                updateTaskRequestDto.getDeadline().isBefore(task.getCreatedAt().toLocalDate())) {
            throw new RuntimeException("Deadline cannot be before created date");
        }
        if (updateTaskRequestDto.getAssignedUserId() != null) {
            User user = userRepository.findById(updateTaskRequestDto.getAssignedUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            task.setAssignedUser(user);
        } else {
            task.setAssignedUser(null);
        }

        Task updatedTask = taskRepository.save(task);
        return convertToDto(updatedTask);
    }

    // get tasks by user id with pagination
    public PaginatedResponseDto<TaskResponseDto> getTasksByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Task> tasks = taskRepository.findByAssignedUserId(userId, pageable);
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

    // get task by id
    public TaskResponseDto getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        return convertToDto(task);
    }

    // search tasks by title with pagination
    public PaginatedResponseDto<TaskResponseDto> searchTasksByTitle(String title, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Task> tasks = taskRepository.findByTitleContainingIgnoreCase(title, pageable);
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

    // filter tasks by status with pagination
    public PaginatedResponseDto<TaskResponseDto> filterTasksByStatus(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        TaskStatus taskStatus;
        try {
            taskStatus = TaskStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid task status");
        }
        Page<Task> tasks = taskRepository.findByStatus(taskStatus, pageable);
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

    // get tasks by user id and title with pagination
    public PaginatedResponseDto<TaskResponseDto> getTasksByUserAndTitle(Long userId, String title, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Task> tasks = taskRepository.findByAssignedUserIdAndTitleContainingIgnoreCase(userId, title, pageable);
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

    // get tasks by user id and status with pagination
    public PaginatedResponseDto<TaskResponseDto> getTasksByUserAndStatus(Long userId, String status, int page,
            int size) {
        Pageable pageable = PageRequest.of(page, size);
        TaskStatus taskStatus;
        try {
            taskStatus = TaskStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid task status");
        }
        Page<Task> tasks = taskRepository.findByAssignedUserIdAndStatus(userId, taskStatus, pageable);
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

    // user update task by id
    public TaskResponseDto updateTaskByUser(UpdateTaskByUserRequestDto updateTaskRequestDto) {
        Task task = taskRepository.findById(updateTaskRequestDto.getId())
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setTitle(updateTaskRequestDto.getTitle());
        task.setDescription(updateTaskRequestDto.getDescription());
        task.setDeadline(updateTaskRequestDto.getDeadline());
        if (updateTaskRequestDto.getStatus() != null) {
            task.setStatus(TaskStatus.valueOf(updateTaskRequestDto.getStatus()));
        }
        if (updateTaskRequestDto.getDeadline() != null &&
                updateTaskRequestDto.getDeadline().isBefore(task.getCreatedAt().toLocalDate())) {
            throw new RuntimeException("Deadline cannot be before created date");
        }
        Task updatedTask = taskRepository.save(task);
        return convertToDto(updatedTask);
    }
}
