package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskService.
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private User testUser;
    private Task testTask;

    @BeforeEach
    void setUp() {
        // Set up test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");

        // Set up test task
        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setStatus(Task.Status.TODO);
        testTask.setUser(testUser);
        testTask.setCreatedAt(LocalDateTime.now());

        // Set up security context with authenticated user
        Authentication auth = new UsernamePasswordAuthenticationToken(testUser, null, Arrays.asList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void getAllTasks_ShouldReturnAllTasksForCurrentUser() {
        // Arrange
        List<Task> tasks = Arrays.asList(testTask);
        when(taskRepository.findByUserId(1L)).thenReturn(tasks);

        // Act
        List<TaskResponse> result = taskService.getAllTasks();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Task", result.get(0).getTitle());
        verify(taskRepository, times(1)).findByUserId(1L);
    }

    @Test
    void getTasksByStatus_ShouldReturnFilteredTasks() {
        // Arrange
        List<Task> tasks = Arrays.asList(testTask);
        when(taskRepository.findByUserIdAndStatus(1L, Task.Status.TODO)).thenReturn(tasks);

        // Act
        List<TaskResponse> result = taskService.getTasksByStatus(Task.Status.TODO);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Task.Status.TODO, result.get(0).getStatus());
        verify(taskRepository, times(1)).findByUserIdAndStatus(1L, Task.Status.TODO);
    }

    @Test
    void createTask_ShouldCreateNewTask() {
        // Arrange
        TaskRequest request = new TaskRequest();
        request.setTitle("New Task");
        request.setDescription("New Description");
        request.setStatus(Task.Status.TODO);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(2L);
            task.setCreatedAt(LocalDateTime.now());
            return task;
        });

        // Act
        TaskResponse result = taskService.createTask(request);

        // Assert
        assertNotNull(result);
        assertEquals("New Task", result.getTitle());
        assertEquals("New Description", result.getDescription());
        assertEquals(Task.Status.TODO, result.getStatus());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void createTask_WithNullStatus_ShouldDefaultToTODO() {
        // Arrange
        TaskRequest request = new TaskRequest();
        request.setTitle("New Task");
        request.setDescription("New Description");
        // Status is null

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(2L);
            task.setCreatedAt(LocalDateTime.now());
            return task;
        });

        // Act
        TaskResponse result = taskService.createTask(request);

        // Assert
        assertNotNull(result);
        assertEquals(Task.Status.TODO, result.getStatus());
    }

    @Test
    void updateTask_ShouldUpdateExistingTask() {
        // Arrange
        TaskRequest request = new TaskRequest();
        request.setTitle("Updated Title");
        request.setDescription("Updated Description");
        request.setStatus(Task.Status.IN_PROGRESS);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // Act
        TaskResponse result = taskService.updateTask(1L, request);

        // Assert
        assertNotNull(result);
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void updateTask_TaskNotFound_ShouldThrowException() {
        // Arrange
        TaskRequest request = new TaskRequest();
        request.setTitle("Updated Title");

        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> taskService.updateTask(999L, request));
    }

    @Test
    void updateTask_UnauthorizedUser_ShouldThrowException() {
        // Arrange
        TaskRequest request = new TaskRequest();
        request.setTitle("Updated Title");

        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setName("Other User");
        
        Task taskWithOtherUser = new Task();
        taskWithOtherUser.setId(1L);
        taskWithOtherUser.setTitle("Task");
        taskWithOtherUser.setUser(otherUser);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(taskWithOtherUser));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> taskService.updateTask(1L, request));
    }

    @Test
    void deleteTask_ShouldDeleteTask() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        doNothing().when(taskRepository).delete(testTask);

        // Act
        taskService.deleteTask(1L);

        // Assert
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).delete(testTask);
    }

    @Test
    void deleteTask_TaskNotFound_ShouldThrowException() {
        // Arrange
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> taskService.deleteTask(999L));
    }

    @Test
    void deleteTask_UnauthorizedUser_ShouldThrowException() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(2L);
        
        Task taskWithOtherUser = new Task();
        taskWithOtherUser.setId(1L);
        taskWithOtherUser.setTitle("Task");
        taskWithOtherUser.setUser(otherUser);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(taskWithOtherUser));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> taskService.deleteTask(1L));
    }
}
