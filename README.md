# Remote Team Task Manager

A full-stack task management application with JWT authentication, built with Spring Boot (backend) and React (frontend).

## Project Overview

This is a production-ready full-stack application following clean architecture principles:

- **Backend**: Spring Boot 3.2.x with Java 17+
- **Frontend**: React 18 with Vite
- **Database**: H2 (in-memory for development)
- **Authentication**: JWT (JSON Web Tokens)
- **API Documentation**: Swagger UI

## Project Structure

```
ReactSpringBoot/
├── backend/                 # Spring Boot API
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/taskmanager/
│   │       │   ├── controller/    # REST endpoints
│   │       │   ├── service/        # Business logic
│   │       │   ├── repository/    # Data access
│   │       │   ├── model/         # JPA entities
│   │       │   ├── dto/           # Data transfer objects
│   │       │   ├── security/      # JWT & auth
│   │       │   ├── config/        # Configuration
│   │       │   └── exception/     # Error handling
│   │       └── resources/
│   │           └── application.yml
│   ├── pom.xml
│   └── README.md
│
├── frontend/                # React + Vite
│   ├── src/
│   │   ├── components/      # Reusable UI components
│   │   ├── pages/           # Page components
│   │   ├── services/        # API calls
│   │   ├── context/         # React context
│   │   └── hooks/           # Custom hooks
│   ├── package.json
│   └── vite.config.js
│
└── README.md               # This file
```

## Quick Start

### Prerequisites

- Java 17+
- Node.js 18+
- Maven 3.8+

### Running the Backend

```bash
cd backend
mvn spring-boot:run
```

The backend will start at http://localhost:8080

- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console

### Running the Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend will start at http://localhost:5173

## Features

### Authentication
- User registration with email/password
- Secure login with JWT tokens
- Token stored in localStorage
- Automatic token attachment to requests
- Protected routes

### Task Management
- Create new tasks
- View all tasks
- Filter tasks by status (TODO, IN_PROGRESS, DONE)
- Update task details
- Delete tasks
- Real-time status updates

### User Interface
- Clean, modern design
- Responsive layout (works on mobile)
- Loading states
- Error handling with user feedback
- Form validation

## API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /auth/register | Register new user |
| POST | /auth/login | Login and get token |

### Tasks (Protected)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /tasks | Get all tasks |
| GET | /tasks?status=STATUS | Get tasks by status |
| POST | /tasks | Create new task |
| PUT | /tasks/{id} | Update task |
| DELETE | /tasks/{id} | Delete task |

## Tech Stack Details

### Backend
- Spring Boot 3.2.x
- Spring Security
- Spring Data JPA
- H2 Database
- JWT (jjwt 0.12.x)
- Lombok
- SpringDoc OpenAPI

### Frontend
- React 18
- Vite
- React Router 6
- Axios
- Context API for state management

## Environment

### Backend Configuration (application.yml)
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:mem:taskmanager
  jpa:
    hibernate:
      ddl-auto: update

app:
  jwt:
    secret: mySecretKeyForJWTTokenGenerationAndValidation123456
    expiration: 86400000
```

### Frontend Configuration
The frontend proxies API requests from port 5173 to port 8080.

## Security

- Passwords are hashed using BCrypt
- JWT tokens expire after 24 hours
- Stateless authentication
- CORS enabled for development

## Future Improvements

### Backend
- Add PostgreSQL/MySQL for production
- Add refresh tokens
- Implement role-based access control
- Add email verification
- Add password reset functionality
- Add unit and integration tests

### Frontend
- Add TypeScript
- Add state management (Redux/Zustand)
- Add unit tests
- Improve error handling
- Add pagination
- Add dark mode
- Add task categories

## Architecture

This project follows **Clean Architecture** principles:

1. **Controller Layer**: Handles HTTP requests and responses
2. **Service Layer**: Contains business logic
3. **Repository Layer**: Handles data persistence
4. **Model/DTO Layer**: Data representation

The frontend follows component-based architecture with:
- Context for global state (auth)
- Custom hooks for reusable logic
- Services for API communication
- Pages for routing
