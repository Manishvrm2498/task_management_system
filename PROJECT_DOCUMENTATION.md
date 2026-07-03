# Task Management System Documentation

## 1. Project Overview

Task Management System ek full-stack web application hai jisme users apne tasks create, update, delete aur track kar sakte hain. Admin users system ke sabhi tasks aur users ko manage kar sakte hain.

The project has three main parts:

```text
Frontend: React + Vite
Backend: Spring Boot REST API
Database: MySQL
```

Production deployment split:

```text
Frontend: Vercel
Backend: Render
Database: Aiven MySQL
```

Local Docker deployment:

```text
Frontend + Backend + MySQL: Docker Compose
```

## 2. Main Features

- User registration and login
- JWT-based authentication
- Role-based authorization
- Normal users can manage their own tasks
- Admin users can view and manage all tasks
- Admin users have a separate Users page
- Admin can view user details
- Admin can update user role
- Admin can enable or disable users
- Admin can delete users
- Swagger/OpenAPI documentation
- Docker support
- Render backend deployment support
- Vercel frontend deployment support

## 3. Project Structure

```text
task-management-system/
├── src/
│   ├── main/
│   │   ├── java/com/example/task_management_system/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── exception/
│   │   │   ├── repository/
│   │   │   ├── security/
│   │   │   ├── service/
│   │   │   └── util/
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── frontend/
│   ├── src/
│   │   ├── api.js
│   │   ├── main.jsx
│   │   └── styles.css
│   ├── package.json
│   ├── vite.config.js
│   └── vercel.json
├── Dockerfile
├── docker-compose.yml
├── .env.example
└── pom.xml
```

## 4. Backend Architecture

Backend Spring Boot REST API hai. It follows a layered structure:

```text
Controller -> Service -> Repository -> Database
```

### Controllers

Controllers HTTP requests receive karte hain aur response return karte hain.

Important controllers:

```text
AuthController
TaskController
AdminController
```

### Services

Services business logic handle karte hain.

Important services:

```text
AuthService
TaskService
AdminUserService
CustomUserDetailsService
```

### Repositories

Repositories database operations ke liye Spring Data JPA use karte hain.

```text
UserRepository
TaskRepository
```

### Entities

Entities database tables represent karte hain.

```text
UserEntity
Task
```

### DTOs

DTOs request/response body ke liye use hote hain.

```text
RegisterRequest
LoginRequest
LoginResponse
TaskRequest
TaskResponse
UserResponse
UpdateUserRequest
MessageResponse
ApiErrorResponse
```

## 5. Backend Dependencies

Backend Maven project hai.

Main dependencies:

```text
Spring Boot
Spring Web MVC
Spring Security
Spring Data JPA
Spring Validation
MySQL Connector/J
JJWT
Lombok
Springdoc OpenAPI
```

Java version:

```text
Java 21
```

Build tool:

```text
Maven Wrapper: ./mvnw
```

## 6. Backend Configuration

Backend configuration file:

```text
src/main/resources/application.properties
```

Important properties:

```properties
server.port=8080
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=update
jwt.secret=${JWT_KEY}
```

