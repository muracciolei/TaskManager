# Remote Team Task Manager - Backend

## Live Demo

- Swagger UI: https://your-backend-url/swagger-ui.html
- Frontend: https://your-frontend-url

A Spring Boot REST API for managing tasks with JWT authentication.

## Architecture

This project follows **Clean Architecture** principles with clear separation of concerns:

```
com.example.taskmanager
├── controller/     # REST API endpoints
├── service/        # Business logic
├── repository/    # Data access layer
├── model/         # JPA entities
├── dto/           # Data Transfer Objects
├── security/      # JWT authentication
├── config/        # Configuration classes
└── exception/     # Exception handling
```
## Design Decisions

- Used DTOs to avoid exposing internal entities.
- Implemented stateless JWT authentication.
- Applied BCrypt for password hashing.
- Separated business logic into services.
- Centralized exception handling.

### Layer Responsibilities:
- **Controller**: Handle HTTP requests/responses, delegate to services
- **Service**: Implement business logic, orchestrate operations
- **Repository**: Data access using JPA
- **DTO**: Separate internal entities from API contracts
- **Security**: JWT token generation and validation
- **Config**: Spring configuration (Security, CORS)
- **Exception**: Centralized error handling

## Technology Stack

- Java 17+
- Spring Boot 3.2.x
- Spring Security
- Spring Data JPA
- H2 Database (in-memory)
- JWT (jjwt)
- Lombok
- SpringDoc OpenAPI (Swagger)

## How to Run

1. **Prerequisites**: Java 17+ and Maven installed

2. **Run the application**:
   ```bash
   cd backend
   mvn spring-boot:run
   ```

3. **Access the application**:
   - API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - H2 Console: http://localhost:8080/h2-console
     - JDBC URL: jdbc:h2:mem:taskmanager
     - Username: sa
     - Password: (empty)

## Authentication

### How It Works

1. **Registration** (`POST /auth/register`):
   - User submits name, email, password
   - Password is hashed using BCrypt
   - User is saved to H2 database

2. **Login** (`POST /auth/login`):
   - User submits email, password
   - System validates credentials against hashed password
   - On success, generates JWT token with 24-hour expiration
   - Returns token in response

3. **Protected Endpoints**:
   - All `/tasks` endpoints require valid JWT token
   - Token must be sent in header: `Authorization: Bearer <token>`
   - JWT filter validates token and sets security context

### Security Configuration
- BCrypt password hashing (strength 10)
- JWT with HS256 algorithm
- Stateless session management
- CORS configured for development environment. In production, origins should be restricted.

## API Endpoints

### Authentication
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | /auth/register | Register new user | No |
| POST | /auth/login | Login and get token | No |

### Tasks
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | /tasks | Get all tasks | Yes |
| GET | /tasks?status=TODO | Get tasks by status | Yes |
| POST | /tasks | Create new task | Yes |
| PUT | /tasks/{id} | Update task | Yes |
| DELETE | /tasks/{id} | Delete task | Yes |

### Request/Response Examples

#### Register
```bash
POST /auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123"
}
```

#### Login
```bash
POST /auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "message": "Login successful"
}
```

#### Create Task
```bash
POST /tasks
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Implement feature",
  "description": "Add new functionality",
  "status": "TODO"
}
```

#### Get Tasks
```bash
GET /tasks
Authorization: Bearer <token>
```

Response:
```json
[
  {
    "id": 1,
    "title": "Implement feature",
    "description": "Add new functionality",
    "status": "TODO",
    "createdAt": "2024-01-15T10:30:00",
    "userId": 1,
    "userName": "John Doe"
  }
]
```

## Task Status Options
- `TODO` - Task needs to be started
- `IN_PROGRESS` - Task is being worked on
- `DONE` - Task is completed

## Error Responses

| Status | Error | Description |
|--------|-------|-------------|
| 400 | Bad Request | Invalid input data |
| 401 | Unauthorized | Invalid or missing token |
| 404 | Not Found | Resource not found |
| 409 | Conflict | User already exists |

Example error response:
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Task not found with id: 1"
}
```

## Possible Future Improvements

1. **Production Enhancements**:
   - Use PostgreSQL/MySQL instead of H2
   - Environment-based configuration
   - HTTPS enforcement
   - Rate limiting

2. **Security Improvements**:
   - Add refresh tokens
   - Implement role-based access control
   - Add email verification
   - Password reset functionality

3. **Features**:
   - Task categories/tags
   - Task assignment to team members
   - Due dates and reminders
   - Task comments
   - File attachments

4. **API Enhancements**:
   - Pagination for list endpoints
   - Sorting and filtering
   - API versioning

5. **Testing**:
   - Unit tests for services
   - Integration tests
   - Test coverage reports

## Project Structure

```
backend/
├── pom.xml
├── src/main/
│   ├── java/com/example/taskmanager/
│   │   ├── TaskManagerApplication.java
│   │   ├── config/
│   │   │   └── SecurityConfig.java
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   └── TaskController.java
│   │   ├── dto/
│   │   │   ├── AuthResponse.java
│   │   │   ├── LoginRequest.java
│   │   │   ├── RegisterRequest.java
│   │   │   ├── TaskRequest.java
│   │   │   ├── TaskResponse.java
│   │   │   └── UserResponse.java
│   │   ├── exception/
│   │   │   ├── ErrorResponse.java
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── ResourceNotFoundException.java
│   │   │   └── UserAlreadyExistsException.java
│   │   ├── model/
│   │   │   ├── Task.java
│   │   │   └── User.java
│   │   ├── repository/
│   │   │   ├── TaskRepository.java
│   │   │   └── UserRepository.java
│   │   ├── security/
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   └── JwtUtil.java
│   │   └── service/
│   │       ├── AuthService.java
│   │       └── TaskService.java
│   └── resources/
│       └── application.yml
└── README.md
```

---
