package com.vku.job.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vku.job.entities.TaskHistory;

@Repository
public interface TaskHistoryJpaRepository extends JpaRepository<TaskHistory, Long> {

    Optional<TaskHistory> findById(Long id);

    List<TaskHistory> findByTaskId(Long taskId);
}
