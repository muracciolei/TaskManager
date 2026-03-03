package com.example.taskmanager.repository;

import com.example.taskmanager.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Task entity operations.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Finds all tasks belonging to a specific user.
     *
     * @param userId the ID of the user
     * @return list of tasks belonging to the user
     */
    List<Task> findByUserId(Long userId);

    /**
     * Finds all tasks belonging to a user with a specific status.
     *
     * @param userId the ID of the user
     * @param status the status to filter by
     * @return list of tasks with the specified status
     */
    List<Task> findByUserIdAndStatus(Long userId, Task.Status status);
}
