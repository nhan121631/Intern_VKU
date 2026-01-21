package com.vku.job.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vku.job.dtos.PaginatedResponseDto;
import com.vku.job.dtos.task.CreateTaskRequestDto;
import com.vku.job.dtos.task.FilterTaskRequestDto;
import com.vku.job.dtos.task.TaskResponseDto;
import com.vku.job.dtos.task.UpdateTaskByUserRequestDto;
import com.vku.job.dtos.task.UpdateTaskRequestDto;
import com.vku.job.dtos.task_history.UpdateTaskHistoryResponseDto;
import com.vku.job.entities.Task;
import com.vku.job.entities.TaskHistory;
import com.vku.job.entities.User;
import com.vku.job.enums.TaskStatus;
import com.vku.job.repositories.TaskHistoryJpaRepository;
import com.vku.job.repositories.TaskJpaRepository;
import com.vku.job.repositories.UserJpaRepository;

@Service
public class TaskService {

    @Autowired
    private TaskJpaRepository taskRepository;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskHistoryJpaRepository taskHistoryRepository;

    private TaskResponseDto convertToDto(Task task) {
        return TaskResponseDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .deadline(task.getDeadline())
                .status(task.getStatus().name())
                .allowUserUpdate(task.isAllowUserUpdate())
                .assignedFullName(
                        task.getAssignedUser() != null ? task.getAssignedUser().getProfile().getFullName() : null)
                .assignedUserId(task.getAssignedUser() != null ? task.getAssignedUser().getId() : null)
                .createdAt(task.getCreatedAt())
                .build();
    }

    private UpdateTaskHistoryResponseDto toTaskHistoryDto(Task task) {
        return UpdateTaskHistoryResponseDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .deadline(task.getDeadline())
                .allowUserUpdate(task.isAllowUserUpdate())
                .assignedUserId(
                        task.getAssignedUser() != null
                                ? task.getAssignedUser().getId()
                                : null)
                .build();
    }

    // Add task
    public TaskResponseDto addTask(CreateTaskRequestDto createTastRequestDto) {

        Task task = new Task();
        task.setTitle(createTastRequestDto.getTitle());
        task.setDescription(createTastRequestDto.getDescription());
        task.setDeadline(createTastRequestDto.getDeadline());
        task.setStatus(TaskStatus.valueOf(createTastRequestDto.getStatus()));
        task.setAllowUserUpdate(createTastRequestDto.isAllowUserUpdate());
        if (createTastRequestDto.getAssignedUserId() != null) {
            User user = userRepository.findById(createTastRequestDto.getAssignedUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            task.setAssignedUser(user);
        }

        Task savedTask = taskRepository.save(task);
        return convertToDto(savedTask);
    }

    // get all tasks with pagination
    public PaginatedResponseDto<TaskResponseDto> getAllTasks(
            int page,
            int size,
            String sortBy,
            String order) {
        sortBy = switch (sortBy) {
            case "id", "title", "createdAt", "deadline" -> sortBy;
            default -> "id";
        };

        Sort sort = order.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Task> tasks = taskRepository.findAll(pageable);

        return new PaginatedResponseDto<>(
                tasks.map(this::convertToDto).getContent(),
                tasks.getNumber(),
                tasks.getSize(),
                tasks.getTotalElements(),
                tasks.getTotalPages(),
                tasks.hasNext(),
                tasks.hasPrevious());
    }

    // delete task by id
    public void deleteTask(Long id) {
        taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        taskRepository.deleteById(id);
    }

    // update task
    public TaskResponseDto updateTask(UpdateTaskRequestDto updateTaskRequestDto, Long currentUserId) {
        Task task = taskRepository.findById(updateTaskRequestDto.getId())
                .orElseThrow(() -> new RuntimeException("Task not found"));

        String oldData;
        try {
            oldData = objectMapper.writeValueAsString(toTaskHistoryDto(task));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot serialize old task data");
        }

        task.setTitle(updateTaskRequestDto.getTitle());
        task.setDescription(updateTaskRequestDto.getDescription());
        task.setAllowUserUpdate(updateTaskRequestDto.isAllowUserUpdate());
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

        String newData;
        try {
            newData = objectMapper.writeValueAsString(toTaskHistoryDto(updatedTask));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot serialize new task data");
        }
        User updatedBy = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (oldData.equals(newData)) {
            return convertToDto(updatedTask);
        }

        TaskHistory history = TaskHistory.builder()
                .task(updatedTask)
                .updatedBy(updatedBy)
                .oldData(oldData)
                .newData(newData)
                .build();

        taskHistoryRepository.save(history);
        return convertToDto(updatedTask);
    }

    // get tasks by user id with pagination
    public PaginatedResponseDto<TaskResponseDto> getTasksByUserId(
            Long userId,
            int page,
            int size,
            String sortBy,
            String order) {

        sortBy = switch (sortBy) {
            case "id", "title", "createdAt", "deadline" -> sortBy;
            default -> "id";
        };

        Sort sort = order.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Task> tasks = taskRepository.findByAssignedUserId(userId, pageable);

        return new PaginatedResponseDto<>(
                tasks.map(this::convertToDto).getContent(),
                tasks.getNumber(),
                tasks.getSize(),
                tasks.getTotalElements(),
                tasks.getTotalPages(),
                tasks.hasNext(),
                tasks.hasPrevious());
    }

    // get task by id
    public TaskResponseDto getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        return convertToDto(task);
    }

