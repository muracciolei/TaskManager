package com.example.taskmanager.controller;

import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for TaskController.
 */
@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
    "app.jwt.secret=thisIsAVeryLongSecretKeyForTestingPurposes1234567890ABCDEFGHIJKLMNOP",
    "app.jwt.expiration=86400"
})
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @Test
    @WithMockUser(username = "testuser@test.com")
    void getTasks_WithoutStatusFilter_ShouldReturnAllTasks() throws Exception {
        // Arrange
        TaskResponse taskResponse = createTaskResponse(1L, "Test Task", Task.Status.TODO);
        List<TaskResponse> tasks = Arrays.asList(taskResponse);
        
        when(taskService.getAllTasks()).thenReturn(tasks);

        // Act & Assert
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Task"))
                .andExpect(jsonPath("$[0].status").value("TODO"));
    }

    @Test
    @WithMockUser(username = "testuser@test.com")
    void getTasks_WithStatusFilter_ShouldReturnFilteredTasks() throws Exception {
        // Arrange
        TaskResponse taskResponse = createTaskResponse(1L, "In Progress Task", Task.Status.IN_PROGRESS);
        List<TaskResponse> tasks = Arrays.asList(taskResponse);
        
        when(taskService.getTasksByStatus(Task.Status.IN_PROGRESS)).thenReturn(tasks);

        // Act & Assert
        mockMvc.perform(get("/tasks").param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"));
    }

    @Test
    @WithMockUser(username = "testuser@test.com")
    void createTask_WithValidRequest_ShouldReturnCreated() throws Exception {
        // Arrange
        TaskRequest request = new TaskRequest();
        request.setTitle("New Task");
        request.setDescription("New Description");
        request.setStatus(Task.Status.TODO);

        TaskResponse response = createTaskResponse(1L, "New Task", Task.Status.TODO);
        
        when(taskService.createTask(any(TaskRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/tasks")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("New Task"));
    }

    @Test
    @WithMockUser(username = "testuser@test.com")
    void createTask_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Arrange
        TaskRequest request = new TaskRequest();
        // Title is required but missing

        // Act & Assert
        mockMvc.perform(post("/tasks")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser@test.com")
    void updateTask_WithValidRequest_ShouldReturnUpdatedTask() throws Exception {
        // Arrange
        TaskRequest request = new TaskRequest();
        request.setTitle("Updated Task");
        request.setDescription("Updated Description");
        request.setStatus(Task.Status.DONE);

        TaskResponse response = createTaskResponse(1L, "Updated Task", Task.Status.DONE);
        
        when(taskService.updateTask(eq(1L), any(TaskRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/tasks/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    @WithMockUser(username = "testuser@test.com")
    void deleteTask_WithValidId_ShouldReturnNoContent() throws Exception {
        // Arrange
        doNothing().when(taskService).deleteTask(1L);

        // Act & Assert
        mockMvc.perform(delete("/tasks/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void getTasks_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createTask_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        TaskRequest request = new TaskRequest();
        request.setTitle("New Task");

        // Act & Assert
        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    private TaskResponse createTaskResponse(Long id, String title, Task.Status status) {
        return new TaskResponse(
                id,
                title,
                "Description",
                status,
                LocalDateTime.now(),
                1L,
                "Test User"
        );
    }
}
