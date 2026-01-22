package com.vku.job.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vku.job.dtos.task_history.TaskHistoryDetailResponseDto;
import com.vku.job.dtos.task_history.TaskHistoryResponse;
import com.vku.job.entities.Role;
import com.vku.job.entities.TaskHistory;
import com.vku.job.entities.User;
import com.vku.job.entities.UserProfile;
import com.vku.job.repositories.TaskHistoryJpaRepository;
import com.vku.job.services.TaskHistoryService;

@ExtendWith(MockitoExtension.class)
public class TaskHistoryServiceTest {

    @Mock
    private TaskHistoryJpaRepository taskHistoryJpaRepository;

    @InjectMocks
    private TaskHistoryService taskHistoryService;

    // ====== GET TASK HISTORY DETAIL BY ID ======

    // success - full data
    @Test
    void getTaskHistoriesDetailById_success_fullData() {
        // ===== Arrange =====
        Role roleAdmin = new Role();
        roleAdmin.setName("ADMIN");

        UserProfile profile = new UserProfile();
        profile.setFullName("Nguyen Van A");

        User updater = new User();
        updater.setProfile(profile);
        updater.setRoles(List.of(roleAdmin));

        TaskHistory history = new TaskHistory();
        history.setId(1L);
        history.setUpdatedBy(updater);
        history.setOldData("{old}");
        history.setNewData("{new}");
        history.setUpdatedAt(LocalDateTime.now());

        Mockito.when(taskHistoryJpaRepository.findById(1L))
                .thenReturn(Optional.of(history));

        // ===== Act =====
        TaskHistoryDetailResponseDto dto = taskHistoryService.getTaskHistoriesDetailById(1L);

        // ===== Assert =====
        assertEquals(1L, dto.getId());
        assertEquals("Nguyen Van A", dto.getUpdatedByName());
        assertEquals(List.of("ADMIN"), dto.getRoles());
        assertEquals("{old}", dto.getOldData());
        assertEquals("{new}", dto.getNewData());
        assertNotNull(dto.getUpdatedAt());
    }

    // success - null updatedBy
    @Test
    void getTaskHistoriesDetailById_updatedByNull() {
        TaskHistory history = new TaskHistory();
        history.setId(1L);
        history.setUpdatedBy(null);

        Mockito.when(taskHistoryJpaRepository.findById(1L))
                .thenReturn(Optional.of(history));

        TaskHistoryDetailResponseDto dto = taskHistoryService.getTaskHistoriesDetailById(1L);

        assertNull(dto.getUpdatedByName());
        assertEquals(List.of(), dto.getRoles());
    }

    // success - updatedBy with null profile
    @Test
    void getTaskHistoriesDetailById_profileNull() {
        User updater = new User();
        updater.setProfile(null);
        updater.setRoles(List.of());

        TaskHistory history = new TaskHistory();
        history.setUpdatedBy(updater);

        Mockito.when(taskHistoryJpaRepository.findById(1L))
                .thenReturn(Optional.of(history));

        TaskHistoryDetailResponseDto dto = taskHistoryService.getTaskHistoriesDetailById(1L);

        assertNull(dto.getUpdatedByName());
        assertEquals(List.of(), dto.getRoles());
    }

    // role list is null
    @Test
    void getTaskHistoriesDetailById_rolesNull() {
        UserProfile profile = new UserProfile();
        profile.setFullName("Nguyen Van A");

        User updater = new User();
        updater.setProfile(profile);
        updater.setRoles(null);

        TaskHistory history = new TaskHistory();
        history.setUpdatedBy(updater);

        Mockito.when(taskHistoryJpaRepository.findById(1L))
                .thenReturn(Optional.of(history));

        TaskHistoryDetailResponseDto dto = taskHistoryService.getTaskHistoriesDetailById(1L);

        assertEquals("Nguyen Van A", dto.getUpdatedByName());
        assertEquals(List.of(), dto.getRoles());
    }

    // failure - task history not found
    @Test
    void getTaskHistoriesDetailById_notFound() {
        Mockito.when(taskHistoryJpaRepository.findById(1L))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> taskHistoryService.getTaskHistoriesDetailById(1L));

        assertEquals("Task history not found", ex.getMessage());
    }
    // ====== END GET TASK HISTORY DETAIL BY ID ======

    // ====== GET SIMPLE TASK HISTORIES BY TASK ID ======

    // success
    @Test
    void getSimpleTaskHistoriesByTaskId_success_fullData() {
        // ===== Arrange =====
        UserProfile profile = new UserProfile();
        profile.setFullName("Nguyen Van A");

        User updater = new User();
        updater.setProfile(profile);

        TaskHistory history = new TaskHistory();
        history.setId(1L);
        history.setUpdatedBy(updater);
        history.setUpdatedAt(LocalDateTime.now());

        Mockito.when(taskHistoryJpaRepository.findByTaskId(10L))
                .thenReturn(List.of(history));

        // ===== Act =====
        List<TaskHistoryResponse> result = taskHistoryService.getSimpleTaskHistoriesByTaskId(10L);

        // ===== Assert =====
        assertEquals(1, result.size());

        TaskHistoryResponse dto = result.get(0);
        assertEquals(1L, dto.getId());
        assertEquals("Nguyen Van A", dto.getUpdateBy());
        assertNotNull(dto.getUpdatedAt());
    }

    // updatedBy is null
    @Test
    void getSimpleTaskHistoriesByTaskId_updatedByNull() {
        TaskHistory history = new TaskHistory();
        history.setId(1L);
        history.setUpdatedBy(null);
        history.setUpdatedAt(LocalDateTime.now());

        Mockito.when(taskHistoryJpaRepository.findByTaskId(10L))
                .thenReturn(List.of(history));

        List<TaskHistoryResponse> result = taskHistoryService.getSimpleTaskHistoriesByTaskId(10L);

        assertEquals(1, result.size());
        assertNull(result.get(0).getUpdateBy());
    }

    // profile is null
    @Test
    void getSimpleTaskHistoriesByTaskId_profileNull() {
        User updater = new User();
        updater.setProfile(null);

        TaskHistory history = new TaskHistory();
        history.setId(1L);
        history.setUpdatedBy(updater);
        history.setUpdatedAt(LocalDateTime.now());

        Mockito.when(taskHistoryJpaRepository.findByTaskId(10L))
                .thenReturn(List.of(history));

        List<TaskHistoryResponse> result = taskHistoryService.getSimpleTaskHistoriesByTaskId(10L);

        assertEquals(1, result.size());
        assertNull(result.get(0).getUpdateBy());
    }

    // empty list
    @Test
    void getSimpleTaskHistoriesByTaskId_emptyList() {
        Mockito.when(taskHistoryJpaRepository.findByTaskId(10L))
                .thenReturn(List.of());

        List<TaskHistoryResponse> result = taskHistoryService.getSimpleTaskHistoriesByTaskId(10L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

}
