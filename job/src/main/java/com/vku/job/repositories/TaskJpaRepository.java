package com.vku.job.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vku.job.entities.Task;
import com.vku.job.enums.TaskStatus;
import com.vku.job.repositories.projection.TaskForUserProjection;
import com.vku.job.repositories.projection.TaskStatusUserProjection;
import com.vku.job.repositories.projection.TaskSummaryProjection;

@Repository
public interface TaskJpaRepository extends JpaRepository<Task, Long> {

        Page<Task> findByAssignedUserId(Long userId, Pageable pageable);

        @Query("""
                            SELECT t FROM Task t
                            WHERE (:userId IS NULL OR t.assignedUser.id = :userId)
                              AND (:status IS NULL OR t.status = :status)
                              AND (:from IS NULL OR t.createdAt >= :from)
                              AND (:to IS NULL OR t.createdAt <= :to)
                        """)
        Page<Task> filterTasks(
                        @Param("userId") Long userId,
                        @Param("status") TaskStatus status,
                        @Param("from") LocalDateTime from,
                        @Param("to") LocalDateTime to,
                        Pageable pageable);

        Page<Task> findByTitleContainingIgnoreCase(String title, Pageable pageable);

        Page<Task> findByAssignedUserIdAndTitleContainingIgnoreCase(Long userId, String title, Pageable pageable);

        Page<Task> findByStatus(TaskStatus status, Pageable pageable);

        Page<Task> findByAssignedUserIdAndStatus(Long userId, TaskStatus status, Pageable pageable);

        List<Task> findByAssignedUserId(Long userId);

        // Statistics queries task summary by status
        @Query("SELECT t.status AS status, COUNT(t) AS count FROM Task t GROUP BY t.status")
        List<TaskSummaryProjection> countTasksByStatus();

        // Statistics queries task summary by user
        @Query("""
                            SELECT
                                up.fullName AS fullName,
                                COUNT(t) AS count
                            FROM Task t
                            JOIN t.assignedUser u
                            JOIN u.profile up
                            GROUP BY up.fullName
                        """)
        List<TaskForUserProjection> countTasksByUser();

        @Query("""
                            SELECT
                                t.status AS status,
                                COUNT(t) AS count
                            FROM Task t
                            WHERE t.assignedUser.id = :userId
                            GROUP BY t.status
                        """)
        List<TaskSummaryProjection> countTasksByStatusByUser(
                        @Param("userId") Long userId);

        // Statistics queries task summary by status between dates
        @Query("""
                            SELECT t.status AS status, COUNT(t) AS count
                            FROM Task t
                            WHERE t.createdAt BETWEEN :start AND :end
                            GROUP BY t.status
                        """)
        List<TaskSummaryProjection> countTasksByStatusBetween(
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        // Statistics queries task summary by user between dates
        @Query("""
                            SELECT up.fullName AS fullName, COUNT(t) AS count
                            FROM Task t
                            JOIN t.assignedUser u
                            JOIN u.profile up
                            WHERE t.createdAt BETWEEN :start AND :end
                            GROUP BY up.fullName
                        """)
        List<TaskForUserProjection> countTasksByUserBetween(
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        @Query("""
                            SELECT
                            up.fullName AS fullName,
                                t.status AS status,
                                COUNT(t) AS value
                            FROM Task t
                            JOIN t.assignedUser u
                            JOIN u.profile up
                            WHERE t.createdAt BETWEEN :start AND :end
                            GROUP BY up.fullName, t.status
                        """)
        List<TaskStatusUserProjection> countTasksByStatusByAllUserBetween(
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        @Query("""
                            SELECT
                            up.fullName AS fullName,
                                t.status AS status,
                                COUNT(t) AS value
                            FROM Task t
                            JOIN t.assignedUser u
                            JOIN u.profile up
                            Where u.id = :userId
                            AND t.createdAt BETWEEN :start AND :end
                            GROUP BY up.fullName, t.status
                        """)
        List<TaskStatusUserProjection> countTasksByStatusByUserBetween(
                        @Param("userId") Long userId,
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

}