Required backend environment variables:

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
JWT_KEY
APP_CORS_ALLOWED_ORIGINS
```

Example:

```text
SPRING_DATASOURCE_URL=jdbc:mysql://mysql-host:3306/defaultdb?sslMode=REQUIRED&serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=avnadmin
SPRING_DATASOURCE_PASSWORD=your-db-password
JWT_KEY=mysupersecretkeymysupersecretkey1234567890
APP_CORS_ALLOWED_ORIGINS=https://your-frontend.vercel.app
```

JWT key should be at least 32 characters.

## 7. Backend API Endpoints

Base URL:

```text
http://localhost:8080/api/v1
```

Production backend:

```text
https://task-management-system-crkk.onrender.com/api/v1
```

### Auth APIs

```http
POST /api/v1/auth/register
POST /api/v1/auth/login
```

### Task APIs

Authentication required:

```http
GET /api/v1/tasks
POST /api/v1/tasks
GET /api/v1/tasks/{id}
PUT /api/v1/tasks/{id}
DELETE /api/v1/tasks/{id}
```

### Admin APIs

Admin role required:

```http
GET /api/v1/admin/tasks
GET /api/v1/admin/tasks/{id}
DELETE /api/v1/admin/tasks/{id}
GET /api/v1/admin/users
GET /api/v1/admin/users/{id}
PUT /api/v1/admin/users/{id}
DELETE /api/v1/admin/users/{id}
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
https://task-management-system-crkk.onrender.com/swagger-ui.html
```

## 8. Authentication Flow

1. User registers using email and password.
2. User logs in.
3. Backend validates credentials.
4. Backend returns JWT token.
5. Frontend stores token in localStorage.
6. Frontend sends token in Authorization header.

Header format:

```text
Authorization: Bearer <token>
```

## 9. Roles and Permissions

### USER

- Can register and login
- Can create tasks
- Can view own tasks
- Can update own tasks
- Can delete own tasks

### ADMIN

- Can view all tasks
- Can manage all tasks
- Can view all users
- Can update user roles
- Can enable/disable users
- Can delete users

## 10. Frontend Architecture

Frontend React + Vite application hai.

Main frontend files:

```text
frontend/src/main.jsx
frontend/src/api.js
frontend/src/styles.css
```

### main.jsx

Contains:

- Login UI
- Register UI
- User session state
- Task dashboard
- Admin task view
- Separate admin users view
- Theme toggle
- API calls through api.js

### api.js

API helper file hai. It sends requests to backend.

Production backend URL:

```js
const API_BASE = "https://task-management-system-crkk.onrender.com/api/v1";
```

Recommended flexible version:

```js
const API_BASE = import.meta.env.VITE_API_BASE || "https://task-management-system-crkk.onrender.com/api/v1";
```

### styles.css

Contains:

- Login page styling
- Dashboard layout
- Task cards
- Admin users page
- Light/dark theme
- Responsive layout

## 11. Frontend Dependencies

Frontend dependencies:

```text
React
React DOM
Vite
@vitejs/plugin-react
lucide-react
```

Frontend scripts:

```bash
npm run dev
npm run build
npm run preview
```

## 12. Local Setup Without Docker

### Requirements

Install:

```text
Java 21
Node.js 20 or newer
MySQL
Git
```

### Start MySQL

Create database:

```sql
CREATE DATABASE task_db;
```

Set environment variables:

```bash
export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/task_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC"
export SPRING_DATASOURCE_USERNAME="root"
export SPRING_DATASOURCE_PASSWORD="your-password"
export JWT_KEY="mysupersecretkeymysupersecretkey1234567890"
export APP_CORS_ALLOWED_ORIGINS="http://localhost:5173"
```

### Run Backend

From project root:

```bash
./mvnw spring-boot:run
```

Backend runs on:

```text
http://localhost:8080
```

### Run Frontend

From frontend folder:

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on:

```text
http://localhost:5173
```

## 13. Local Setup With Docker Compose

Docker Compose runs:

```text
MySQL container
Backend container
Frontend container
```

Create `.env`:

```bash
cp .env.example .env
```

Start:

```bash
docker compose up --build
```

Run in background:

```bash
docker compose up --build -d
```

Stop:

```bash
docker compose down
```

Stop and remove database volume:

```bash
docker compose down -v
```

Local Docker URLs:

```text
Frontend: http://localhost
Backend: http://localhost:8080
Swagger: http://localhost:8080/swagger-ui.html
MySQL: localhost:3307
```

## 14. Docker Files

### Backend Dockerfile

Backend Dockerfile:

```text
Dockerfile
```

It:

1. Uses Maven + Java 21 image to build the jar.
2. Runs `./mvnw -B -DskipTests package`.
3. Copies jar into Java 21 runtime image.
4. Starts app using `java -jar app.jar`.

### Frontend Dockerfile

Frontend Dockerfile:

```text
frontend/Dockerfile
```

It:

1. Uses Node image to build Vite app.
2. Runs `npm ci`.
3. Runs `npm run build`.
4. Uses Nginx to serve `dist`.

## 15. Production Deployment Plan

Recommended production split:

```text
Database: Aiven MySQL
Backend: Render
Frontend: Vercel
```

Why this split:

- Vercel is good for static React frontend.
- Render can run Docker backend easily.
- Aiven provides managed MySQL.

## 16. Aiven MySQL Setup

1. Create Aiven account.
2. Create MySQL service.
3. Copy host, port, database name, username, password.
4. Use those values in Render backend env variables.

Aiven connection values example:

```text
Host: mysql-xxxxx.aivencloud.com
Port: 15199
Database: defaultdb
Username: avnadmin
Password: your-password
```

Correct JDBC URL:

```text
jdbc:mysql://mysql-xxxxx.aivencloud.com:15199/defaultdb?sslMode=REQUIRED&serverTimezone=UTC
```

Wrong URL:

```text
mysql://avnadmin:password@host:15199/defaultdb?ssl-mode=REQUIRED
```

Spring Boot needs `jdbc:mysql://`, not `mysql://`.

