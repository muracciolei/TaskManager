package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for task operations.
 */
@Service
@RequiredArgsConstructor
public class TaskService {

    /**
     * Logger for TaskService.
     */
    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    /**
     * Task repository for task operations.
     */
    private final TaskRepository taskRepository;

    /**
     * User repository for user operations.
     */
    private final UserRepository userRepository;

    /**
     * Gets all tasks for the authenticated user.
     *
     * @return list of task responses
     */
    public List<TaskResponse> getAllTasks() {
        Long userId = getCurrentUserId();
        logger.info("Fetching all tasks for user: {}", userId);
        
        return taskRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets tasks by status for the authenticated user.
     *
     * @param status the task status to filter by
     * @return list of task responses
     */
    public List<TaskResponse> getTasksByStatus(Task.Status status) {
        Long userId = getCurrentUserId();
        logger.info("Fetching tasks with status {} for user: {}", status, userId);
        
        return taskRepository.findByUserIdAndStatus(userId, status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new task for the authenticated user.
     *
     * @param request the task request
     * @return the created task response
     */
    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        Long userId = getCurrentUserId();
        logger.info("Creating task for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus() != null ? request.getStatus() : Task.Status.TODO);
        task.setUser(user);

        Task savedTask = taskRepository.save(task);
        logger.info("Task created with id: {}", savedTask.getId());
        
        return mapToResponse(savedTask);
    }

    /**
     * Updates an existing task.
     *
     * @param id the task ID
     * @param request the task request
     * @return the updated task response
     */
    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request) {
        Long userId = getCurrentUserId();
        logger.info("Updating task {} for user: {}", id, userId);
        
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        // Verify ownership
        if (!task.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Task not found with id: " + id);
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }

        Task updatedTask = taskRepository.save(task);
        logger.info("Task updated: {}", updatedTask.getId());
        
        return mapToResponse(updatedTask);
    }

    /**
     * Deletes a task.
     *
     * @param id the task ID
     */
    @Transactional
    public void deleteTask(Long id) {
        Long userId = getCurrentUserId();
        logger.info("Deleting task {} for user: {}", id, userId);
        
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        // Verify ownership
        if (!task.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Task not found with id: " + id);
        }

        taskRepository.delete(task);
        logger.info("Task deleted: {}", id);
    }

    /**
     * Gets the current authenticated user's ID.
     *
     * @return the user ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }

    /**
     * Maps a Task entity to TaskResponse DTO.
     *
     * @param task the task entity
     * @return the task response DTO
     */
    private TaskResponse mapToResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getCreatedAt(),
                task.getUser().getId(),
                task.getUser().getName()
        );
    }
}
