# Task Management System

A Spring Boot REST API for managing tasks with JWT authentication, role-based access, and MySQL persistence.

## Features

- User registration and login
- JWT-based authentication
- Role-based authorization for users and admins
- Create, read, update, and delete tasks
- Admin access to all users' tasks
- Request validation and centralized error responses
- Swagger/OpenAPI configuration

## Tech Stack

- Java 21
- Spring Boot 4.1.0
- Spring Web MVC
- Spring Security
- Spring Data JPA
- MySQL
- JWT (`jjwt`)
- Lombok
- Maven

## Project Structure

```text
src/main/java/com/example/task_management_system
├── controller
│   ├── AdminController.java
│   ├── AuthController.java
│   └── TaskController.java
├── dto
├── entity
├── exception
├── repository
├── security
├── service
└── util
```

## Prerequisites

- Java 21 or newer
- MySQL running locally
- Maven, or use the included Maven wrapper

## Database Configuration

The application uses this MySQL database by default:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/task_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=Password
spring.jpa.hibernate.ddl-auto=update
```

You can change these values in:

```text
src/main/resources/application.properties
```

## Run the Application

From the project root:

```bash
./mvnw spring-boot:run
```

The API runs on:

```text
http://localhost:8080
```

## Run the React Frontend

The React frontend is in:

```text
frontend/
```

Install dependencies once:

```bash
cd frontend
npm install
```

Start Spring Boot from the project root:

```bash
./mvnw spring-boot:run
```

Then start React:

```bash
cd frontend
npm run dev
```

Open:

```text
http://localhost:5173
```

During development, Vite proxies `/api/**` requests to the Spring Boot backend at `http://localhost:8080`.

## Run Tests

```bash
./mvnw test
```

## Authentication

Register and login endpoints are public. All task endpoints require a JWT token.

After login, send the token in the `Authorization` header:

```text
Authorization: Bearer <token>
```

## API Endpoints

### Auth

#### Register

```http
POST /api/v1/auth/register
```

Request body:

```json
{
  "firstName": "Unknown",
  "lastName": "Unknown",
  "email": "example@example.com",
  "password": "Password@123"
}
```

Newly registered users are created with the `USER` role.

#### Login

```http
POST /api/v1/auth/login
```

Request body:

```json
{
  "email": "example@example.com",
  "password": "Password@123"
}
```

Response:

```json
{
  "message": "Login successful!",
  "token": "jwt-token"
}
```

### Tasks

These endpoints require authentication.

#### Create Task

```http
POST /api/v1/tasks
```

Request body:

```json
{
  "title": "Learn Spring Boot",
  "description": "Practice building REST APIs with Spring Boot",
  "status": "PENDING"
}
```

#### Get Tasks

```http
GET /api/v1/tasks
```

- Normal users get only their own tasks.
- Admin users get all tasks.

#### Get Task by ID

```http
GET /api/v1/tasks/{id}
```

Normal users can access only their own task. Admin users can access any task.

#### Update Task

```http
PUT /api/v1/tasks/{id}
```

Request body:

```json
{
  "title": "Learn Spring Security",
  "description": "Practice JWT authentication and role-based access",
  "status": "IN_PROGRESS"
}
```

#### Delete Task

```http
DELETE /api/v1/tasks/{id}
```

### Admin

These endpoints require the `ADMIN` or `ROLE_ADMIN` role.

#### Get All Tasks

```http
GET /api/v1/admin/tasks
```

#### Get Any Task by ID

```http
GET /api/v1/admin/tasks/{id}
```

#### Delete Any Task

```http
DELETE /api/v1/admin/tasks/{id}
```

## Roles

Supported roles:

```text
USER
ADMIN
```

The application also accepts database values like `ROLE_ADMIN` and normalizes them internally.

To make a user an admin, update the `users` table:

```sql
UPDATE users
SET role = 'ADMIN'
WHERE email = 'admin@example.com';
```

Then log in again to get a fresh JWT token.

## Task Status Values

Use one of the enum values defined in `TaskStatus.java`.

Common values include:

```text
PENDING
IN_PROGRESS
COMPLETED
```

## Swagger

Swagger/OpenAPI configuration is included with JWT Bearer authentication support.

After starting the application, open:

```text
http://localhost:8080/swagger-ui/index.html
```

or:

```text
http://localhost:8080/swagger-ui.html
```

To test secured APIs from Swagger:

1. Login using `POST /api/v1/auth/login`.
2. Copy the returned JWT token.
3. Click the `Authorize` button in Swagger UI.
4. Paste only the token value. Swagger will send it as a Bearer token.
5. Try task or admin APIs directly from Swagger.

OpenAPI JSON is available at:

```text
http://localhost:8080/v3/api-docs
```

## Common Issues

### Admin cannot see all tasks

Check these first:

- The user role in MySQL must be `ADMIN` or `ROLE_ADMIN`.
- Restart the Spring Boot app after code changes.
- Log in again after changing the role.
- Use the new admin token in the `Authorization` header.
- Call `GET /api/v1/tasks` or `GET /api/v1/admin/tasks`.

### 401 Unauthorized

The token is missing, expired, or invalid. Log in again and pass:

```text
Authorization: Bearer <token>
```

### 403 Forbidden

The logged-in user does not have the required role.

## Build

```bash
./mvnw clean package
```

The generated JAR will be created under:

```text
target/
```