## 17. Render Backend Deployment

### Render Service Type

Use:

```text
Web Service
```

Deploy from GitHub repo:

```text
https://github.com/Manishvrm2498/task_management_system
```

### Render Docker Settings

Use Docker deployment.

```text
Root Directory: blank
Dockerfile Path: Dockerfile
Docker Build Context Directory: .
```

Do not use Mac local paths like:

```text
/Users/manishverma/Downloads/task-management-system
```

Render runs on Linux and cannot access your local machine path.

### Render Environment Variables

Set these in Render:

```text
SPRING_DATASOURCE_URL=jdbc:mysql://mysql-host:15199/defaultdb?sslMode=REQUIRED&serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=avnadmin
SPRING_DATASOURCE_PASSWORD=your-db-password
JWT_KEY=mysupersecretkeymysupersecretkey1234567890
APP_CORS_ALLOWED_ORIGINS=https://your-frontend.vercel.app
```

### Render Deploy Steps

1. Push latest code to GitHub.
2. Go to Render service.
3. Add environment variables.
4. Click Manual Deploy.
5. Select Deploy latest commit.

If env vars changed:

```text
Manual Deploy -> Clear build cache & deploy
```

## 18. Vercel Frontend Deployment

### Vercel Project Settings

Use:

```text
Framework Preset: Vite
Root Directory: frontend
Build Command: npm run build
Output Directory: dist
Install Command: npm install
```

Root Directory must be:

```text
frontend
```

Do not use:

```text
frontend/src
```

Because `package.json` is inside `frontend`, not `frontend/src`.

### Vercel Environment Variable

Recommended env var:

```text
VITE_API_BASE=https://task-management-system-crkk.onrender.com/api/v1
```

Environment variable key should be:

```text
VITE_API_BASE
```

Do not write:

```text
const API_BASE
```

That is JavaScript code, not an environment variable name.

### vercel.json

The frontend includes:

```text
frontend/vercel.json
```

It defines:

```json
{
  "buildCommand": "npm run build",
  "installCommand": "npm install",
  "outputDirectory": "dist",
  "framework": "vite"
}
```

## 19. CORS Setup

Because frontend and backend are on different domains, backend must allow frontend origin.

Example:

```text
Frontend: https://your-app.vercel.app
Backend: https://task-management-system-crkk.onrender.com
```

Render backend env:

```text
APP_CORS_ALLOWED_ORIGINS=https://your-app.vercel.app
```

No trailing slash.

Correct:

```text
https://your-app.vercel.app
```

Wrong:

```text
https://your-app.vercel.app/
```

## 20. Git Workflow

After changes:

```bash
git status
git add .
git commit -m "Your message"
git push origin main
```

Render and Vercel deploy only GitHub commits. If a file exists only locally and is not pushed, deployment will fail.

Common example:

```text
Dockerfile exists locally but not on GitHub -> Render cannot find Dockerfile.
```

