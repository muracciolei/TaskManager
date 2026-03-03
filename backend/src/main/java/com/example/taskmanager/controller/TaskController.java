package com.example.taskmanager.controller;

import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for task operations.
 */
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    /**
     * Task service for task operations.
     */
    private final TaskService taskService;

    /**
     * Gets all tasks for the authenticated user.
     * Optional query parameter: status (TODO, IN_PROGRESS, DONE)
     *
     * @param status optional status filter
     * @return list of task responses
     */
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasks(
            @RequestParam(required = false) Task.Status status) {
        
        List<TaskResponse> tasks;
        if (status != null) {
            tasks = taskService.getTasksByStatus(status);
        } else {
            tasks = taskService.getAllTasks();
        }
        
        return ResponseEntity.ok(tasks);
    }

    /**
     * Creates a new task.
     *
     * @param request the task request
     * @return the created task response
     */
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request) {
        TaskResponse response = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Updates an existing task.
     *
     * @param id the task ID
     * @param request the task request
     * @return the updated task response
     */
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id, 
            @Valid @RequestBody TaskRequest request) {
        
        TaskResponse response = taskService.updateTask(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a task.
     *
     * @param id the task ID
     * @return no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