    // search tasks by title with pagination
    public PaginatedResponseDto<TaskResponseDto> searchTasksByTitle(
            String title,
            int page,
            int size,
            String sortBy,
            String order) {
        sortBy = switch (sortBy) {
            case "id", "title", "createdAt", "deadline" -> sortBy;
            default -> "id";
        };

        Sort sort = order.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Task> tasks = taskRepository.findByTitleContainingIgnoreCase(title, pageable);

        return new PaginatedResponseDto<>(
                tasks.map(this::convertToDto).getContent(),
                tasks.getNumber(),
                tasks.getSize(),
                tasks.getTotalElements(),
                tasks.getTotalPages(),
                tasks.hasNext(),
                tasks.hasPrevious());
    }

    public PaginatedResponseDto<TaskResponseDto> filterTasks(FilterTaskRequestDto dto) {

        // 1. Validate: ít nhất 1 điều kiện
        if (dto.getUserId() == null
                && (dto.getStatus() == null || dto.getStatus().isBlank())
                && (dto.getCreateAtFrom() == null || dto.getCreateAtFrom().isBlank())
                && (dto.getCreateAtTo() == null || dto.getCreateAtTo().isBlank())) {
            throw new RuntimeException("At least one filter condition is required");
        }

        // 2. Validate date range
        if (dto.getCreateAtFrom() != null && !dto.getCreateAtFrom().isBlank()
                && dto.getCreateAtTo() != null && !dto.getCreateAtTo().isBlank()) {
            if (dto.getCreateAtFrom().compareTo(dto.getCreateAtTo()) > 0) {
                throw new RuntimeException("createAtFrom cannot be after createAtTo");
            }
        }

        // 3. Parse status
        TaskStatus taskStatus = null;
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            try {
                taskStatus = TaskStatus.valueOf(dto.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid task status");
            }
        }

        // 4. Sort whitelist
        String sortBy = switch (dto.getSortBy()) {
            case "id", "title", "createdAt", "deadline" -> dto.getSortBy();
            default -> "id";
        };

        Sort sort = "desc".equalsIgnoreCase(dto.getOrder())
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(dto.getPage(), dto.getSize(), sort);

        // 5. Normalize date
        LocalDateTime from = dto.getCreateAtFrom() != null && !dto.getCreateAtFrom().isBlank()
                ? LocalDate.parse(dto.getCreateAtFrom()).atStartOfDay()
                : null;

        LocalDateTime to = dto.getCreateAtTo() != null && !dto.getCreateAtTo().isBlank()
                ? LocalDate.parse(dto.getCreateAtTo()).atTime(23, 59, 59)
                : null;

        Page<Task> tasks = taskRepository.filterTasks(
                dto.getUserId(),
                taskStatus,
                from,
                to,
                pageable);

        return new PaginatedResponseDto<>(
                tasks.map(this::convertToDto).getContent(),
                tasks.getNumber(),
                tasks.getSize(),
                tasks.getTotalElements(),
                tasks.getTotalPages(),
                tasks.hasNext(),
                tasks.hasPrevious());
    }

