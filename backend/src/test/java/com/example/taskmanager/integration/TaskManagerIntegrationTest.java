package com.example.taskmanager.integration;

import com.example.taskmanager.dto.AuthResponse;
import com.example.taskmanager.dto.LoginRequest;
import com.example.taskmanager.dto.RegisterRequest;
import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the Task Manager API.
 * Uses TestRestTemplate to test the full HTTP stack.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TaskManagerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;

    public TaskManagerIntegrationTest() {
        this.baseUrl = "http://localhost";
    }

    private String getBaseUrl() {
        return baseUrl + ":" + port;
    }

    // ==================== Authentication Tests ====================

    @Test
    void register_WithValidData_ShouldCreateUser() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setName("Integration Test User");
        request.setEmail("integration" + System.currentTimeMillis() + "@example.com");
        request.setPassword("password123");

        // Act
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                getBaseUrl() + "/auth/register",
                request,
                AuthResponse.class
        );

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getToken());
    }

    @Test
    void register_WithDuplicateEmail_ShouldReturnBadRequest() {
        // Arrange
        String email = "duplicate" + System.currentTimeMillis() + "@example.com";
        
        RegisterRequest request1 = new RegisterRequest();
        request1.setName("First User");
        request1.setEmail(email);
        request1.setPassword("password123");
        
        // Register first user
        restTemplate.postForEntity(getBaseUrl() + "/auth/register", request1, AuthResponse.class);

        // Try to register with same email
        RegisterRequest request2 = new RegisterRequest();
        request2.setName("Second User");
        request2.setEmail(email);
        request2.setPassword("password123");

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl() + "/auth/register",
                request2,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void login_WithValidCredentials_ShouldReturnToken() {
        // Arrange - First register a user
        String email = "login" + System.currentTimeMillis() + "@example.com";
        String password = "password123";
        
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("Test User");
        registerRequest.setEmail(email);
        registerRequest.setPassword(password);
        
        restTemplate.postForEntity(getBaseUrl() + "/auth/register", registerRequest, AuthResponse.class);

        // Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        // Act
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                getBaseUrl() + "/auth/login",
                loginRequest,
                AuthResponse.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getToken());
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnBadRequest() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("wrongpassword");

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl() + "/auth/login",
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ==================== Task Tests ====================

    @Test
    void createTask_WithoutAuthentication_ShouldReturnUnauthorized() {
        // Arrange
        TaskRequest request = new TaskRequest();
        request.setTitle("Test Task");
        request.setDescription("Test Description");
        request.setStatus(com.example.taskmanager.model.Task.Status.TODO);

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl() + "/tasks",
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void fullTaskLifecycle_WithAuthentication_ShouldWork() {
        // Step 1: Register a user
        String email = "tasklife" + System.currentTimeMillis() + "@example.com";
        String password = "password123";
        
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("Task User");
        registerRequest.setEmail(email);
        registerRequest.setPassword(password);
        
        ResponseEntity<AuthResponse> registerResponse = restTemplate.postForEntity(
                getBaseUrl() + "/auth/register",
                registerRequest,
                AuthResponse.class
        );
        
        String token = registerResponse.getBody().getToken();
        
        // Step 2: Create a task
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setTitle("Integration Test Task");
        taskRequest.setDescription("This is an integration test");
        taskRequest.setStatus(com.example.taskmanager.model.Task.Status.TODO);
        
        HttpEntity<TaskRequest> taskEntity = new HttpEntity<>(taskRequest, headers);
        
        ResponseEntity<TaskResponse> createResponse = restTemplate.postForEntity(
                getBaseUrl() + "/tasks",
                taskEntity,
                TaskResponse.class
        );
        
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        Long taskId = createResponse.getBody().getId();
        
        // Step 3: Get all tasks
        ResponseEntity<TaskResponse[]> getResponse = restTemplate.exchange(
                getBaseUrl() + "/tasks",
                org.springframework.http.HttpMethod.GET,
                taskEntity,
                TaskResponse[].class
        );
        
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertTrue(getResponse.getBody().length > 0);
        
        // Step 4: Update the task
        taskRequest.setTitle("Updated Task Title");
        taskRequest.setStatus(com.example.taskmanager.model.Task.Status.IN_PROGRESS);
        
        ResponseEntity<TaskResponse> updateResponse = restTemplate.exchange(
                getBaseUrl() + "/tasks/" + taskId,
                org.springframework.http.HttpMethod.PUT,
                taskEntity,
                TaskResponse.class
        );
        
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertEquals("Updated Task Title", updateResponse.getBody().getTitle());
        
        // Step 5: Delete the task
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                getBaseUrl() + "/tasks/" + taskId,
                org.springframework.http.HttpMethod.DELETE,
                null,
                Void.class
        );
        
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());
        
        // Step 6: Verify task is deleted
        ResponseEntity<TaskResponse[]> getAfterDelete = restTemplate.exchange(
                getBaseUrl() + "/tasks",
                org.springframework.http.HttpMethod.GET,
                taskEntity,
                TaskResponse[].class
        );
        
        assertEquals(HttpStatus.OK, getAfterDelete.getStatusCode());
        assertEquals(0, getAfterDelete.getBody().length);
    }

    @Test
    void getTasks_WithStatusFilter_ShouldReturnFilteredTasks() {
        // Register and login
        String email = "filter" + System.currentTimeMillis() + "@example.com";
        String password = "password123";
        
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("Filter User");
        registerRequest.setEmail(email);
        registerRequest.setPassword(password);
        
        ResponseEntity<AuthResponse> registerResponse = restTemplate.postForEntity(
                getBaseUrl() + "/auth/register",
                registerRequest,
                AuthResponse.class
        );
        
        String token = registerResponse.getBody().getToken();
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        
        // Create a TODO task
        TaskRequest todoTask = new TaskRequest();
        todoTask.setTitle("Todo Task");
        todoTask.setStatus(com.example.taskmanager.model.Task.Status.TODO);
        
        HttpEntity<TaskRequest> entity = new HttpEntity<>(todoTask, headers);
        restTemplate.postForEntity(getBaseUrl() + "/tasks", entity, TaskResponse.class);
        
        // Create a DONE task
        TaskRequest doneTask = new TaskRequest();
        doneTask.setTitle("Done Task");
        doneTask.setStatus(com.example.taskmanager.model.Task.Status.DONE);
        
        HttpEntity<TaskRequest> entity2 = new HttpEntity<>(doneTask, headers);
        restTemplate.postForEntity(getBaseUrl() + "/tasks", entity2, TaskResponse.class);
        
        // Filter by TODO status
        ResponseEntity<TaskResponse[]> filteredResponse = restTemplate.exchange(
                getBaseUrl() + "/tasks?status=TODO",
                org.springframework.http.HttpMethod.GET,
                entity,
                TaskResponse[].class
        );
        
        assertEquals(HttpStatus.OK, filteredResponse.getStatusCode());
        assertNotNull(filteredResponse.getBody());
        assertEquals(1, filteredResponse.getBody().length);
        assertEquals(com.example.taskmanager.model.Task.Status.TODO, filteredResponse.getBody()[0].getStatus());
    }
}
