package com.vku.job.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vku.job.entities.Task;

@Repository
public interface TaskJpaRepository extends JpaRepository<Task, Long> {

    // Page<Task> getAllTasks(Pageable pageable);
}
