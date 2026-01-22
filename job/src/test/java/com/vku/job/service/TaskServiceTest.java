package com.vku.job.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
import com.vku.job.entities.UserProfile;
import com.vku.job.enums.TaskStatus;
import com.vku.job.repositories.TaskHistoryJpaRepository;
import com.vku.job.repositories.TaskJpaRepository;
import com.vku.job.repositories.UserJpaRepository;
import com.vku.job.services.TaskService;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {
    @InjectMocks
    private TaskService taskService;

    @Mock
    private TaskJpaRepository taskRepository;

    @Mock
    private UserJpaRepository userRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private TaskHistoryJpaRepository taskHistoryRepository;

    @Test
    void convertToDto_withAssignedUser_success() {
        // given
        UserProfile profile = new UserProfile();
        profile.setFullName("Nhan Pham");

        User user = new User();
        user.setId(10L);
        UserProfile userProfile = new UserProfile();
        userProfile.setFullName("Assigned User");
        userProfile.setUser(user);
        user.setProfile(userProfile);
        user.setProfile(profile);

        Task task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("Description");
        task.setDeadline(LocalDate.now());
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setAllowUserUpdate(true);
        task.setAssignedUser(user);
        task.setCreatedAt(LocalDateTime.now());

        // when
        TaskResponseDto dto = taskService.convertToDto(task);

        // then
        assertEquals(1L, dto.getId());
        assertEquals("Test Task", dto.getTitle());
        assertEquals("Description", dto.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS.name(), dto.getStatus());
        assertTrue(dto.isAllowUserUpdate());

        assertEquals("Nhan Pham", dto.getAssignedFullName());
        assertEquals(10L, dto.getAssignedUserId());

        assertNotNull(dto.getCreatedAt());
    }

    @Test
    void toTaskHistoryDto_withAssignedUser_success() {
        // given
        User user = new User();
        user.setId(5L);

        Task task = new Task();
        task.setId(1L);
        task.setTitle("Task A");
        task.setDescription("Description");
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setDeadline(LocalDate.now());
        task.setAllowUserUpdate(true);
        task.setAssignedUser(user);

        // when
        UpdateTaskHistoryResponseDto dto = taskService.toTaskHistoryDto(task);

        // then
        assertEquals(1L, dto.getId());
        assertEquals("Task A", dto.getTitle());
        assertEquals("Description", dto.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, dto.getStatus());
        assertEquals(user.getId(), dto.getAssignedUserId());
        assertTrue(dto.isAllowUserUpdate());
    }

    // ======= ADD TASK TEST =======

    // add task - success
    @Test
    void addTask_success() {
        // given
        CreateTaskRequestDto dto = new CreateTaskRequestDto();
        dto.setTitle("Task A");
        dto.setStatus("OPEN");
        dto.setAssignedUserId(1L);

        UserProfile profile = new UserProfile();
        profile.setFullName("Nhan Pham");

        User user = new User();
        user.setId(1L);
        user.setProfile(profile);

        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        Task savedTask = new Task();
        savedTask.setId(10L);
        savedTask.setTitle("Task A");
        savedTask.setStatus(TaskStatus.OPEN);
        savedTask.setAssignedUser(user);

        Mockito.when(taskRepository.save(any(Task.class)))
                .thenReturn(savedTask);

        // when
        TaskResponseDto response = taskService.addTask(dto);

        // then
        assertEquals(10L, response.getId());
        assertEquals("Nhan Pham", response.getAssignedFullName());

        verify(taskRepository).save(any(Task.class));
    }

    // add task - failure: assigned user not found
    @Test
    void addTask_assignedUserNotFound() {
        // given
        CreateTaskRequestDto dto = new CreateTaskRequestDto();
        dto.setTitle("Task A");
        dto.setStatus("OPEN");
        dto.setAssignedUserId(1L);

        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        // when & then
        RuntimeException ex = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> taskService.addTask(dto));

        assertEquals("User not found", ex.getMessage());
    }

    // ======= END ADD TASK TEST =======

    // ======= GET ALL TASKS WITH PAGINATION =======

    // get all tasks with pagination - success
    @Test
    void getAllTasks_success_sortAsc() {
        // given
        UserProfile profile = new UserProfile();
        profile.setFullName("Nhan Pham");

        User user = new User();
        user.setId(1L);
        user.setProfile(profile);

        Task task = new Task();
        task.setId(1L);
        task.setTitle("Task A");
        task.setStatus(TaskStatus.OPEN);
        task.setAssignedUser(user);

        Pageable pageable = PageRequest.of(
                0,
                10,
                Sort.by("title").ascending());

        Page<Task> taskPage = new PageImpl<>(
                List.of(task),
                pageable,
                1);

        Mockito.when(taskRepository.findAll(any(Pageable.class)))
                .thenReturn(taskPage);

        // when
        PaginatedResponseDto<TaskResponseDto> response = taskService.getAllTasks(0, 10, "title", "asc");

        // then
        assertEquals(1, response.getData().size());
        assertEquals(0, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getTotalPages());

        TaskResponseDto dto = response.getData().get(0);
        assertEquals("Task A", dto.getTitle());
        assertEquals("Nhan Pham", dto.getAssignedFullName());

        verify(taskRepository).findAll(any(Pageable.class));
    }

    // get all tasks with pagination - success
    @Test
    void getAllTasks_success_sortDesc() {
        // given
        UserProfile profile = new UserProfile();
        profile.setFullName("Nhan Pham");
        User user = new User();
        user.setId(1L);
        user.setProfile(profile);
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Task A");
        task.setStatus(TaskStatus.OPEN);
        task.setAssignedUser(user);
        Pageable pageable = PageRequest.of(
                0,
                10,
                Sort.by("title").descending());
        Page<Task> taskPage = new PageImpl<>(

                List.of(task),
                pageable,
                1);
        Mockito.when(taskRepository.findAll(any(Pageable.class)))
                .thenReturn(taskPage);
        // when
        PaginatedResponseDto<TaskResponseDto> response = taskService.getAllTasks(0,
                10,
                "title",
                "desc");
        // then
        assertEquals(1, response.getData().size());
        assertEquals(0, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        TaskResponseDto dto = response.getData().get(0);
        assertEquals("Task A", dto.getTitle());
        assertEquals("Nhan Pham", dto.getAssignedFullName());
        verify(taskRepository).findAll(any(Pageable.class));
    }

    // get all tasks with pagination - success: empty page
    @Test
    void getAllTasks_success_emptyPage() {
        // given
        Pageable pageable = PageRequest.of(1, 10, Sort.by("title").ascending());

        Page<Task> taskPage = new PageImpl<>(
                List.of(),
                pageable,
                0);

        Mockito.when(taskRepository.findAll(any(Pageable.class)))
                .thenReturn(taskPage);

        // when
        PaginatedResponseDto<TaskResponseDto> response = taskService.getAllTasks(1, 10, "title", "asc");

        // then
        assertEquals(0, response.getData().size());
        assertEquals(1, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(0, response.getTotalElements());
        assertEquals(0, response.getTotalPages());

        verify(taskRepository).findAll(any(Pageable.class));
    }

    // ======= END GET ALL TASKS WITH PAGINATION =======

    // ======= DELETE TASK BY ID TEST =======

    // delete task by id - success
    @Test
    void deleteTask_success() {
        // given
        Long taskId = 1L;

        Task task = new Task();
        task.setId(taskId);

        Mockito.when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        // when
        taskService.deleteTask(taskId);

        // then
        verify(taskRepository).findById(taskId);
        verify(taskRepository).deleteById(taskId);
    }

    // delete task by id - failure: task not found
    @Test
    void deleteTask_taskNotFound_throwException() {
        // given
        Long taskId = 99L;

        Mockito.when(taskRepository.findById(taskId))
                .thenReturn(Optional.empty());

        // when
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> taskService.deleteTask(taskId));

        // then
        assertEquals("Task not found", ex.getMessage());

        verify(taskRepository, never()).deleteById(any());
    }
    // ======= END DELETE TASK BY ID TEST =======

    // ======= UPDATE TASK TEST =======

    // update task - success save history
    @Test
    void updateTask_success_withHistory() throws Exception {
        // given
        Long taskId = 1L;
        Long currentUserId = 10L;

        Task task = new Task();
        task.setId(taskId);
        task.setTitle("Old title");
        task.setCreatedAt(LocalDateTime.now().minusDays(1));
        task.setStatus(TaskStatus.OPEN);

        UpdateTaskRequestDto dto = new UpdateTaskRequestDto();
        dto.setId(taskId);
        dto.setTitle("New title");
        dto.setAllowUserUpdate(true);
        dto.setDeadline(LocalDate.now().plusDays(1));
        dto.setStatus("IN_PROGRESS");

        User updater = new User();
        updater.setId(currentUserId);

        Mockito.when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        Mockito.when(taskRepository.save(any(Task.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Mockito.when(objectMapper.writeValueAsString(any()))
                .thenReturn("OLD_DATA")
                .thenReturn("NEW_DATA");

        Mockito.when(userRepository.findById(currentUserId))
                .thenReturn(Optional.of(updater));

        // when
        TaskResponseDto result = taskService.updateTask(dto, currentUserId);

        // then
        assertEquals("New title", result.getTitle());
        verify(taskHistoryRepository).save(any(TaskHistory.class));
    }

    // update task - success don't save history
    @Test
    void updateTask_success_noHistory_whenDataNotChanged() throws Exception {
        // given
        Long taskId = 1L;
        Long currentUserId = 10L;

        Task task = new Task();
        task.setId(taskId);
        task.setCreatedAt(LocalDateTime.now().minusDays(1));

        UpdateTaskRequestDto dto = new UpdateTaskRequestDto();
        dto.setId(taskId);

        Mockito.when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        Mockito.when(taskRepository.save(any(Task.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Mockito.when(objectMapper.writeValueAsString(any()))
                .thenReturn("SAME_DATA");

        Mockito.when(userRepository.findById(currentUserId))
                .thenReturn(Optional.of(new User()));

        // when
        taskService.updateTask(dto, currentUserId);

        // then
        verify(taskHistoryRepository, never()).save(any());
    }

    // update task - failure: task not found
    @Test
    void updateTask_taskNotFound() {
        UpdateTaskRequestDto dto = new UpdateTaskRequestDto();
        dto.setId(99L);

        Mockito.when(taskRepository.findById(99L))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> taskService.updateTask(dto, 1L));

        assertEquals("Task not found", ex.getMessage());
    }

    // update task - failure: deadline > createdAt
    @Test
    void updateTask_deadlineBeforeCreatedAt_throwException() throws Exception {
        Task task = new Task();
        task.setId(1L);
        task.setCreatedAt(LocalDateTime.now());

        UpdateTaskRequestDto dto = new UpdateTaskRequestDto();
        dto.setId(1L);
        dto.setDeadline(LocalDate.now().minusDays(1));

        Mockito.when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        Mockito.when(objectMapper.writeValueAsString(any()))
                .thenReturn("OLD");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> taskService.updateTask(dto, 1L));

        assertEquals("Deadline cannot be before created date", ex.getMessage());
    }

    // update task - failure: assigned user not found
    @Test
    void updateTask_assignedUserNotFound_throwException() throws Exception {
        Task task = new Task();
        task.setId(1L);
        task.setCreatedAt(LocalDateTime.now().minusDays(1));

        UpdateTaskRequestDto dto = new UpdateTaskRequestDto();
        dto.setId(1L);
        dto.setAssignedUserId(99L);

        Mockito.when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        Mockito.when(objectMapper.writeValueAsString(any()))
                .thenReturn("OLD_DATA");

        Mockito.when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> taskService.updateTask(dto, 1L));

        assertEquals("User not found", ex.getMessage());
    }

    // ======= END UPDATE TASK TEST =======

    // ======= GET TASK BY USER ID WITH PAGINATION TEST=======

    // get task by user id - success
    @Test
    void getTasksByUserId_success() {
        // given
        Long userId = 1L;
        int page = 0;
        int size = 2;

        Task task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Task 1");
        task1.setStatus(TaskStatus.OPEN);
        task1.setCreatedAt(LocalDateTime.now());

        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task 2");
        task2.setStatus(TaskStatus.IN_PROGRESS);
        task2.setCreatedAt(LocalDateTime.now());

        Page<Task> taskPage = new PageImpl<>(
                List.of(task1, task2),
                PageRequest.of(page, size),
                2);

        Mockito.when(taskRepository.findByAssignedUserId(
                eq(userId),
                any(Pageable.class)))
                .thenReturn(taskPage);

        // when
        PaginatedResponseDto<TaskResponseDto> result = taskService.getTasksByUserId(userId, page, size, "id", "asc");

        // then
        assertEquals(2, result.getData().size());
        assertEquals(0, result.getPage());
        assertEquals(2, result.getSize());
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());

        verify(taskRepository).findByAssignedUserId(eq(userId), any(Pageable.class));
    }

    // get task by user id - success: empty page
    @Test
    void getTasksByUserId_emptyResult() {
        // given
        Long userId = 1L;

        Page<Task> emptyPage = Page.empty();

        Mockito.when(taskRepository.findByAssignedUserId(
                eq(userId),
                any(Pageable.class)))
                .thenReturn(emptyPage);

        // when
        PaginatedResponseDto<TaskResponseDto> result = taskService.getTasksByUserId(userId, 0, 10, "id", "asc");

        // then
        assertTrue(result.getData().isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    // get task by user id - invalid sortBy field
    @Test
    void getTasksByUserId_invalidSortBy_defaultToId() {
        // given
        Long userId = 1L;

        Mockito.when(taskRepository.findByAssignedUserId(
                eq(userId),
                any(Pageable.class)))
                .thenReturn(Page.empty());

        // when
        taskService.getTasksByUserId(userId, 0, 10, "invalidField", "asc");

        // then
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(taskRepository).findByAssignedUserId(eq(userId), captor.capture());

        Pageable pageable = captor.getValue();
        assertEquals("id", pageable.getSort().iterator().next().getProperty());
    }

    // get task by user id - sort descending
    @Test
    void getTasksByUserId_sortDesc() {
        // given
        Long userId = 1L;

        Mockito.when(taskRepository.findByAssignedUserId(
                eq(userId),
                any(Pageable.class)))
                .thenReturn(Page.empty());

        // when
        taskService.getTasksByUserId(userId, 0, 10, "createdAt", "desc");

        // then
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(taskRepository).findByAssignedUserId(eq(userId), captor.capture());

        Sort.Order order = captor.getValue().getSort().iterator().next();
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }

    // ======= END GET TASK BY USER ID WITH PAGINATION TEST=======

    // ======= GET TASK BY ID TEST =======

    // get task by id - success
    @Test
    void getTaskById_success() {
        // given
        Long taskId = 1L;

        UserProfile profile = new UserProfile();
        profile.setFullName("Nhan Pham");

        User user = new User();
        user.setId(10L);
        user.setProfile(profile);

        Task task = new Task();
        task.setId(taskId);
        task.setTitle("Test Task");
        task.setDescription("Desc");
        task.setStatus(TaskStatus.OPEN);
        task.setAllowUserUpdate(true);
        task.setAssignedUser(user);
        task.setCreatedAt(LocalDateTime.now());

        Mockito.when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        // when
        TaskResponseDto response = taskService.getTaskById(taskId);

        // then
        assertEquals(taskId, response.getId());
        assertEquals("Test Task", response.getTitle());
        assertEquals("Nhan Pham", response.getAssignedFullName());
        assertEquals(10L, response.getAssignedUserId());

        verify(taskRepository).findById(taskId);
    }

    // get task by id - failure: task not found
    @Test
    void getTaskById_notFound_throwException() {
        // given
        Long taskId = 99L;

        Mockito.when(taskRepository.findById(taskId))
                .thenReturn(Optional.empty());

        // when & then
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> taskService.getTaskById(taskId));

        assertEquals("Task not found", ex.getMessage());
    }
    // ======= END GET TASK BY ID TEST =======

    // ======= SEARCH TASKS BY TITLE WITH PAGINATION =======

    // search tasks by title - success
    @Test
    void searchTasksByTitle_success() {
        // given
        String title = "test";
        int page = 0;
        int size = 10;

        UserProfile profile = new UserProfile();
        profile.setFullName("Nhan Pham");

        User user = new User();
        user.setId(1L);
        user.setProfile(profile);

        Task task = new Task();
        task.setId(100L);
        task.setTitle("Test Task");
        task.setDescription("Desc");
        task.setStatus(TaskStatus.OPEN);
        task.setAllowUserUpdate(true);
        task.setAssignedUser(user);
        task.setCreatedAt(LocalDateTime.now());

        Page<Task> taskPage = new PageImpl<>(
                List.of(task),
                PageRequest.of(page, size),
                1);

        Mockito.when(taskRepository.findByTitleContainingIgnoreCase(
                eq(title),
                any(Pageable.class))).thenReturn(taskPage);

        // when
        PaginatedResponseDto<TaskResponseDto> response = taskService.searchTasksByTitle(title, page, size, "id", "asc");

        // then
        assertEquals(1, response.getData().size());
        assertEquals(100L, response.getData().get(0).getId());
        assertEquals("Test Task", response.getData().get(0).getTitle());
        assertEquals("Nhan Pham", response.getData().get(0).getAssignedFullName());

        assertEquals(0, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(1, response.getTotalElements());

        verify(taskRepository).findByTitleContainingIgnoreCase(
                eq(title),
                any(Pageable.class));
    }

    // search tasks by title - success: empty result
    @Test
    void searchTasksByTitle_emptyResult() {
        // given
        String title = "notfound";
        int page = 0;
        int size = 10;

        Page<Task> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(page, size),
                0);

        Mockito.when(taskRepository.findByTitleContainingIgnoreCase(
                eq(title),
                any(Pageable.class))).thenReturn(emptyPage);

        // when
        PaginatedResponseDto<TaskResponseDto> response = taskService.searchTasksByTitle(title, page, size, "title",
                "desc");

        // then
        assertTrue(response.getData().isEmpty());
        assertEquals(0, response.getTotalElements());
        assertEquals(0, response.getTotalPages());

        verify(taskRepository).findByTitleContainingIgnoreCase(
                eq(title),
                any(Pageable.class));
    }
    // ======= END SEARCH TASKS BY TITLE WITH PAGINATION =======

    // ======= FILTER TASKS WITH PAGINATION =======
    // filter tasks - success by userId
    @Test
    void filterTasks_byUserId_success() {
        FilterTaskRequestDto dto = new FilterTaskRequestDto();
        dto.setUserId(1L);
        dto.setPage(0);
        dto.setSize(10);
        dto.setSortBy("id");
        dto.setOrder("asc");

        UserProfile profile = new UserProfile();
        profile.setFullName("Nhan Pham");

        User user = new User();
        user.setId(1L);
        user.setProfile(profile);

        Task task = new Task();
        task.setId(100L);
        task.setTitle("Task 1");
        task.setStatus(TaskStatus.OPEN);
        task.setAssignedUser(user);
        task.setCreatedAt(LocalDateTime.now());

        Page<Task> page = new PageImpl<>(
                List.of(task),
                PageRequest.of(0, 10),
                1);

        Mockito.when(taskRepository.filterTasks(
                eq(1L),
                isNull(),
                isNull(),
                isNull(),
                any(Pageable.class))).thenReturn(page);

        PaginatedResponseDto<TaskResponseDto> response = taskService.filterTasks(dto);

        assertEquals(1, response.getData().size());
        assertEquals("Nhan Pham", response.getData().get(0).getAssignedFullName());

        verify(taskRepository).filterTasks(
                eq(1L),
                isNull(),
                isNull(),
                isNull(),
                any(Pageable.class));
    }

    // filter tasks - success by status + date
    @Test
    void filterTasks_statusAndDate_success() {
        FilterTaskRequestDto dto = new FilterTaskRequestDto();
        dto.setStatus("OPEN");
        dto.setCreateAtFrom("2024-06-01");
        dto.setCreateAtTo("2024-06-30");
        dto.setPage(0);
        dto.setSize(10);

        Page<Task> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 10),
                0);

        Mockito.when(taskRepository.filterTasks(
                isNull(),
                eq(TaskStatus.OPEN),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(Pageable.class))).thenReturn(emptyPage);

        PaginatedResponseDto<TaskResponseDto> response = taskService.filterTasks(dto);

        assertTrue(response.getData().isEmpty());
    }

    // filter tasks - don't have any filter criteria
    @Test
    void filterTasks_noCondition_throwException() {
        FilterTaskRequestDto dto = new FilterTaskRequestDto();
        dto.setPage(0);
        dto.setSize(10);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> taskService.filterTasks(dto));

        assertEquals("At least one filter condition is required", ex.getMessage());
    }

    // filer tasks - createAtFrom > createAtTo
    @Test
    void filterTasks_invalidDateRange_throwException() {
        FilterTaskRequestDto dto = new FilterTaskRequestDto();
        dto.setCreateAtFrom("2024-06-10");
        dto.setCreateAtTo("2024-06-01");
        dto.setPage(0);
        dto.setSize(10);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> taskService.filterTasks(dto));

        assertEquals("createAtFrom cannot be after createAtTo", ex.getMessage());
    }

    // filter tasks - invalid status
    @Test
    void filterTasks_invalidStatus_throwException() {
        FilterTaskRequestDto dto = new FilterTaskRequestDto();
        dto.setStatus("INVALID_STATUS");
        dto.setPage(0);
        dto.setSize(10);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> taskService.filterTasks(dto));

        assertEquals("Invalid task status", ex.getMessage());
    }

    // ======= END FILTER TASKS WITH PAGINATION =======

    // ======= GET TASKS BY USER ID AND TITLE WITH PAGINATION =======

    // get tasks by user id and title - success
    @Test
    void getTasksByUserAndTitle_success() {
        // given
        Long userId = 1L;
        String title = "report";

        UserProfile profile = new UserProfile();
        profile.setFullName("Nhan Pham");

        User user = new User();
        user.setId(userId);
        user.setProfile(profile);

        Task task = new Task();
        task.setId(10L);
        task.setTitle("Monthly report");
        task.setStatus(TaskStatus.OPEN);
        task.setAssignedUser(user);
        task.setCreatedAt(LocalDateTime.now());

        Page<Task> page = new PageImpl<>(
                List.of(task),
                PageRequest.of(0, 10),
                1);

        Mockito.when(taskRepository.findByAssignedUserIdAndTitleContainingIgnoreCase(
                eq(userId),
                eq(title),
                any(Pageable.class))).thenReturn(page);

        // when
        PaginatedResponseDto<TaskResponseDto> response = taskService.getTasksByUserAndTitle(userId, title, 0, 10, "id",
                "asc");

        // then
        assertEquals(1, response.getData().size());
        assertEquals("Monthly report", response.getData().get(0).getTitle());
        assertEquals("Nhan Pham", response.getData().get(0).getAssignedFullName());

        verify(taskRepository).findByAssignedUserIdAndTitleContainingIgnoreCase(
                eq(userId),
                eq(title),
                any(Pageable.class));
    }

    // get tasks by user id and title - empty result
    @Test
    void getTasksByUserAndTitle_emptyResult() {
        Long userId = 1L;
        String title = "not-exist";

        Page<Task> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 10),
                0);

        Mockito.when(taskRepository.findByAssignedUserIdAndTitleContainingIgnoreCase(
                eq(userId),
                eq(title),
                any(Pageable.class))).thenReturn(emptyPage);

        PaginatedResponseDto<TaskResponseDto> response = taskService.getTasksByUserAndTitle(userId, title, 0, 10,
                "title", "desc");

        assertTrue(response.getData().isEmpty());
        assertEquals(0, response.getTotalElements());
    }

    // get tasks by user id and title - invalid sortBy field
    @Test
    void getTasksByUserAndTitle_invalidSortBy_defaultToId() {
        Long userId = 1L;

        Mockito.when(taskRepository.findByAssignedUserIdAndTitleContainingIgnoreCase(
                eq(userId),
                anyString(),
                any(Pageable.class))).thenReturn(Page.empty());

        taskService.getTasksByUserAndTitle(userId, "test", 0, 10, "abc", "asc");

        verify(taskRepository).findByAssignedUserIdAndTitleContainingIgnoreCase(
                eq(userId),
                eq("test"),
                any(Pageable.class));
    }

    // ====== END GET TASKS BY USER ID AND TITLE WITH PAGINATION =======

    // ====== FILTER TASKS BY ASSIGNED USER AND STATUS WITH PAGINATION =======

    // filter tasks by assigned user and status - don't have any filter criteria
    @Test
    void getTasksByUserAndStatus_noCondition_throwException() {
        FilterTaskRequestDto dto = new FilterTaskRequestDto();
        dto.setPage(0);
        dto.setSize(10);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> taskService.getTasksByUserAndStatus(dto));

        assertEquals("At least one filter condition is required", ex.getMessage());
    }

    // filter tasks by assigned user and status - ceateateAtFrom > createAtTo
    @Test
    void getTasksByUserAndStatus_invalidDateRange_throwException() {
        FilterTaskRequestDto dto = new FilterTaskRequestDto();
        dto.setCreateAtFrom("2024-06-10");
        dto.setCreateAtTo("2024-06-01");
        dto.setPage(0);
        dto.setSize(10);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> taskService.getTasksByUserAndStatus(dto));

        assertEquals("createAtFrom cannot be after createAtTo", ex.getMessage());
    }

    // filter tasks by assigned user and status - invalid status
    @Test
    void getTasksByUserAndStatus_invalidStatus_throwException() {
        FilterTaskRequestDto dto = new FilterTaskRequestDto();
        dto.setStatus("INVALID");
        dto.setPage(0);
        dto.setSize(10);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> taskService.getTasksByUserAndStatus(dto));

        assertEquals("Invalid task status", ex.getMessage());
    }

    // filter tasks by assigned user and status - success by userId
    @Test
    void getTasksByUserAndStatus_byUserId_success() {
        FilterTaskRequestDto dto = new FilterTaskRequestDto();
        dto.setUserId(1L);
        dto.setPage(0);
        dto.setSize(10);
        dto.setSortBy("id");
        dto.setOrder("asc");

        UserProfile profile = new UserProfile();
        profile.setFullName("Nhan Pham");

        User user = new User();
        user.setId(1L);
        user.setProfile(profile);

        Task task = new Task();
        task.setId(10L);
        task.setTitle("Task 1");
        task.setStatus(TaskStatus.OPEN);
        task.setAssignedUser(user);
        task.setCreatedAt(LocalDateTime.now());

        Page<Task> page = new PageImpl<>(
                List.of(task),
                PageRequest.of(0, 10),
                1);

        Mockito.when(taskRepository.filterTasks(
                eq(1L),
                isNull(),
                isNull(),
                isNull(),
                any(Pageable.class))).thenReturn(page);

        PaginatedResponseDto<TaskResponseDto> response = taskService.getTasksByUserAndStatus(dto);

        assertEquals(1, response.getData().size());
        assertEquals("Nhan Pham", response.getData().get(0).getAssignedFullName());
    }

    // filter tasks by assigned user and status - success by status + date
    @Test
    void getTasksByUserAndStatus_statusAndDate_success() {
        FilterTaskRequestDto dto = new FilterTaskRequestDto();
        dto.setStatus("OPEN");
        dto.setCreateAtFrom("2024-06-01");
        dto.setCreateAtTo("2024-06-30");
        dto.setPage(0);
        dto.setSize(10);

        Page<Task> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 10),
                0);

        Mockito.when(taskRepository.filterTasks(
                isNull(),
                eq(TaskStatus.OPEN),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(Pageable.class))).thenReturn(emptyPage);

        PaginatedResponseDto<TaskResponseDto> response = taskService.getTasksByUserAndStatus(dto);

        assertTrue(response.getData().isEmpty());
    }

    // filter tasks by assigned user and status - empty result
    @Test
    void getTasksByUserAndStatus_emptyResult() {
        FilterTaskRequestDto dto = new FilterTaskRequestDto();
        dto.setUserId(99L);
        dto.setPage(0);
        dto.setSize(10);

        Mockito.when(taskRepository.filterTasks(
                eq(99L),
                isNull(),
                isNull(),
                isNull(),
                any(Pageable.class))).thenReturn(Page.empty());

        PaginatedResponseDto<TaskResponseDto> response = taskService.getTasksByUserAndStatus(dto);

        assertEquals(0, response.getTotalElements());
        assertTrue(response.getData().isEmpty());
    }

    // ====== END FILTER TASKS BY ASSIGNED USER AND STATUS WITH PAGINATION =======

    // ====== UPDATE TASK BY USER ID TEST =======

    // update task by user id - task not found
    @Test
    void updateTaskByUser_taskNotFound_throwException() {
        UpdateTaskByUserRequestDto dto = new UpdateTaskByUserRequestDto();
        dto.setId(1L);

        Mockito.when(taskRepository.findById(1L))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> taskService.updateTaskByUser(dto, 10L));

        assertEquals("Task not found", ex.getMessage());
    }

    // update task by user id - user not allowed to update
    @Test
    void updateTaskByUser_notAllowed_throwException() {
        Task task = new Task();
        task.setId(1L);
        task.setAllowUserUpdate(false);

        Mockito.when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        UpdateTaskByUserRequestDto dto = new UpdateTaskByUserRequestDto();
        dto.setId(1L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> taskService.updateTaskByUser(dto, 10L));

        assertEquals("User is not allowed to update this task", ex.getMessage());
    }

    // update task by user id - createAt after deadline
    @Test
    void updateTaskByUser_deadlineBeforeCreated_throwException() throws Exception {
        Task task = new Task();
        task.setId(1L);
        task.setAllowUserUpdate(true);
        task.setCreatedAt(LocalDateTime.of(2024, 6, 10, 10, 0));

        Mockito.when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        Mockito.when(objectMapper.writeValueAsString(any()))
                .thenReturn("old");

        UpdateTaskByUserRequestDto dto = new UpdateTaskByUserRequestDto();
        dto.setId(1L);
        dto.setDeadline(LocalDate.of(2024, 6, 1));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> taskService.updateTaskByUser(dto, 10L));

        assertEquals("Deadline cannot be before created date", ex.getMessage());
    }

    // update task by user id - success and save history
    @Test
    void updateTaskByUser_success_withHistory() throws Exception {
        Task task = new Task();
        task.setId(1L);
        task.setAllowUserUpdate(true);
        task.setTitle("Old title");
        task.setCreatedAt(LocalDateTime.now());

        Mockito.when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        Mockito.when(taskRepository.save(any(Task.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Mockito.when(objectMapper.writeValueAsString(any()))
                .thenReturn("old-data", "new-data");

        User user = new User();
        user.setId(10L);

        Mockito.when(userRepository.findById(10L))
                .thenReturn(Optional.of(user));

        UpdateTaskByUserRequestDto dto = new UpdateTaskByUserRequestDto();
        dto.setId(1L);
        dto.setTitle("New title");

        TaskResponseDto response = taskService.updateTaskByUser(dto, 10L);

        assertNotNull(response);

        verify(taskHistoryRepository).save(any(TaskHistory.class));
    }

    // update task by user id - success but no history saved
    @Test
    void updateTaskByUser_noChange_noHistorySaved() throws Exception {
        Task task = new Task();
        task.setId(1L);
        task.setAllowUserUpdate(true);
        task.setCreatedAt(LocalDateTime.now());

        Mockito.when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        Mockito.when(taskRepository.save(any(Task.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Mockito.when(objectMapper.writeValueAsString(any()))
                .thenReturn("same-data");

        UpdateTaskByUserRequestDto dto = new UpdateTaskByUserRequestDto();
        dto.setId(1L);

        taskService.updateTaskByUser(dto, 10L);

        verify(taskHistoryRepository, never()).save(any());
    }

    // update task by user id - assigned user not found
    @Test
    void updateTaskByUser_updatedByNotFound_throwException() throws Exception {
        Task task = new Task();
        task.setId(1L);
        task.setAllowUserUpdate(true);
        task.setCreatedAt(LocalDateTime.now());

        Mockito.when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        Mockito.when(taskRepository.save(any(Task.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Mockito.when(objectMapper.writeValueAsString(any()))
                .thenReturn("old", "new");

        Mockito.when(userRepository.findById(10L))
                .thenReturn(Optional.empty());

        UpdateTaskByUserRequestDto dto = new UpdateTaskByUserRequestDto();
        dto.setId(1L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> taskService.updateTaskByUser(dto, 10L));

        assertEquals("User not found", ex.getMessage());
    }

    // ====== END UPDATE TASK BY USER ID TEST =======

    // ====== GET TASKS FOR EXPORT TEST =======

    // get tasks for export - success
    @Test
    void getTasksForExport_success() {
        // ===== Arrange =====
        UserProfile profile = new UserProfile();
        profile.setFullName("Nguyen Van A");

        User user = new User();
        user.setId(1L);
        user.setProfile(profile);

        Task task = new Task();
        task.setId(10L);
        task.setTitle("Test task");
        task.setDescription("Test description");
        task.setStatus(TaskStatus.OPEN);
        task.setAllowUserUpdate(true);
        task.setAssignedUser(user);
        task.setCreatedAt(LocalDateTime.now());
        task.setDeadline(LocalDate.now().plusDays(3));

        when(taskRepository.findAll()).thenReturn(List.of(task));

        // ===== Act =====
        List<TaskResponseDto> result = taskService.getTasksForExport();

        // ===== Assert =====
        assertNotNull(result);
        assertEquals(1, result.size());

        TaskResponseDto dto = result.get(0);
        assertEquals(10L, dto.getId());
        assertEquals("Test task", dto.getTitle());
        assertEquals("Test description", dto.getDescription());
        assertEquals("OPEN", dto.getStatus());
        assertEquals(true, dto.isAllowUserUpdate());
        assertEquals(1L, dto.getAssignedUserId());
        assertEquals("Nguyen Van A", dto.getAssignedFullName());

        verify(taskRepository, times(1)).findAll();
    }

    // get tasks for export - empty result
    @Test
    void getTasksForExport_emptyList() {
        when(taskRepository.findAll()).thenReturn(Collections.emptyList());

        List<TaskResponseDto> result = taskService.getTasksForExport();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(taskRepository, times(1)).findAll();
    }

    // ====== END GET TASKS FOR EXPORT TEST =======

    // ====== GET TASK TASKS FOR USER EXPORT TEST=======

    // get tasks for user export - success
    @Test
    void getTasksForExportByUserId_success() {
        // ===== Arrange =====
        Long userId = 1L;

        UserProfile profile = new UserProfile();
        profile.setFullName("Nguyen Van A");

        User user = new User();
        user.setId(userId);
        user.setProfile(profile);

        Task task1 = new Task();
        task1.setId(10L);
        task1.setTitle("Task 1");
        task1.setDescription("Description 1");
        task1.setStatus(TaskStatus.OPEN);
        task1.setAllowUserUpdate(true);
        task1.setAssignedUser(user);
        task1.setCreatedAt(LocalDateTime.now());
        task1.setDeadline(LocalDate.now().plusDays(2));

        Task task2 = new Task();
        task2.setId(11L);
        task2.setTitle("Task 2");
        task2.setDescription("Description 2");
        task2.setStatus(TaskStatus.IN_PROGRESS);
        task2.setAllowUserUpdate(false);
        task2.setAssignedUser(user);
        task2.setCreatedAt(LocalDateTime.now());
        task2.setDeadline(LocalDate.now().plusDays(5));

        when(taskRepository.findByAssignedUserId(userId))
                .thenReturn(List.of(task1, task2));

        // ===== Act =====
        List<TaskResponseDto> result = taskService.getTasksForExportByUserId(userId);

        // ===== Assert =====
        assertNotNull(result);
        assertEquals(2, result.size());

        TaskResponseDto dto1 = result.get(0);
        assertEquals(10L, dto1.getId());
        assertEquals("Task 1", dto1.getTitle());
        assertEquals("OPEN", dto1.getStatus());
        assertEquals(userId, dto1.getAssignedUserId());
        assertEquals("Nguyen Van A", dto1.getAssignedFullName());

        TaskResponseDto dto2 = result.get(1);
        assertEquals(11L, dto2.getId());
        assertEquals("Task 2", dto2.getTitle());
        assertEquals("IN_PROGRESS", dto2.getStatus());

        verify(taskRepository, times(1))
                .findByAssignedUserId(userId);
    }

    // get tasks for user export - empty result
    @Test
    void getTasksForExportByUserId_emptyList() {
        Long userId = 99L;

        when(taskRepository.findByAssignedUserId(userId))
                .thenReturn(Collections.emptyList());

        List<TaskResponseDto> result = taskService.getTasksForExportByUserId(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(taskRepository, times(1))
                .findByAssignedUserId(userId);
    }

}
