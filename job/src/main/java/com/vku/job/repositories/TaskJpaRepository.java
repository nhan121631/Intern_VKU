package com.vku.job.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vku.job.entities.Task;

@Repository
public interface TaskJpaRepository extends JpaRepository<Task, Long> {

    Page<Task> findByAssignedUserId(Long userId, Pageable pageable);

    Page<Task> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Task> findByAssignedUserIdAndTitleContainingIgnoreCase(Long userId, String title, Pageable pageable);
}