## 21. Testing

### Backend Build

```bash
./mvnw -DskipTests package
```

### Backend Tests

```bash
./mvnw test
```

### Frontend Build

```bash
cd frontend
npm run build
```

### Test Backend

Open:

```text
https://task-management-system-crkk.onrender.com/swagger-ui.html
```

### Test Frontend

Open Vercel frontend URL.

Try:

1. Register user.
2. Login.
3. Create task.
4. Update task.
5. Delete task.
6. Login as admin.
7. Open Users page.
8. Update user role/status.

## 22. Common Deployment Errors

### Render cannot find Dockerfile

Error:

```text
failed to read dockerfile: open Dockerfile: no such file or directory
```

Fix:

```text
Dockerfile Path: Dockerfile
Docker Build Context Directory: .
```

Also push Dockerfile to GitHub.

### Invalid Dockerfile Path

Do not put:

```text
.
```

in Dockerfile Path.

Use:

```text
Dockerfile
```

### Missing Java classes on Render

Error:

```text
cannot find symbol ApiErrorResponse
```

Fix:

```bash
git add src/main/java/...
git commit -m "Add missing backend files"
git push origin main
```

Render builds GitHub, not local files.

### JWT Bean Creation Error

Cause:

```text
JWT_KEY missing or too short
```

Fix:

```text
JWT_KEY=mysupersecretkeymysupersecretkey1234567890
```

### Hibernate cannot determine dialect

Cause:

```text
Database URL missing or invalid
```

Fix:

```text
SPRING_DATASOURCE_URL=jdbc:mysql://host:port/db?sslMode=REQUIRED&serverTimezone=UTC
```

### MySQL driver does not accept jdbcUrl

Wrong:

```text
mysql://user:password@host:port/db
```

Correct:

```text
jdbc:mysql://host:port/db?sslMode=REQUIRED&serverTimezone=UTC
```

### Vercel no dist directory

Error:

```text
No Output Directory named "dist" found
```

Fix:

```text
Root Directory: frontend
Build Command: npm run build
Output Directory: dist
```

### Vercel invalid env variable

Wrong key:

```text
const API_BASE
```

Correct key:

```text
VITE_API_BASE
```

### Backend is not reachable

Possible causes:

- Backend URL is wrong
- Render backend is sleeping
- CORS is not configured
- Backend crashed
- Database connection failed

Check:

```text
https://task-management-system-crkk.onrender.com/swagger-ui.html
```

If Swagger does not open, backend is not running.

## 23. Admin User Details Page

Admin has separate navigation:

```text
Tasks | Users
```

### Tasks Page

Shows:

- Total tasks
- Pending tasks
- In-progress tasks
- Completed tasks
- Create task
- Find task
- All task cards

### Users Page

Shows:

- Total users
- Admin count
- Enabled users
- Disabled users
- Search user
- Filter by role
- User ID
- Name
- Email
- Role
- Status
- Created date
- Update role
- Enable/disable user
- Delete user

## 24. Recommended Final Deployment Checklist

### Database

- Aiven MySQL running
- Host copied
- Port copied
- Username copied
- Password copied
- Database name copied

### Backend Render

- GitHub repo connected
- Dockerfile path is `Dockerfile`
- Build context is `.`
- Env vars added
- Latest commit deployed
- Swagger opens

### Frontend Vercel

- Root directory is `frontend`
- Build command is `npm run build`
- Output directory is `dist`
- Backend API URL configured
- Latest commit deployed
- Login/register tested

### GitHub

- All files committed
- All files pushed
- Render and Vercel using latest commit

## 25. Useful Commands

Backend:

```bash
./mvnw spring-boot:run
./mvnw test
./mvnw -DskipTests package
```

Frontend:

```bash
cd frontend
npm install
npm run dev
npm run build
```

Docker:

```bash
docker compose up --build
docker compose up --build -d
docker compose down
docker compose down -v
```

Git:

```bash
git status
git add .
git commit -m "Update project"
git push origin main
```
