# GigShift — Microservices Backend Platform

A Spring Boot microservices backend for a gig economy shift management platform. GigShift connects employers posting hourly shifts with workers seeking flexible work. Built as a capstone project for the Master of Science in Computer Science at Scaler Neovarsity (Woolf).

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Services](#services)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
- [Key Design Decisions](#key-design-decisions)
- [Known Limitations](#known-limitations)
- [Author](#author)

---

## Overview

GigShift handles the full lifecycle of a gig shift — from creation and discovery, through worker assignment, check-in/check-out, payment simulation, and immutable audit logging.

The core engineering challenge is concurrency: multiple workers can attempt to claim the same shift simultaneously. GigShift solves this using Redis-based distributed locking (SETNX) to guarantee zero double-booking without blocking database threads.

---

## Architecture

```
Internet → API Gateway (planned) → ECS Services → Data Layer

Services:
├── user-service        (port 8081)  — Auth, JWT, profiles
├── shift-service       (port 8080)  — Shifts, cart, assignments, search
├── notification-service(port 8090)  — Email/SMS via Kafka events
├── payment-service     (port 8084)  — Earnings simulation
└── audit-service       (port 8086)  — Immutable logs in MongoDB

Data Stores:
├── MySQL (RDS)         — Core transactional data
├── MongoDB (Atlas)     — Audit logs
├── Redis (ElastiCache) — Cart state + distributed locks
└── Elasticsearch       — Full-text shift search
```

---

## Services

### user-service
Handles registration, login, JWT token issuance, OAuth2 (Google), and profile management. Roles: `WORKER`, `EMPLOYER`, `ADMIN`.

### shift-service
The largest service — contains multiple co-located domains:
- **Catalog** — shift creation and lifecycle (OPEN → ASSIGNED → INPROGRESS → COMPLETED)
- **Selection** — workers express interest; employers view applicants
- **Assignment** — employer confirms worker; check-in/check-out/approve flow
- **Cart** — Redis-backed temporary shift selection with TTL and distributed locking
- **Search** — Elasticsearch-powered full-text and skill-based shift discovery

### notification-service
Consumes Kafka events (`shift.events`, `assignment.events`) and dispatches email/SMS notifications with retry logic and exponential backoff.

### payment-service
Simulates earnings calculation (`pay_rate × actual_duration`) and payout processing. No real payment gateway integrated — placeholder for Stripe/PayPal.

### audit-service
Stores immutable event logs in MongoDB. Every sensitive action (login, shift claim, payout) is recorded with actor, resource, before/after state, and timestamp.

---

## Tech Stack

| Technology | Version | Role |
|---|---|---|
| Spring Boot | 3.3.0 | Core framework for all services |
| MySQL | 8.0 | Transactional data — users, shifts, assignments |
| MongoDB | 7.x | Audit logs (append-only, schema-flexible) |
| Redis | 7.x | Distributed locking + cart TTL storage |
| Apache Kafka | — | Async event bus between services |
| Elasticsearch | 8.x | Full-text shift search with skill filtering |
| Docker | — | Containerization per service |
| AWS ECS Fargate | — | Container orchestration (prod deployment) |
| GitHub Actions | — | CI/CD pipeline |

---

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker Desktop (for local dependencies)

### 1. Start local dependencies
```bash
docker-compose up -d
```
This starts MySQL, MongoDB, Redis, Kafka, and Elasticsearch locally.

### 2. Build all services
```bash
./mvnw clean install -DskipTests
```

Expected output:
```
[INFO] gigshift-backend ........ SUCCESS [  0.188 s]
[INFO] user-service ............ SUCCESS [  4.771 s]
[INFO] shift-service ........... SUCCESS [  2.300 s]
[INFO] notification-service .... SUCCESS [  3.991 s]
[INFO] payment-service ......... SUCCESS [  1.282 s]
[INFO] audit-service ........... SUCCESS [  0.721 s]
[INFO] BUILD SUCCESS
```

### 3. Run services
Start each service in a separate terminal:
```bash
cd user-service && ./mvnw spring-boot:run
cd shift-service && ./mvnw spring-boot:run
cd notification-service && ./mvnw spring-boot:run
cd payment-service && ./mvnw spring-boot:run
cd audit-service && ./mvnw spring-boot:run
```

### 4. Test with Postman
Import the collection and hit:
```
POST http://localhost:8081/auth/register
POST http://localhost:8081/auth/login
POST http://localhost:8080/api/v1/shifts        (EMPLOYER token)
GET  http://localhost:8080/api/v1/shifts/search?q=barista
POST http://localhost:8080/api/v1/cart/shifts/{shiftId}
POST http://localhost:8080/api/v1/cart/checkout
```

---

## API Endpoints

### user-service (port 8081)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | /auth/register | Public | Register new user |
| POST | /auth/login | Public | Login, returns JWT |
| POST | /auth/forgot-password | Public | Request password reset |
| POST | /auth/reset-password | Public | Submit new password |
| GET | /users | ADMIN | List all users |
| GET | /users/{id} | Any | Get user by ID |

### shift-service (port 8080)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | /api/v1/shifts | EMPLOYER | Create shift |
| GET | /api/v1/shifts | Any | List open shifts |
| GET | /api/v1/shifts/search | Any | Search shifts |
| POST | /api/v1/shifts/{id}/select | WORKER | Express interest |
| GET | /api/v1/shifts/{id}/selections | EMPLOYER | View applicants |
| POST | /api/v1/shifts/{id}/confirm | EMPLOYER | Confirm worker |
| POST | /api/v1/cart/shifts/{id} | WORKER | Add to cart |
| GET | /api/v1/cart | WORKER | View cart |
| POST | /api/v1/cart/checkout | WORKER | Checkout cart |
| POST | /api/v1/{id}/check-in | WORKER | Worker check-in |
| POST | /api/v1/{id}/check-out | WORKER | Worker check-out |
| POST | /api/v1/assignments/{id}/approve | EMPLOYER | Approve completed work |

---

## Key Design Decisions

### Redis Distributed Locking
Database-level `SELECT FOR UPDATE` on the shifts table holds a lock for the entire transaction duration — including Kafka publish. At concurrent load this creates a queue. Redis `SETNX` completes in sub-millisecond time and releases immediately, allowing 100 concurrent workers to claim 100 different shifts in parallel with zero contention.

```
lock:shift:{shiftId} → {workerId}:{timestamp}  TTL: 60s
cart:{workerId}       → JSON list of cart items  TTL: 30min
```

### Header-Based Identity Propagation
`user-service` issues JWTs at login. Downstream services read `X-User-Id` and `X-User-Role` headers via `HeaderAuthFilter` (OncePerRequestFilter) and construct a `CustomUserPrincipal` injected via `@AuthenticationPrincipal`. In production, the API Gateway validates the JWT and populates these headers.

### Cart Co-located in shift-service
Cart and assignment were initially designed as separate services. The cross-service transaction problem (atomic lock + assignment creation) made this impractical at this scope. Co-locating cart inside shift-service as a separate package keeps the boundary clear while avoiding distributed transaction complexity.

### Polyglot Persistence
- **MySQL** — ACID transactions for business data where consistency is critical
- **MongoDB** — Audit logs where `changeData` structure varies per event type; relational schema would require constant migrations
- **Redis** — Cart state needs native TTL expiry; MySQL would need a cleanup job
- **Elasticsearch** — MySQL LIKE queries don't support fuzzy matching or skill filtering without complex SQL

---

## Known Limitations

| Area | Limitation |
|---|---|
| Testing | All tests skipped in build (`-DskipTests`). No integration test suite. |
| Security | shift-service uses header-based dev auth. No JWT re-validation in downstream services. |
| API Gateway | No gateway module exists. No centralized routing or rate limiting. |
| Payment | Fully simulated. No Stripe/PayPal integration. |
| Geolocation | lat/lon columns exist but geo-distance search not active in Elasticsearch. |
| Frontend | No frontend. APIs tested via Postman only. |
| Port Conflict | audit-service and payment-service both default to 8085 — change one before running locally. |

---

## Project Structure

```
gigshift-backend/
├── user-service/           # Auth, JWT, profiles
├── shift-service/          # Shifts, cart, assignment, search
│   └── src/main/java/com/gigshift/allocation/shift/
│       ├── catalog/        # ShiftCatalogController
│       ├── selection/      # ShiftSelectionController
│       ├── assignment/     # AssignmentController
│       ├── cart/           # CartController + CartRedisService
│       ├── search/         # ShiftSearchController
│       └── security/       # HeaderAuthFilter + CustomUserPrincipal
├── notification-service/   # Kafka consumer + email dispatch
├── payment-service/        # Earnings simulation
├── audit-service/          # MongoDB audit logs
└── pom.xml                 # Parent POM
```

---

## Author

**Ashish Maurya**
Master of Science in Computer Science
Scaler Neovarsity — Woolf, 2026
Email: Ashishmaurya9193@gmail.com