    // get tasks by user id and title with pagination
    public PaginatedResponseDto<TaskResponseDto> getTasksByUserAndTitle(
            Long userId,
            String title,
            int page,
            int size,
            String sortBy,
            String order) {
        sortBy = switch (sortBy) {
            case "id", "title", "createdAt", "deadline" -> sortBy;
            default -> "id";
        };

        Sort sort = order.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Task> tasks = taskRepository.findByAssignedUserIdAndTitleContainingIgnoreCase(
                userId,
                title,
                pageable);

        return new PaginatedResponseDto<>(
                tasks.map(this::convertToDto).getContent(),
                tasks.getNumber(),
                tasks.getSize(),
                tasks.getTotalElements(),
                tasks.getTotalPages(),
                tasks.hasNext(),
                tasks.hasPrevious());
    }

    // get tasks by user id and status with pagination
    public PaginatedResponseDto<TaskResponseDto> getTasksByUserAndStatus(
            FilterTaskRequestDto dto) {
        // 1. Validate: ít nhất 1 điều kiện
        if (dto.getUserId() == null
                && (dto.getStatus() == null || dto.getStatus().isBlank())
                && (dto.getCreateAtFrom() == null || dto.getCreateAtFrom().isBlank())
                && (dto.getCreateAtTo() == null || dto.getCreateAtTo().isBlank())) {
            throw new RuntimeException("At least one filter condition is required");
        }

        // 2. Validate date range
        if (dto.getCreateAtFrom() != null && !dto.getCreateAtFrom().isBlank()
                && dto.getCreateAtTo() != null && !dto.getCreateAtTo().isBlank()) {
            if (dto.getCreateAtFrom().compareTo(dto.getCreateAtTo()) > 0) {
                throw new RuntimeException("createAtFrom cannot be after createAtTo");
            }
        }

        // 3. Parse status
        TaskStatus taskStatus = null;
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            try {
                taskStatus = TaskStatus.valueOf(dto.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid task status");
            }
        }

        // 4. Sort whitelist
        String sortBy = switch (dto.getSortBy()) {
            case "id", "title", "createdAt", "deadline" -> dto.getSortBy();
            default -> "id";
        };

        Sort sort = "desc".equalsIgnoreCase(dto.getOrder())
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(dto.getPage(), dto.getSize(), sort);

        // 5. Normalize date
        LocalDateTime from = dto.getCreateAtFrom() != null && !dto.getCreateAtFrom().isBlank()
                ? LocalDate.parse(dto.getCreateAtFrom()).atStartOfDay()
                : null;

        LocalDateTime to = dto.getCreateAtTo() != null && !dto.getCreateAtTo().isBlank()
                ? LocalDate.parse(dto.getCreateAtTo()).atTime(23, 59, 59)
                : null;

        Page<Task> tasks = taskRepository.filterTasks(
                dto.getUserId(),
                taskStatus,
                from,
                to,
                pageable);

        return new PaginatedResponseDto<>(
                tasks.map(this::convertToDto).getContent(),
                tasks.getNumber(),
                tasks.getSize(),
                tasks.getTotalElements(),
                tasks.getTotalPages(),
                tasks.hasNext(),
                tasks.hasPrevious());
    }

    // user update task by id
    public TaskResponseDto updateTaskByUser(UpdateTaskByUserRequestDto updateTaskRequestDto, Long currentUserId) {
        Task task = taskRepository.findById(updateTaskRequestDto.getId())
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.isAllowUserUpdate()) {
            throw new RuntimeException("User is not allowed to update this task");
        }
        String oldData;
        try {
            oldData = objectMapper.writeValueAsString(toTaskHistoryDto(task));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot serialize old task data");
        }
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

        String newData;
        try {
            newData = objectMapper.writeValueAsString(toTaskHistoryDto(updatedTask));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot serialize new task data");
        }

        if (oldData.equals(newData)) {
            return convertToDto(updatedTask);
        }

        User updatedBy = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TaskHistory history = TaskHistory.builder()
                .task(updatedTask)
                .updatedBy(updatedBy)
                .oldData(oldData)
                .newData(newData)
                .build();
        taskHistoryRepository.save(history);
        return convertToDto(updatedTask);
    }

    // get all tasks for export
    public List<TaskResponseDto> getTasksForExport() {
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream().map(this::convertToDto).toList();
    }

    // get tasks for export by user id
    public List<TaskResponseDto> getTasksForExportByUserId(Long userId) {
        List<Task> tasks = taskRepository.findByAssignedUserId(userId);
        return tasks.stream().map(this::convertToDto).toList();
    }
}
