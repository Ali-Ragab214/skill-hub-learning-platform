# SkillHub Learning Platform

> A RESTful backend for online education — layered architecture, JWT-based RBAC, Redis caching, and Dockerized deployment.

![Spring Boot 4.0](https://img.shields.io/badge/Spring_Boot-4.0.6-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Java 17](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-Auth-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-OpenAPI-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)
![Flyway](https://img.shields.io/badge/Flyway-Migrations-CC0200?style=for-the-badge&logo=flyway&logoColor=white)
![JUnit 5](https://img.shields.io/badge/JUnit_5-Tested-25A162?style=for-the-badge&logo=junit5&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)

---

## Table of Contents

1. [System Overview](#system-overview)
2. [Key Highlights](#key-highlights)
3. [Functional Requirements](#functional-requirements)
4. [Non-Functional Requirements](#non-functional-requirements)
5. [Use Cases / User Stories](#use-cases--user-stories)
6. [System Architecture](#system-architecture)
7. [Architecture Decisions](#architecture-decisions)
8. [Sequence Flows](#sequence-flows)
9. [Technology Stack](#technology-stack)
10. [Database Design](#database-design)
11. [API Design](#api-design)
12. [Security](#security)
13. [Performance Optimizations](#performance-optimizations)
14. [Deployment Architecture](#deployment-architecture)
15. [DevOps & CI/CD Readiness](#devops--cicd-readiness)
16. [Monitoring & Observability](#monitoring--observability)
17. [Scalability & Future Evolution](#scalability--future-evolution)
18. [Project Structure](#project-structure)
19. [Quick Start](#quick-start)
20. [Environment Variables](#environment-variables)
21. [Testing Strategy](#testing-strategy)
22. [License](#license)
23. [Author](#author)

---

## System Overview

### Business Context

Online education platforms require management of complex domain interactions: multi-role user hierarchies, hierarchical course content (courses → sections → lessons), enrollment lifecycles, progress tracking, peer reviews, and role-scoped access control. Most off-the-shelf LMS solutions are either rigid in their data models or costly to customize.

### Problem Statement

Building a platform that serves three distinct personas — **Students**, **Instructors**, and **Administrators** — requires:

- Distinct permission boundaries per role
- Cascading content hierarchies with ownership rules
- Enrollment-coupled features (progress tracking, reviews)
- Consistent API contracts consumable by any frontend client
- Deployment simplicity without sacrificing performance or security

### Solution

SkillHub provides a RESTful API that models the education domain as discrete bounded contexts. Each context (courses, enrollments, reviews, progress) is independently maintainable and testable. The system is packaged as a Dockerized monolith with Redis caching and PostgreSQL persistence — deployable with a single orchestration command.

---

## Key Highlights

- **Layered Architecture** — API, Application, Domain, and Infrastructure layers with inward-pointing dependencies. Domain entities serve dual purpose as JPA entities and business objects, a pragmatic compromise that avoids a separate mapping layer between the domain and persistence models.
- **Role-Based Access Control** — Three roles (Student, Instructor, Admin) enforced via Spring Security method-level `@PreAuthorize` annotations and JWT Bearer token authentication.
- **Redis-Backed Caching** — Read responses for courses and sections cached via `@Cacheable` with configurable TTL; cache eviction triggered on write operations.
- **40+ REST Endpoints** — Covering authentication, course/section/lesson CRUD, enrollment lifecycle, reviews and ratings, lesson progress tracking, and admin user management.
- **Containerized Deployment** — Docker Compose orchestrates three services: Spring Boot 4 application, PostgreSQL 16, and Redis 7.
- **Database Migrations** — Flyway-managed versioned SQL scripts for repeatable, environment-safe schema evolution.
- **Database Indexing** — B-tree indexes on foreign key columns for join performance; unique composite indexes on business keys for data integrity.

---

## Functional Requirements

### Authentication & Authorization

| ID | Requirement | Actors |
|----|-------------|--------|
| FR-01 | Users shall register with name, email, and password; passwords hashed via BCrypt before storage | Public |
| FR-02 | Registered users shall authenticate and receive a signed JWT Bearer token | Public |
| FR-03 | JWT tokens shall expire after a configurable duration (default 24 hours) | System |
| FR-04 | The system shall enforce three roles: `STUDENT`, `INSTRUCTOR`, `ADMIN` via `@PreAuthorize` | System |
| FR-05 | Duplicate email registration shall be rejected with HTTP 409 Conflict | System |
| FR-06 | Password strength shall be validated: minimum 8 characters, at least one uppercase, one lowercase, one digit, one special character | System |
| FR-07 | JWT tokens shall include `sub` (email), `iat`, and `exp` claims; signed with HMAC-SHA256 | System |

### User Management

| ID | Requirement | Actors |
|----|-------------|--------|
| FR-08 | Administrators shall list all registered users | Admin |
| FR-09 | Administrators shall delete any user account; deletion cascades to associated lesson progress records | Admin |

### Course Management

| ID | Requirement | Actors |
|----|-------------|--------|
| FR-10 | Instructors shall create courses with title, description, price, and difficulty level | Instructor, Admin |
| FR-11 | Instructors shall update their own courses | Instructor, Admin |
| FR-12 | Instructors shall delete their own courses; deletion cascades to sections, lessons, enrollments, reviews, and progress records | Instructor, Admin |
| FR-13 | Instructors shall publish draft courses via a dedicated PATCH endpoint | Instructor, Admin |
| FR-14 | Courses shall support a lifecycle with `DRAFT`, `PUBLISHED`, and `ARCHIVED` statuses | System |
| FR-15 | Unauthenticated users shall see only published courses | Public |
| FR-16 | Instructors shall view their own courses regardless of publish status | Instructor |
| FR-17 | Courses shall be filterable by level (`BEGINNER`, `INTERMEDIATE`, `ADVANCED`) | Public |
| FR-18 | Courses shall be filterable by status (instructors and admins only) | Instructor, Admin |
| FR-19 | Administrators shall view all courses across all instructors and statuses | Admin |
| FR-20 | Course title must be unique across all courses | System |

### Section Management

| ID | Requirement | Actors |
|----|-------------|--------|
| FR-21 | Instructors shall create sections within their courses with a title and order index | Instructor |
| FR-22 | Section titles must be unique within a course | System |
| FR-23 | Sections shall be ordered by `order_index` | System |
| FR-24 | Instructors shall update and delete sections within their courses | Instructor |
| FR-25 | Deleting a section shall cascade-delete its lessons | System |
| FR-26 | Sections shall be searchable by title within a course | Public |

### Lesson Management

| ID | Requirement | Actors |
|----|-------------|--------|
| FR-27 | Instructors shall create lessons with title, video URL, duration, preview flag, and order index | Instructor |
| FR-28 | Lesson titles must be unique within a section | System |
| FR-29 | Video URLs must start with `http://` or `https://` | System |
| FR-30 | Duration must be between 1 and 300 minutes | System |
| FR-31 | Instructors shall update and delete lessons within their sections | Instructor |
| FR-32 | The system shall expose a public preview endpoint returning only lessons where `is_preview = true` | Public |

### Enrollment Management

| ID | Requirement | Actors |
|----|-------------|--------|
| FR-33 | Students shall enroll in any published course | Student |
| FR-34 | The system shall prevent duplicate enrollments via a unique composite constraint on (student_id, course_id) | System |
| FR-35 | Students shall unenroll from courses; unenrollment cascades to associated lesson progress records | Student |
| FR-36 | Students shall view their paginated enrollment list | Student |
| FR-37 | Students shall check enrollment status for a specific course | Student |
| FR-38 | The system shall expose a public enrollment count endpoint per course | Public |
| FR-39 | Instructors cannot enroll in their own courses | System |

### Lesson Progress Tracking

| ID | Requirement | Actors |
|----|-------------|--------|
| FR-40 | Students shall mark a lesson as completed | Student |
| FR-41 | Students shall mark a completed lesson as incomplete | Student |
| FR-42 | The system shall record a completion timestamp when a lesson is marked complete | System |
| FR-43 | Students shall view progress status for a specific lesson | Student |
| FR-44 | Students shall view aggregated progress across all lessons in a course | Student |
| FR-45 | Course progress percentage shall be calculated as `(completed_lessons / total_lessons) * 100` | System |
| FR-46 | Progress tracking requires active enrollment in the course | System |

### Reviews & Ratings

| ID | Requirement | Actors |
|----|-------------|--------|
| FR-47 | Students shall create a review (rating 1–5, optional comment up to 1000 characters) for an enrolled course | Student |
| FR-48 | Students shall update their own review | Student |
| FR-49 | Students shall delete their own review | Student |
| FR-50 | The system shall enforce one review per student per course via unique composite constraint | System |
| FR-51 | The system shall expose a public average rating endpoint per course | Public |
| FR-52 | Review listings shall be paginated | Public |

---

## Non-Functional Requirements

| ID | Requirement | Description |
|----|-------------|-------------|
| NFR-01 | **Scalability** | The API is stateless — no server-side session state. Any instance can handle any request, enabling horizontal scaling behind a load balancer. Redis caching reduces repetitive database load on read-heavy endpoints. HikariCP connection pooling manages concurrent database access with configurable pool limits. |
| NFR-02 | **Maintainability** | The codebase follows a layered architecture with four distinct tiers. Each bounded context (auth, courses, enrollments, reviews, progress) lives in its own service module. Service interfaces in the application layer allow alternative implementations without modifying callers. |
| NFR-03 | **Security** | All non-public endpoints require JWT Bearer authentication. Passwords are hashed with BCrypt before storage. Input validation uses Jakarta Bean Validation with a custom `@StrongPassword` annotation. CSRF is disabled by design for stateless APIs. SQL injection is prevented via Hibernate parameterized queries. |
| NFR-04 | **Performance** | JPA relationships default to `FetchType.LAZY`. Open-in-View is disabled to enforce explicit fetch planning. DTO projections prevent entity exposure and over-fetching. Read endpoints for courses and sections are cached in Redis with configurable TTL. |
| NFR-05 | **Reliability** | Flyway-managed versioned migrations guarantee reproducible schema across environments. Database-level unique constraints prevent duplicate records. Foreign key constraints enforce referential integrity. Transactions are managed declaratively via `@Transactional`. |
| NFR-06 | **Availability** | Dockerized deployment with health checks and restart policies supports self-healing. Actuator health endpoints enable integration with container orchestration platforms. |
| NFR-07 | **Observability** | Spring Boot Actuator exposes health, metrics, and info endpoints. All authentication attempts, authorization failures, and data mutations are logged. Environment-specific log levels (DEBUG in development, WARN in production). |
| NFR-08 | **Portability** | Containerization via Docker ensures identical runtime across development, staging, and production environments. Environment-specific configuration is externalized via environment variables with sensible defaults. The database, cache, and application are decoupled services. |
| NFR-09 | **API Consistency** | All responses use a uniform `ApiResponse<T>` envelope with `success`, `message`, `data`, `timestamp`, and `statusCode` fields. Paginated responses follow a consistent `PaginationResponse<T>` record with `content`, `page`, `size`, `totalElements`, `totalPages`, `first`, and `last`. Error responses expose field-level validation details when applicable. |
| NFR-10 | **Extensibility** | New bounded contexts can be added as new sub-packages under `application/services/` without modifying existing modules. Service interfaces allow multiple implementations (e.g., mock for tests, real for production). |

---

## Use Cases / User Stories

### Student

| ID | Story |
|----|-------|
| S-01 | As a student, I want to browse published courses so that I can discover learning content |
| S-02 | As a student, I want to view course details including sections, lessons, and ratings so that I can evaluate the content |
| S-03 | As a student, I want to register and log in so that I can access protected features |
| S-04 | As a student, I want to enroll in a course so that I can access its full content |
| S-05 | As a student, I want to unenroll from a course so that I can manage my learning list |
| S-06 | As a student, I want to mark lessons as completed so that I can track my progress |
| S-07 | As a student, I want to view my overall progress in a course so that I know how much I have completed |
| S-08 | As a student, I want to write a review for an enrolled course so that I can share my feedback |

### Instructor

| ID | Story |
|----|-------|
| I-01 | As an instructor, I want to create courses so that I can offer my content to students |
| I-02 | As an instructor, I want to organize my course into sections and lessons so that content is structured |
| I-03 | As an instructor, I want to publish my course when ready so that students can enroll |
| I-04 | As an instructor, I want to update my course content so that I can keep it current |
| I-05 | As an instructor, I want to delete my course so that I can remove outdated content |
| I-06 | As an instructor, I want to see my courses regardless of publish status so that I can work on drafts |

### Administrator

| ID | Story |
|----|-------|
| A-01 | As an admin, I want to view all users so that I can manage the platform community |
| A-02 | As an admin, I want to delete users so that I can remove policy violators |
| A-03 | As an admin, I want to view all courses across all instructors so that I have platform oversight |

---

## System Architecture

### Layered Architecture

SkillHub follows a four-tier layered architecture inspired by Clean Architecture principles. The defining constraint is the **Dependency Rule**: source code dependencies always point **inward**. Outer layers depend on inner layers; inner layers have no knowledge of outer layers.

```
┌──────────────────────────────────────────────────────────┐
│                      API LAYER                           │
│                (Interface Adapters)                      │
│                                                          │
│  Controllers, DTOs, Validation                          │
│  ┌──────────────────────────────────────────────────┐   │
│  │  Depends on: Application Layer (services, DTOs)   │   │
│  │  Depends on: Infrastructure (config)              │   │
│  └──────────────────────────────────────────────────┘   │
├──────────────────────────────────────────────────────────┤
│                   APPLICATION LAYER                       │
│                  (Use Case Orchestration)                 │
│                                                          │
│  Services, Mappers, Exceptions, Cache Constants          │
│  ┌──────────────────────────────────────────────────┐   │
│  │  Depends on: Domain Layer (entities, enums)      │   │
│  │  Does NOT depend on: API, Infrastructure         │   │
│  └──────────────────────────────────────────────────┘   │
├──────────────────────────────────────────────────────────┤
│                     DOMAIN LAYER                          │
│                  (Enterprise Business Logic)              │
│                                                          │
│  JPA Entities, Enums                                     │
│  ┌──────────────────────────────────────────────────┐   │
│  │  Depends on: Nothing (framework annotations only) │   │
│  │  Does NOT depend on: API, Application, Infra     │   │
│  └──────────────────────────────────────────────────┘   │
├──────────────────────────────────────────────────────────┤
│                  INFRASTRUCTURE LAYER                     │
│              (Framework & Driver Adapters)                │
│                                                          │
│  Repositories, Security Config, JWT Filter,              │
│  Redis Config, Swagger Config, JPA Config                │
│  ┌──────────────────────────────────────────────────┐   │
│  │  Depends on: Application Layer (interfaces)      │   │
│  │  Depends on: Domain Layer (entities)             │   │
│  └──────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────┘
```

### Layer Boundaries

| Layer | Contains | Dependency Direction |
|-------|----------|---------------------|
| **API** | Controllers, DTOs, validation annotations | → Application, Infrastructure |
| **Application** | Services (interfaces + implementations), mappers, exceptions, cache constants, response wrappers | → Domain |
| **Domain** | JPA entities, enums | → None (framework annotations only) |
| **Infrastructure** | Repository implementations, security filters, framework configuration | → Application (interfaces), Domain |

### Dependency Rule Enforcement

- **API → Application**: Controllers inject service interfaces defined in the Application layer. DTOs are defined in Application, not API.
- **Application → Domain**: Services operate on domain entities and enums. Business rules are expressed in services, not entities.
- **Domain → Nothing**: Domain entities carry JPA annotations (`@Entity`, `@Table`, `@Id`, etc.) but have no import dependencies on Spring Web, Spring Security, or any infrastructure code. This is a pragmatic compromise — entities serve as both business objects and persistence models, avoiding a separate ORM mapping layer.
- **Infrastructure → Application**: Repository interfaces are defined in Application and implemented in Infrastructure. Security components (JWT filter, security config) implement interfaces from Application.

---

## Architecture Decisions

### ADR-001: Layered Monolith over Microservices

**Context:** The system needs to serve three user roles with bounded-context features, deployed by a small team.

**Decision:** Implement a layered monolith with package-level bounded contexts rather than distributed microservices.

**Rationale:**
- Deployment overhead of microservices (service discovery, distributed tracing, API gateways) is not justified for this domain complexity
- Package-level isolation (`application/services/{auth, course, enrollment, ...}`) enables future extraction into separate services if needed
- Single database transaction scope simplifies enrollment and progress tracking consistency
- Docker Compose deployment keeps operational complexity low

**Trade-offs:**
- Scaling requires replicating the entire monolith, not individual bounded contexts
- A future split into microservices would require extracting shared domain entities and migrating inter-service communication to async messaging or REST calls

### ADR-002: Domain Entities as JPA Entities

**Context:** The domain layer needs to persist business objects without introducing a separate persistence model.

**Decision:** Domain entities carry JPA annotations (`@Entity`, `@Table`, `@Id`, `@Column`, `@ManyToOne`, `@OneToMany`) and serve dual purpose as business objects and ORM entities.

**Rationale:**
- Eliminates the need for a separate persistence mapping layer (e.g., separate POJO entities + repository mappers)
- Reduces code volume and maintenance overhead in a small-to-medium codebase
- JPA annotations are metadata — they do not couple the domain to any specific framework behavior at runtime

**Trade-offs:**
- Domain entities cannot be used with non-relational databases without modification
- `@ManyToOne` and `@OneToMany` relationships couple entity lifecycle management to Hibernate's persistence context
- Some architects prefer a strict domain model with zero annotations and a separate infrastructure mapping layer — this decision trades purity for pragmatism

### ADR-003: Spring Cache Abstraction with Redis

**Context:** Course and section listings are read-heavy endpoints accessed by unauthenticated users. Repeated database queries on the same data reduce throughput.

**Decision:** Use Spring's `@Cacheable` and `@CacheEvict` annotations with Redis as the cache store.

**Rationale:**
- Declarative caching keeps cache logic out of service implementations
- Redis provides a distributed cache that survives application restarts and is shared across replicas
- Configurable TTL (10 minutes) balances freshness with cache hit rate
- `@CacheEvict` on write operations ensures eventual consistency

### ADR-004: JWT Bearer Authentication over Session-Based Auth

**Context:** The API is consumed by stateless frontend clients (SPA, mobile). Session-based authentication would require sticky sessions or a centralized session store.

**Decision:** Use JWT Bearer tokens with HMAC-SHA256 signing and configurable expiration.

**Rationale:**
- Stateless — no server-side session storage; any instance can verify a token independently
- Token carries user identity and role claims, eliminating a database lookup on each request
- Configurable expiration (default 24 hours) balances security with user experience

**Trade-offs:**
- Token revocation requires a blocklist (not implemented in the current version)
- Token size increases with claim payload; large tokens impact HTTP header size

### ADR-005: Flyway for Schema Migrations

**Context:** The application needs a reproducible, version-controlled mechanism for evolving the database schema across environments.

**Decision:** Use Flyway with versioned SQL migration scripts.

**Rationale:**
- SQL scripts are plain text, reviewable in pull requests, and executable against any environment
- Flyway tracks applied migrations in a dedicated table, preventing duplicate execution
- Spring Boot auto-configures Flyway with minimal configuration
- Works with `ddl-auto=update` in development and `ddl-auto=none` in production

### ADR-006: Stateless API Design (SessionCreationPolicy.STATELESS)

**Context:** The API serves mobile and SPA clients without browser-based sessions.

**Decision:** Configure Spring Security with `SessionCreationPolicy.STATELESS`.

**Rationale:**
- No HTTP sessions — each request is self-contained with its JWT
- Enables horizontal scaling without sticky sessions or a shared session store
- CSRF protection is unnecessary for stateless APIs and is disabled

---

## Sequence Flows

### Authentication Flow

```
Client                      AuthController              AuthService              UserRepository        JwtService
  │                              │                          │                        │                  │
  │  POST /api/v1/auth/login     │                          │                        │                  │
  │  {email, password}           │                          │                        │                  │
  │ ─────────────────────────►   │                          │                        │                  │
  │                              │  authenticate()          │                        │                  │
  │                              │ ─────────────────────►   │                        │                  │
  │                              │                          │  findByEmail(email)    │                  │
  │                              │                          │ ───────────────────►   │                  │
  │                              │                          │ ◄────────────────────  │                  │
  │                              │                          │    User or null        │                  │
  │                              │                          │                        │                  │
  │                              │                          │  BCrypt.matches()      │                  │
  │                              │                          │    ────┐               │                  │
  │                              │                          │    verify password     │                  │
  │                              │                          │    <───┘               │                  │
  │                              │                          │                        │                  │
  │                              │                          │  generateToken(user)   │                  │
  │                              │                          │ ──────────────────────────────────►       │
  │                              │                          │ ◄──────────────────────────────────       │
  │                              │                          │    JWT string          │                  │
  │                              │                          │                        │                  │
  │                              │ ◄─────────────────────   │                        │                  │
  │                              │   ApiResponse(token,     │                        │                  │
  │                              │    user)                 │                        │                  │
  │ ◄─────────────────────────   │                          │                        │                  │
  │  200 OK                      │                          │                        │                  │
```

### Enrollment Flow

```
Client                    EnrollmentController         EnrollmentService         CourseRepository    EnrollmentRepository
  │                              │                          │                        │                     │
  │  POST /api/enrollments      │                          │                        │                     │
  │  {courseId}                 │                          │                        │                     │
  │  Authorization: Bearer JWT  │                          │                        │                     │
  │ ─────────────────────────►  │                          │                        │                     │
  │                              │  enroll(studentId,      │                        │                     │
  │                              │   courseId)             │                        │                     │
  │                              │ ─────────────────────►  │                        │                     │
  │                              │                          │  findById(courseId)    │                     │
  │                              │                          │ ───────────────────►  │                     │
  │                              │                          │ ◄──────────────────── │                     │
  │                              │                          │   Course or throw      │                     │
  │                              │                          │                        │                     │
  │                              │                          │  ── validate:          │                     │
  │                              │                          │  course is PUBLISHED   │                     │
  │                              │                          │  student != instructor │                     │
  │                              │                          │  not already enrolled  │                     │
  │                              │                          │                        │                     │
  │                              │                          │  existsByStudentAnd... │                     │
  │                              │                          │ ──────────────────────────────────►         │
  │                              │                          │ ◄──────────────────────────────────         │
  │                              │                          │   boolean               │                     │
  │                              │                          │                        │                     │
  │                              │                          │  save(enrollment)       │                     │
  │                              │                          │ ──────────────────────────────────►         │
  │                              │                          │ ◄──────────────────────────────────         │
  │                              │                          │   Enrollment            │                     │
  │                              │                          │                        │                     │
  │                              │ ◄─────────────────────  │                        │                     │
  │                              │  ApiResponse(enrollment) │                        │                     │
  │ ◄─────────────────────────  │                          │                        │                     │
  │  201 Created                 │                          │                        │                     │
```

### Course Publishing Flow

```
Client                    CourseController              CourseService            CourseRepository
  │                              │                          │                        │
  │  PATCH /api/v1/courses/      │                          │                        │
  │  {id}/publish                │                          │                        │
  │  Authorization: Bearer JWT   │                          │                        │
  │ ─────────────────────────►  │                          │                        │
  │                              │  publishCourse(courseId, │                        │
  │                              │   principal)             │                        │
  │                              │ ─────────────────────►  │                        │
  │                              │                          │  findById(courseId)    │
  │                              │                          │ ───────────────────►  │
  │                              │                          │ ◄──────────────────── │
  │                              │                          │   Course               │
  │                              │                          │                        │
  │                              │                          │  ── validate:          │
  │                              │                          │  course exists         │
  │                              │                          │  principal is owner    │
  │                              │                          │  or ADMIN              │
  │                              │                          │  status != PUBLISHED   │
  │                              │                          │                        │
  │                              │                          │  setStatus(PUBLISHED)  │
  │                              │                          │  save(course)          │
  │                              │                          │ ───────────────────►  │
  │                              │                          │ ◄──────────────────── │
  │                              │                          │   Course (updated)     │
  │                              │                          │                        │
  │                              │ ◄─────────────────────  │                        │
  │                              │  ApiResponse(course)     │                        │
  │ ◄─────────────────────────  │                          │                        │
  │  200 OK                      │                          │                        │
```

---

## Technology Stack

### Backend

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 17 | Language runtime |
| Spring Boot | 4.0.6 | Application framework |
| Spring Security | 6.x | Authentication and authorization |
| Spring Data JPA / Hibernate | 6.x | ORM and data access |
| Spring Cache Abstraction | — | Declarative caching via `@Cacheable` |
| Spring Boot Actuator | — | Health checks, metrics, monitoring |
| JJWT (io.jsonwebtoken) | 0.12.6 | JWT generation and validation |
| Lombok | — | Boilerplate reduction |
| MapStruct | 1.5.5.Final | DTO mapping |
| Flyway | — | Versioned database migrations |
| Jakarta Bean Validation | — | Input validation |

### Database & Cache

| Technology | Version | Purpose |
|------------|---------|---------|
| PostgreSQL | 16 | Primary relational database |
| HikariCP | — | Connection pooling (max 10, min 5, 30s timeout in production) |
| Redis | 7-alpine | Response caching |

### DevOps

| Technology | Purpose |
|------------|---------|
| Docker | Containerization |
| Docker Compose | Multi-service orchestration (app + PostgreSQL + Redis) |
| Maven Wrapper | Build automation (no local Maven installation required) |

### API Documentation & Testing

| Technology | Purpose |
|------------|---------|
| SpringDoc OpenAPI | Auto-generated OpenAPI 3.0 specification |
| Swagger UI | Interactive API documentation and testing |
| JUnit 5 | Test framework |
| Mockito | Service mocking and behavior verification |
| Spring WebMvc Test | Controller-layer integration testing (`@WebMvcTest`) |
| Spring Security Test | Security-aware test utilities |

---

## Database Design

### Entity-Relationship Diagram

```
┌──────────┐     ┌────────────┐     ┌──────────┐
│  users   │1──N │  courses   │1──N │ sections │
│          │     │            │     │          │
│ PK: id   │     │ PK: id     │     │ PK: id   │
│ email (U)│     │ title (U)  │     │ title (U)│
│ role     │     │ status     │     │order_idx │
│ password │     │ price      │     │course_id │──FK
│          │     │ level      │     │          │
└────┬─────┘     │instructor──│──FK └─────┬────┘
     │           └─────┬──────┘           │
     │                 │                  │
     │        ┌────────┴────────┐         │
     │        │                 │         │
     │  ┌─────┴─────┐   ┌──────┴──┐  ┌───┴─────┐
     │  │enrollments│   │ reviews │  │ lessons │
     │  │           │   │         │  │         │
     │  │student_id─│FK │student_─│FK │section  │──FK
     │  │course_id─ │FK │course_id│FK │video_url│
     │  │progress_% │   │rating   │  │duration │
     │  └─────┬─────┘   └────┬────┘  │is_preview│
     │        │              │       │order_idx│
     │        │              │       └────┬────┘
     │        │              │            │
     └────────┴──────────────┴────────────┘
                                           │
                                      ┌────┴────┐
                                      │lesson_  │
                                      │progress │
                                      │         │
                                      │student_ │──FK
                                      │lesson_id│──FK
                                      │completed│
                                      └─────────┘
```

### Entity Descriptions

| Entity | Table | Key Fields | Unique Constraints |
|--------|-------|------------|--------------------|
| `User` | `users` | id, name, email, password, role (`STUDENT`, `INSTRUCTOR`, `ADMIN`) | `uk_users_email` on email |
| `Course` | `courses` | id, title, description, price, level, status, avg_rating, instructor_id (FK → users) | `uk_course_title` on title |
| `Section` | `sections` | id, title, order_index, course_id (FK → courses) | `unq_sections_course_title` on (course_id, title) |
| `Lesson` | `lessons` | id, title, video_url, duration, is_preview, order_index, section_id (FK → sections) | (section_id, title) |
| `Enrollment` | `enrollments` | id, student_id (FK → users), course_id (FK → courses), progress_percentage | `unq_enrollments_student_course` on (student_id, course_id) |
| `Review` | `reviews` | id, student_id (FK → users), course_id (FK → courses), rating, comment | `unq_reviews_student_course` on (student_id, course_id) |
| `LessonProgress` | `lesson_progress` | id, student_id (FK → users), lesson_id (FK → lessons), is_completed, completed_at | (student_id, lesson_id) |

### Indexing Strategy

| Table | Index | Type | Purpose |
|-------|-------|------|---------|
| `users` | `idx_users_email` | Unique B-tree | Email lookup on login and registration |
| `courses` | `idx_courses_instructor_id` | B-tree | Filter courses by instructor |
| `courses` | `idx_courses_status` | B-tree | Filter published courses |
| `courses` | `idx_courses_level` | B-tree | Filter by difficulty level |
| `sections` | `idx_sections_course_id` | B-tree | Fetch sections for a course |
| `sections` | `unq_sections_course_title` | Unique B-tree | Enforce title uniqueness per course |
| `lessons` | `idx_lessons_section_id` | B-tree | Fetch lessons for a section |
| `enrollments` | `idx_enrollments_student_id` | B-tree | Query student's enrollments |
| `enrollments` | `unq_enrollments_student_course` | Unique B-tree | Prevent duplicate enrollment |
| `reviews` | `unq_reviews_student_course` | Unique B-tree | One review per student per course |

---

## API Design

### Conventions

| Aspect | Convention |
|--------|------------|
| **Protocol** | HTTP/1.1, HTTPS in production |
| **Media Type** | `application/json` |
| **Base URL** | `http://localhost:8080/api` |
| **URL Structure** | `/api/v1/{resource}` for versioned endpoints; `/api/{resource}` for sub-resources |
| **HTTP Verbs** | GET (read), POST (create), PUT (full update), PATCH (partial update), DELETE (remove) |
| **Pluralization** | Resources use plural nouns: `/courses`, `/enrollments` |

### Authentication

All protected endpoints require an `Authorization: Bearer <token>` header. Tokens are obtained via `POST /api/v1/auth/login`.

### Pagination

All collection endpoints support pagination via query parameters:

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | 0 | Zero-based page index |
| `size` | int | 20 | Number of items per page |

Response uses `PaginationResponse<T>`:

```json
{
  "content": [ ... ],
  "page": 0,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8,
  "first": true,
  "last": false
}
```

### Validation

Input validation uses Jakarta Bean Validation annotations (`@NotBlank`, `@Email`, `@Size`, `@DecimalMin`, etc.) combined with a custom `@StrongPassword` annotation. Violations return HTTP 400 with field-level error details:

```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "password": "Password must contain at least one uppercase letter",
    "email": "Email is required"
  },
  "timestamp": "2026-06-25T12:00:00Z",
  "statusCode": 400
}
```

### Error Response Standards

| HTTP Status | Meaning | When |
|-------------|---------|------|
| 200 | OK | Successful read or write |
| 201 | Created | Resource created successfully |
| 204 | No Content | Resource deleted successfully |
| 400 | Bad Request | Validation failure, business rule violation, invalid parameters |
| 401 | Unauthorized | Missing, expired, or invalid JWT |
| 403 | Forbidden | Authenticated but insufficient role |
| 404 | Not Found | Resource does not exist |
| 409 | Conflict | Duplicate resource (email, title, enrollment, review) |
| 500 | Internal Server Error | Unexpected server-side failure |

All error responses use the `ApiResponse<T>` envelope with `success: false`.

### Endpoints Summary

| Prefix | Endpoints | Authentication |
|--------|-----------|----------------|
| `/api/v1/auth` | register, login | Public |
| `/api/v1/courses` | CRUD, publish, filter by level/status, enrollment count | Mixed (public read, protected write) |
| `/api/courses/{courseId}/sections` | CRUD, search, pagination | Mixed |
| `/api/courses/{courseId}/sections/{sectionId}/lessons` | CRUD, preview, pagination | Mixed |
| `/api/enrollments` | enroll, unenroll, list my, status, detail | Student |
| `/api/courses/{courseId}/reviews` | CRUD, average rating, pagination | Mixed |
| `/api/lessons/{lessonId}/progress` | mark complete/incomplete, get status | Student |
| `/api/courses/{courseId}/progress` | aggregated progress | Student |
| `/api/v1/admin/users` | list all, delete | Admin |

### Interactive Documentation

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI Spec:** `http://localhost:8080/v3/api-docs`

---

## Security

| Layer | Mechanism | Implementation |
|-------|-----------|----------------|
| Authentication | JWT Bearer Token | `JwtAuthFilter` (extends `OncePerRequestFilter`) extracts and validates JWT from `Authorization` header on every request |
| Authorization | Role-Based Access Control | Method-level `@PreAuthorize("hasRole('...')")` on controller methods |
| Password Storage | BCrypt | `BCryptPasswordEncoder` — one-way hash with random salt before storage |
| Input Validation | Jakarta Bean Validation | `@Valid`, `@NotBlank`, `@Email`, `@Size`, `@DecimalMin` + custom `@StrongPassword` |
| CSRF | Disabled | Stateless API — no browser session to protect |
| SQL Injection | Parameterized Queries | Spring Data JPA / Hibernate prepared statements; no dynamic SQL concatenation |
| Session Management | Stateless | `SessionCreationPolicy.STATELESS` — no `HttpSession` created or used |

### Permission Matrix

| Resource | Anonymous | Student | Instructor | Admin |
|----------|-----------|---------|------------|-------|
| POST /api/v1/auth/register | ✓ | — | — | — |
| POST /api/v1/auth/login | ✓ | — | — | — |
| GET /api/v1/courses | ✓ | ✓ | ✓ | ✓ |
| GET /api/v1/courses/{id} | ✓ | ✓ | ✓ | ✓ |
| GET /api/v1/courses/instructor/{id} | ✓ | ✓ | ✓ | ✓ |
| GET /api/v1/courses/level | ✓ | ✓ | ✓ | ✓ |
| GET /api/v1/courses/{id}/enrollments/count | ✓ | ✓ | ✓ | ✓ |
| GET /api/courses/{id}/reviews/average-rating | ✓ | ✓ | ✓ | ✓ |
| GET .../sections | ✓ | ✓ | ✓ | ✓ |
| GET .../lessons/preview | ✓ | ✓ | ✓ | ✓ |
| POST /api/enrollments | — | ✓ | — | — |
| DELETE /api/enrollments/{courseId} | — | ✓ | — | — |
| GET /api/enrollments/my | — | ✓ | — | — |
| POST .../lessons/{id}/progress | — | ✓ | — | — |
| DELETE .../lessons/{id}/progress | — | ✓ | — | — |
| CRUD .../reviews | — | ✓ | — | — |
| POST /api/v1/courses | — | — | ✓ | ✓ |
| PUT /api/v1/courses/{id} | — | — | ✓ (own) | ✓ |
| DELETE /api/v1/courses/{id} | — | — | ✓ (own) | ✓ |
| PATCH /api/v1/courses/{id}/publish | — | — | ✓ (own) | ✓ |
| GET /api/v1/courses/my-courses | — | — | ✓ | — |
| GET /api/v1/courses/status | — | — | ✓ | ✓ |
| CRUD .../sections (own course) | — | — | ✓ | ✓ |
| CRUD .../lessons (own course) | — | — | ✓ | ✓ |
| GET /api/v1/courses/admin/all | — | — | — | ✓ |
| GET /api/v1/admin/users | — | — | — | ✓ |
| DELETE /api/v1/admin/users/{id} | — | — | — | ✓ |

---

## Performance Optimizations

| Optimization | Implementation |
|--------------|----------------|
| **Redis Caching** | `@Cacheable("courses")` on course read endpoints; `@Cacheable("sections")` on section read endpoints; `@CacheEvict` on all mutation endpoints; configurable TTL |
| **Pagination** | Every collection endpoint supports `page` and `size` query parameters (default `page=0, size=20`) |
| **Lazy Loading** | All JPA relationships configured with `FetchType.LAZY`; `@Transactional` ensures availability within the session scope |
| **Connection Pooling** | HikariCP with tuned pool: maximum 10 connections, minimum 5 idle, 30-second connection timeout (production profile) |
| **Open-in-View** | Disabled (`spring.jpa.open-in-view=false`) — prevents lazy initialization exceptions in views and encourages explicit fetch planning via `JOIN FETCH` |
| **DTO Projections** | Entities are never serialized directly to HTTP responses. MapStruct mappers transform entities to DTOs, controlling exactly which fields are exposed and preventing circular reference issues |
| **Database Indexing** | B-tree indexes on all foreign key columns; unique composite indexes on business keys to enforce constraints efficiently |
| **Query Optimization** | Repository methods use derived queries; `@NamedEntityGraph` on `Course` defines fetch graph for eager-loading specific relationships; `@BatchSize` on collection fields mitigates N+1 queries |

---

## Deployment Architecture

### Component Interaction

```
                         ┌─────────────────────────────────┐
                         │         Client (SPA / Mobile)    │
                         │     HTTP/JSON over HTTPS         │
                         └───────────────┬─────────────────┘
                                         │
                                         │ Authorization: Bearer <JWT>
                                         ▼
                         ┌─────────────────────────────────┐
                         │     Docker Container: app        │
                         │  ┌───────────────────────────┐  │
                         │  │   Spring Boot 4 / Java 17 │  │
                         │  │                          │  │
                         │  │  ┌─────────────────────┐ │  │
                         │  │  │ JwtAuthFilter       │ │  │
                         │  │  │ (OncePerRequest)    │ │  │
                         │  │  └─────────┬───────────┘ │  │
                         │  │            ▼              │  │
                         │  │  ┌─────────────────────┐ │  │
                         │  │  │ Controllers (8)     │ │  │
                         │  │  └─────────┬───────────┘ │  │
                         │  │            ▼              │  │
                         │  │  ┌─────────────────────┐ │  │
                         │  │  │ Services (8 modules)│ │  │
                         │  │  └──┬──────────────┬───┘ │  │
                         │  │     │              │      │  │
                         │  │     ▼              ▼      │  │
                         │  │  ┌────────┐ ┌──────────┐ │  │
                         │  │  │Redis   │ │ PostgreSQL│ │  │
                         │  │  │Cache   │ │ Database  │ │  │
                         │  │  └───┬────┘ └────┬─────┘ │  │
                         │  └──────┼───────────┼───────┘  │
                         └─────────┼───────────┼──────────┘
                                   │           │
                                   ▼           ▼
                         ┌────────────┐  ┌────────────┐
                         │  Docker    │  │  Docker    │
                         │  Container │  │  Container │
                         │  redis:7   │  │  postgres: │
                         │  -alpine   │  │  16-alpine │
                         │  :6379     │  │  :5432     │
                         └────────────┘  └────────────┘
```

### Network Flow

1. Client sends HTTP request to port `8080` on the host
2. Docker's port mapping forwards `8080:8080` to the `app` container
3. `JwtAuthFilter` intercepts the request, extracts JWT from `Authorization` header, validates signature and expiration
4. Controller receives the authenticated principal and delegates to the appropriate service
5. Service checks Spring Security authorization context (`@PreAuthorize`)
6. Service reads from Redis cache if available (course/section GET endpoints); otherwise queries PostgreSQL
7. Service maps entities to DTOs via MapStruct and returns `ApiResponse<T>`
8. Response is serialized to JSON and returned to the client

---

## DevOps & CI/CD Readiness

### Environment Separation

| Environment | Profile | DDL Mode | SQL Logging | Log Level | HikariCP |
|-------------|---------|----------|-------------|-----------|----------|
| **Development** | `default` | `update` | Enabled | DEBUG | Default |
| **Production** | `prod` | `none` | Disabled | WARN | max=10, min-idle=5, timeout=30s |

Run with a specific profile:

```bash
java -jar app.jar --spring.profiles.active=prod
```

### CI/CD Pipeline (Recommended)

```
┌─────────┐    ┌─────────┐    ┌─────────┐    ┌──────────┐    ┌──────────┐
│  Code   │───►│  Build  │───►│  Test   │───►│  Docker  │───►│  Deploy  │
│  Push   │    │ mvnw    │    │ mvnw    │    │  Build   │    │  Cloud   │
│         │    │ package │    │ test    │    │ compose  │    │  (ECS /  │
│         │    │         │    │         │    │  up      │    │  Render) │
└─────────┘    └─────────┘    └─────────┘    └──────────┘    └──────────┘
```

### Docker

```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Docker Compose

```yaml
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATA_REDIS_HOST=redis
      - DB_URL=jdbc:postgresql://db:5432/${DB_NAME}
      - DB_USERNAME=${DB_USERNAME}
      - DB_PASSWORD=${DB_PASSWORD}
    depends_on:
      - db
      - redis

  db:
    image: postgres:16-alpine
    ports:
      - "5433:5435"
    environment:
      POSTGRES_DB: ${DB_NAME}
      POSTGRES_USER: ${DB_USERNAME}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

volumes:
  postgres_data:
  redis_data:
```

### CI/CD Pipeline (Recommended Configuration)

The pipeline can be configured in GitHub Actions or GitLab CI with the following stages:

1. **Build** — `./mvnw clean package -DskipTests`
2. **Test** — `./mvnw test`
3. **Dockerize** — `docker build -t skillhub .`
4. **Push** — Push image to container registry (Docker Hub, ECR, GHCR)
5. **Deploy** — Deploy to target environment (AWS ECS, Render, Railway, or self-hosted)

---

## Monitoring & Observability

### Actuator Endpoints

| Endpoint | Purpose | Enabled |
|----------|---------|---------|
| `/actuator/health` | Liveness and readiness probes | Always |
| `/actuator/metrics` | JVM metrics (memory, threads, GC), HTTP metrics, cache metrics | Default |
| `/actuator/info` | Application metadata (name, version, Java version) | Default |
| `/actuator/env` | Environment property sources | Default |

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "redis": { "status": "UP" },
    "diskSpace": { "status": "UP" },
    "ping": { "status": "UP" }
  }
}
```

### Logging Strategy

| Aspect | Development | Production |
|--------|-------------|------------|
| **Framework** | SLF4J + Logback | SLF4J + Logback |
| **Application Level** | DEBUG | WARN |
| **Spring Security Level** | INFO | WARN |
| **SQL Logging** | Enabled (`show-sql=true`) | Disabled (`show-sql=false`) |
| **Format** | Timestamp, thread, logger, level, message | Same |

### Tracing Readiness

The application does not currently implement distributed tracing. The following additions would enable full observability in a production deployment:

- Add `micrometer-tracing-bridge-brave` for trace propagation
- Add `logback-correlation-pattern` to inject trace IDs into log lines
- Integrate with Zipkin or Jaeger for trace visualization
- Use `@Observed` annotations on service methods for automatic span creation

---

## Scalability & Future Evolution

### Current Architecture (Monolith)

The monolith scales horizontally by running multiple instances behind a load balancer:

- **Stateless API** — No server-side session state; any instance handles any request
- **Shared Cache** — Redis is a centralized cache shared across instances; cache coherence is handled by TTL-based expiration
- **Shared Database** — All instances connect to the same PostgreSQL instance; connection pooling limits are adjusted per-instance

### Evolution Toward Microservices

If the monolith needs to be decomposed, each bounded context is already isolated at the package level:

| Bounded Context | Potential Microservice | Shared Data |
|----------------|----------------------|-------------|
| `application/services/course/` | Course Service | Course, Section, Lesson tables |
| `application/services/enrollment/` | Enrollment Service | Enrollment table |
| `application/services/review/` | Review Service | Review table |
| `application/services/progress/` | Progress Service | LessonProgress table |
| `application/services/auth/` | Auth Service | User table |

**Migration strategy:**

1. Extract shared domain entities into a shared library package
2. Add REST endpoints to each bounded context for inter-service communication
3. Move each service to a separate process with its own database schema
4. Replace direct repository calls with HTTP or message-based communication
5. Decompose the monolith database into per-service databases using Flyway for independent migrations

### Scaling Considerations

| Component | Limit | Mitigation |
|-----------|-------|------------|
| **Application** | Instance count is unlimited (stateless) | Add instances based on CPU/memory metrics |
| **Redis** | Single node memory capacity | Cluster mode for larger datasets |
| **PostgreSQL** | Connection count, disk I/O | Read replicas for read-heavy workloads; connection pooling per instance |
| **Cache** | TTL-bound eventual consistency | Not suitable for strong consistency requirements |

---

## Project Structure

```
Skill_Hub_Learning_Platform/
├── .env                                    # Local environment variables (gitignored)
├── .gitignore
├── Dockerfile                              # Container build definition
├── docker-compose.yml                      # Multi-service orchestration
├── mvnw / mvnw.cmd                         # Maven wrapper
├── pom.xml                                 # Maven build configuration
│
└── src/
    ├── main/java/com/example/Skill_Hub_Learning_Platform/
    │   ├── SkillHubLearningPlatformApplication.java
    │   │
    │   ├── api/controller/                 # REST controllers (8)
    │   │   ├── AuthController.java
    │   │   ├── CourseController.java
    │   │   ├── EnrollmentController.java
    │   │   ├── LessonController.java
    │   │   ├── LessonProgressController.java
    │   │   ├── ReviewController.java
    │   │   ├── SectionController.java
    │   │   └── UserController.java
    │   │
    │   ├── application/
    │   │   ├── cache/                      # CacheConstants.java
    │   │   ├── dto/request/                # Inbound DTOs
    │   │   ├── dto/response/               # Outbound DTOs
    │   │   ├── exceptions/                 # Custom exceptions + GlobalExceptionHandler
    │   │   ├── mapper/                     # Entity-to-DTO mappers (MapStruct)
    │   │   ├── responses/                  # ApiResponse<T>, PaginationResponse<T>
    │   │   ├── security/                   # JwtService interface, CustomUserDetailsService
    │   │   ├── services/                   # Service modules
    │   │   │   ├── auth/
    │   │   │   ├── course/
    │   │   │   ├── enrollment/
    │   │   │   ├── lesson/
    │   │   │   ├── progress/
    │   │   │   ├── review/
    │   │   │   ├── section/
    │   │   │   └── user/
    │   │   └── validators/                 # @StrongPassword annotation
    │   │
    │   ├── domain/
    │   │   ├── enums/                      # Role, CourseStatus, CourseLevel
    │   │   └── models/                     # JPA entities (7)
    │   │       ├── BaseEntity.java         # Abstract mapped superclass (id, createdAt, updatedAt)
    │   │       ├── Course.java
    │   │       ├── Enrollment.java
    │   │       ├── Lesson.java
    │   │       ├── LessonProgress.java
    │   │       ├── Review.java
    │   │       ├── Section.java
    │   │       └── User.java
    │   │
    │   └── infrastructure/
    │       ├── config/                     # RedisConfig, SwaggerConfig, JpaConfig
    │       ├── repository/                 # Spring Data JPA repositories (6)
    │       └── security/                   # SecurityConfig, JwtAuthFilter, JwtServiceImpl
    │
    └── src/main/resources/
        ├── application.properties          # Development configuration
        ├── application-prod.properties     # Production overrides
        └── db/migration/                   # Flyway migrations
            ├── V1__init_schema.sql
            ├── V2__update_lessons_table.sql
            └── V3__create_lesson_progress_table.sql

    └── test/java/com/example/Skill_Hub_Learning_Platform/
        ├── SkillHubLearningPlatformApplicationTests.java
        ├── api/controller/
        │   ├── AuthControllerIntegrationTest.java
        │   └── UserControllerIntegrationTest.java
        └── application/services/auth/
            └── AuthServiceImplTest.java
```

---

## Quick Start

### Prerequisites

| Tool | Version |
|------|---------|
| Java | 17+ |
| Docker | 24+ |
| Docker Compose | 2.20+ |

### Clone and Build

```bash
git clone https://github.com/Ali-Ragab214/skill-hub-learning-platform.git
cd skill-hub-learning-platform
./mvnw clean package -DskipTests
```

### Run with Docker (Recommended)

```bash
docker compose up --build
```

The API is available at `http://localhost:8080`.

### Run without Docker

Start required services:

```bash
docker run -d --name skillhub-db \
  -e POSTGRES_DB=${DB_NAME:-learning_academy} \
  -e POSTGRES_USER=${DB_USERNAME:-app_user} \
  -e POSTGRES_PASSWORD=${DB_PASSWORD:-change_me} \
  -p 5432:5432 \
  postgres:16-alpine

docker run -d --name skillhub-redis \
  -p 6379:6379 \
  redis:7-alpine
```

Run the application:

```bash
./mvnw spring-boot:run
```

---

## Environment Variables

All configuration is externalized via environment variables with development-safe defaults.

| Variable | Default | Required | Description |
|----------|---------|----------|-------------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/${DB_NAME:-learning_academy}` | Yes | PostgreSQL JDBC connection URL |
| `DB_USERNAME` | `${DB_USERNAME:-app_user}` | Yes | Database user |
| `DB_PASSWORD` | `${DB_PASSWORD:-change_me}` | Yes | Database password |
| `DB_NAME` | `learning_academy` | No | PostgreSQL database name |
| `JWT_SECRET` | *see note* | Yes | HMAC-SHA256 signing key (Base64-encoded, minimum 256 bits) |
| `JWT_EXPIRATION` | `86400000` | Yes | Token validity in milliseconds (default 24 hours) |
| `SERVER_PORT` | `8080` | No | HTTP server port |
| `SHOW_SQL` | `true` | No | Enable Hibernate SQL logging |
| `FORMAT_SQL` | `true` | No | Pretty-print Hibernate SQL |

> **Note:** The `JWT_SECRET` default shown in `application.properties` is a development-only placeholder. In any non-local environment, set `JWT_SECRET` to a unique, cryptographically random Base64-encoded string of at least 256 bits (32 bytes). Generate one with: `openssl rand -base64 32`.

---

## Testing Strategy

| Test Type | Technologies | Scope |
|-----------|--------------|-------|
| **Unit Tests** | JUnit 5, Mockito | Service-layer business logic with mocked repository dependencies |
| **Integration Tests** | Spring WebMvc Test (`@WebMvcTest`) | Controller endpoints — HTTP request/response, validation, error handling, response serialization |
| **Context Load Test** | Spring Boot Test (`@SpringBootTest`) | Application context initialization and bean configuration verification |

### Testing Principles

- Service interfaces are defined in the application layer; unit tests inject mocked implementations via Mockito without bootstrapping the full Spring context
- Controller tests use `@WebMvcTest` slice tests with `@MockitoBean` for service dependencies, loading only the web layer
- Security-aware test utilities verify `@PreAuthorize` enforcement on protected endpoints
- `addFilters = false` on controller tests disables the security filter chain for focused endpoint behavior testing

### Running Tests

```bash
# Run all tests
./mvnw test

# Run a specific test class
./mvnw test -Dtest=AuthServiceImplTest

# Run tests with Maven verify (includes integration tests)
./mvnw verify
```

---

## License

```
MIT License

Copyright (c) 2026 Ali Ragab Ghonim

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## Author

**Ali Ragab Ghonim**

- GitHub: [https://github.com/Ali-Ragab214](https://github.com/Ali-Ragab214)
- Email: [alighonim78@gmail.com](mailto:alighonim78@gmail.com)

---

<p align="center">
  <strong>Built with Spring Boot 4, Java 17, PostgreSQL, and Redis</strong>
  <br>
  <sub>Layered Architecture · RESTful Design · JWT RBAC</sub>
</p>
