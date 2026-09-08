# Employee Leave Management System — REST API

A Spring Boot REST API for managing employee leave requests, with role-based access control (Employee/Manager/Admin), JWT authentication, and full audit tracking.

## Tech Stack

- **Backend:** Spring Boot 4.1.0, Java 22
- **Database:** PostgreSQL
- **Auth:** Spring Security + JWT (jjwt 0.11.5)
- **ORM:** Spring Data JPA / Hibernate
- **Mapping:** ModelMapper
- **Docs:** springdoc-openapi (Swagger UI)
- **Build:** Maven

## Prerequisites

- Java 22
- PostgreSQL running locally
- Maven (or use the included wrapper)

## Setup

1. Create a PostgreSQL database:
   ```sql
   CREATE DATABASE leave_management_db;
   ```

2. Update `src/main/resources/application.properties` with your local database credentials:
   ```
   spring.datasource.url=jdbc:postgresql://localhost:5432/leave_management_db
   spring.datasource.username=postgres
   spring.datasource.password=<your password>
   ```

3. Build and run:
   ```
   mvn spring-boot:run
   ```

4. The API starts on `http://localhost:8080`. Swagger UI is available at `http://localhost:8080/swagger-ui/index.html`.

## Authentication

All endpoints except `/api/auth/**` require a JWT bearer token.

1. Register: `POST /api/auth/register`
2. Login: `POST /api/auth/login` → returns a token
3. Include the token on subsequent requests: `Authorization: Bearer <token>`

## Roles

- **EMPLOYEE** — apply for/view/edit/cancel their own leave requests
- **MANAGER** — approve/reject leave requests, view all employees and requests
- **ADMIN** — full access, manages departments/leave types/users/employees

## API Overview

| Domain | Base Path |
|---|---|
| Auth | `/api/auth` |
| Departments | `/api/departments` |
| Leave Types | `/api/leave-types` |
| Employees | `/api/employees` |
| Users | `/api/users` |
| Leave Requests | `/api/leave-requests` |

List endpoints support pagination and sorting: `?page=1&limit=10&sortDirection=asc&sort=name`

## Business Rules

- Leave cannot be applied for in the past, or with start date after end date
- Leave cannot exceed the employee's remaining balance
- Overlapping pending/approved requests for the same employee are blocked
- Only PENDING requests can be approved, rejected, edited, or cancelled
- Approving leave deducts the balance; cancelling an approved leave restores it
- Only MANAGER/ADMIN can approve or reject requests

## Testing

Run all tests:
```
mvn test
```

## Response Format

Errors follow a consistent shape:
```json
{
  "success": false,
  "message": "Insufficient leave balance",
  "data": null
}