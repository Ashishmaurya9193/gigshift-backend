# GigShift – Shift Service

This service handles all **shift-related** operations for the GigShift platform:

- Shift Catalog – employers create/list/update/cancel shifts.[file:1]
- Shift Selection – workers apply to open shifts.
- Assignment – employers confirm one worker for a shift and mark it ASSIGNED.[file:1]

It is a Spring Boot (Maven) application using **MySQL** as its database.

---

## 1. Prerequisites

- Java: **JDK 17+**
- Maven: **3.8+**
- MySQL running locally
- (Optional) Postman or curl for API testing

---

## 2. Database Setup

### 2.1 Create database and user

In MySQL:

```sql
CREATE DATABASE shift_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER 'shift_user'@'%' IDENTIFIED BY 'yourStrongPassword';
GRANT ALL PRIVILEGES ON shift_service.* TO 'shift_user'@'%';
FLUSH PRIVILEGES;


# ===============================
# Server
# ===============================
server.port=8081

# ===============================
# Datasource (MySQL)
# ===============================
spring.datasource.url=jdbc:mysql://localhost:3306/shift_service?useSSL=false&serverTimezone=UTC
spring.datasource.username=shift_user
spring.datasource.password=yourStrongPassword

# ===============================
# JPA / Hibernate
# ===============================
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true


shift
├─ ShiftServiceApplication.java      # main Spring Boot class

├─ catalog                           # Shift Catalog (CRUD)
│  ├─ controller
│  │  └─ ShiftCatalogController.java
│  ├─ service
│  │  └─ ShiftCatalogService.java
│  ├─ repository
│  │  └─ ShiftRepository.java
│  ├─ model
│  │  ├─ Shift.java
│  │  └─ ShiftStatus.java
│  ├─ dto
│  │  ├─ CreateShiftRequest.java
│  │  ├─ UpdateShiftRequest.java
│  │  └─ ShiftResponse.java
│  └─ event
│     └─ ShiftEventPublisher.java

├─ selection                        # Workers apply to shifts
│  ├─ controller
│  │  └─ ShiftSelectionController.java
│  ├─ service
│  │  └─ ShiftSelectionService.java
│  ├─ repository
│  │  └─ ShiftSelectionRepository.java
│  └─ model
│     └─ ShiftSelection.java

├─ assignment                       # Employers confirm workers
│  ├─ controller
│  │  └─ AssignmentController.java
│  ├─ service
│  │  └─ AssignmentService.java
│  ├─ repository
│  │  └─ AssignmentRepository.java
│  └─ model
│     ├─ Assignment.java
│     └─ AssignmentStatus.java

└─ security                         # simple header-based auth
   ├─ CustomUserPrincipal.java
   └─ SecurityConfig.java


5. Security (Dev Mode)
For now, this service uses header-based authentication:

X-User-Id – user id (e.g. worker-1, employer-1)

X-User-Role – WORKER or EMPLOYER

SecurityConfig registers a filter that:

Reads these headers on each request.

Builds a CustomUserPrincipal(userId, role).

Stores it in the Spring SecurityContext.

Controllers access the current user via:

java
@AuthenticationPrincipal CustomUserPrincipal principal
In Postman, you must send X-User-Id and X-User-Role on all protected endpoints.

Later you can replace this with JWT validation from the User service without changing controllers.

6. Running the Service
From the project root:

bash
mvn clean install
mvn spring-boot:run
Or in your IDE, run ShiftServiceApplication.

Service URL:

http://localhost:8081

7. API Usage (Postman Examples)
7.1 Employer – Create a Shift
Request

Method: POST

URL: http://localhost:8081/api/v1/shifts

Headers

X-User-Id: employer-1

X-User-Role: EMPLOYER

Content-Type: application/json

Body

json
{
  "title": "Evening Barista",
  "latitude": 49.2827,
  "longitude": -123.1207,
  "address": "123 Robson St, Vancouver, BC",
  "durationHours": 4,
  "requiredSkills": "BARISTA,CUSTOMERSERVICE",
  "payRate": 20.50,
  "startTime": "2026-02-10T17:00:00Z",
  "description": "Evening shift at downtown cafe."
}
Example response:

json
{
  "shiftId": "b9e0f9fa-9b0e-4f37-8f41-123456789abc",
  "employerId": "employer-1",
  "title": "Evening Barista",
  "latitude": 49.2827,
  "longitude": -123.1207,
  "address": "123 Robson St, Vancouver, BC",
  "durationHours": 4,
  "requiredSkills": "BARISTA,CUSTOMERSERVICE",
  "payRate": 20.5,
  "startTime": "2026-02-10T17:00:00Z",
  "status": "OPEN",
  "description": "Evening shift at downtown cafe.",
  "createdAt": "2026-02-05T09:00:00Z",
  "updatedAt": "2026-02-05T09:00:00Z"
}
7.2 Employer – List Shifts
Request

Method: GET

URL: http://localhost:8081/api/v1/shifts?page=0&size=20

Headers

X-User-Id: employer-1

X-User-Role: EMPLOYER

7.3 Worker – Select (Apply to) a Shift
Request

Method: POST

URL: http://localhost:8081/api/v1/shifts/{shiftId}/select
Replace {shiftId} with the shiftId from 7.1.

Headers

X-User-Id: worker-1

X-User-Role: WORKER

Body

None

Example response:

json
{
  "id": 1,
  "shiftId": "b9e0f9fa-9b0e-4f37-8f41-123456789abc",
  "workerId": "worker-1",
  "createdAt": "2026-02-05T09:10:00Z"
}
7.4 Worker – View Their Selections
Request

Method: GET

URL: http://localhost:8081/api/v1/my-selections

Headers

X-User-Id: worker-1

X-User-Role: WORKER

Example response:

json
[
  {
    "id": 1,
    "shiftId": "b9e0f9fa-9b0e-4f37-8f41-123456789abc",
    "workerId": "worker-1",
    "createdAt": "2026-02-05T09:10:00Z"
  }
]
7.5 Employer – View All Selections for a Shift
Request

Method: GET

URL: http://localhost:8081/api/v1/shifts/{shiftId}/selections

Headers

X-User-Id: employer-1

X-User-Role: EMPLOYER

Example response:

json
[
  {
    "id": 1,
    "shiftId": "b9e0f9fa-9b0e-4f37-8f41-123456789abc",
    "workerId": "worker-1",
    "createdAt": "2026-02-05T09:10:00Z"
  }
]
7.6 Employer – Confirm One Worker (Create Assignment)
Request

Method: POST

URL: http://localhost:8081/api/v1/shifts/{shiftId}/confirm

Headers

X-User-Id: employer-1

X-User-Role: EMPLOYER

Content-Type: application/json

Body

json
{
  "workerId": "worker-1"
}
Example response:

json
{
  "assignmentId": "f1234567-89ab-4cde-f012-3456789abcde",
  "shiftId": "b9e0f9fa-9b0e-4f37-8f41-123456789abc",
  "workerId": "worker-1",
  "employerId": "employer-1",
  "status": "CONFIRMED",
  "assignedAt": "2026-02-05T09:15:00Z"
}
At this point:

The shift’s status is ASSIGNED.

There is an assignments row for this shift.[file:1]

7.7 Get Assignment by ID
Request

Method: GET

URL: http://localhost:8081/api/v1/assignments/{assignmentId}

Headers

X-User-Id: employer-1

X-User-Role: EMPLOYER

Response: same AssignmentResponse as above.

8. Typical Local Test Flow
Start MySQL and ensure shift_service exists.

Run ShiftServiceApplication (port 8081).

In Postman:

Create a shift as employer.

Apply to that shift as worker.

View selections as worker.

View selections as employer.

Confirm worker as employer.

Optionally fetch assignment.

This gives you an end‑to‑end flow:

Employer posts shift → Worker applies → Employer picks worker → Shift becomes ASSIGNED.[file:1]

