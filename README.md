<div align="center">

# SkillHub Learning Platform

### Production-grade courses platform backend — engineered for clarity, security, and scale.

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.6-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java 17](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-316192?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=for-the-badge&logo=redis&logoColor=white)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://docs.docker.com/compose/)
[![JWT](https://img.shields.io/badge/JWT-HMAC--SHA256-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)](https://jwt.io/)
[![Swagger](https://img.shields.io/badge/Swagger-OpenAPI_3-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)](https://swagger.io/)
[![Flyway](https://img.shields.io/badge/Flyway-Migrations-CC0200?style=for-the-badge&logo=flyway&logoColor=white)](https://flywaydb.org/)
[![JUnit 5](https://img.shields.io/badge/JUnit_5-Tested-25A162?style=for-the-badge&logo=junit5&logoColor=white)](https://junit.org/junit5/)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)](LICENSE)

**[📖 API Docs (Swagger)](#quick-start) · [🚀 Quick Start](#quick-start) · [🏗️ Architecture](#engineering-highlights) · [📬 Contact](#author)**

</div>

---

## At a Glance

SkillHub is a **RESTful backend** for a multi-role online education platform — built to demonstrate real-world engineering decisions, not just working code.

```
3 user roles  ·  7 bounded contexts  ·  40+ REST endpoints  ·  1 docker compose up --build
```

Serves **Students**, **Instructors**, and **Admins** across authentication, course content hierarchy, enrollment lifecycle, progress tracking, and peer reviews — secured with **JWT RBAC** and cached with **Redis**.

---

## Engineering Highlights

> Deliberate design decisions — each chosen for a specific reason.

**🏛️ Strict Layered Architecture** — API → Application → Domain → Infrastructure, dependencies always pointing inward. The Domain layer has zero Spring Web or infrastructure imports — business logic is independently testable without a running container.

**🔐 Method-Level RBAC — Not Just Route Guards**
```java
@PreAuthorize("hasRole('INSTRUCTOR') and @courseService.isOwner(#id, principal.username)")
public void deleteCourse(Long id) { ... }
```
Ownership validated server-side on every mutating operation — knowing the ID isn't enough.

**⚡ Declarative Redis Caching**
```java
@Cacheable(value = "courses", key = "#pageable")
public Page<CourseResponse> getAllCourses(Pageable pageable) { ... }

@CacheEvict(value = "courses", allEntries = true)
public CourseResponse createCourse(CourseRequest request) { ... }
```
Zero cache logic inside services — invalidation happens automatically on writes.

**🗄️ Flyway over `ddl-auto=update`** — Schema changes are versioned SQL scripts in source control. Every environment runs the same migrations in the same order. Schema drift is impossible.

**📦 Stateless by Design** — `SessionCreationPolicy.STATELESS` + JWT. No sticky sessions, no shared session store. Horizontally scalable out of the box.

**🛡️ Custom Validation**
```java
@StrongPassword   // 8+ chars, upper, lower, digit, special char
private String password;
```
Domain-meaningful constraints — not scattered conditional checks.

---

## Tech Stack

| Concern | Technology |
|---------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 4.0.6 |
| Security | Spring Security + JWT (HMAC-SHA256) · `@PreAuthorize` RBAC |
| Persistence | Spring Data JPA (Hibernate) + PostgreSQL 16 |
| Caching | Redis 7 + Spring Cache (`@Cacheable` / `@CacheEvict`) |
| Migrations | Flyway — versioned SQL scripts |
| API Docs | Swagger / OpenAPI 3 |
| Testing | JUnit 5 + Mockito + `@WebMvcTest` |
| Deployment | Docker + Docker Compose (3 services) |

---

## Domain & API

**7 bounded contexts:** Auth · Courses · Sections & Lessons · Enrollments · Progress · Reviews · Admin

| Endpoint | Auth |
|----------|------|
| `POST /api/v1/auth/register` · `POST /api/v1/auth/login` | Public |
| `GET /api/v1/courses` · `GET /api/v1/courses/{id}/sections` | Public |
| `POST /api/v1/courses` · `PATCH /api/v1/courses/{id}/publish` | Instructor (owner) |
| `POST /api/v1/enrollments` · `GET /api/v1/enrollments/me` | Student |
| `POST /api/v1/progress/lessons/{id}/complete` · `GET /api/v1/progress/courses/{id}` | Student (enrolled) |
| `POST /api/v1/reviews` · `GET /api/v1/reviews/courses/{id}/average` | Student / Public |
| `GET /api/v1/users` · `DELETE /api/v1/users/{id}` | Admin |

> Full interactive docs at `http://localhost:8080/swagger-ui.html`

**Key integrity constraints (enforced at DB level):**
- Unique composite on `enrollments(student_id, course_id)` — no duplicate enrollments
- Unique composite on `reviews(student_id, course_id)` — one review per student per course
- FK + CASCADE on all child tables — deleting a course removes all its content

---

## Quick Start

**Prerequisites:** Java 17 · Docker 24+ · Docker Compose 2.20+

```bash
git clone https://github.com/Ali-Ragab214/skill-hub-learning-platform.git
cd skill-hub-learning-platform
./mvnw clean package -DskipTests
docker compose up --build
```

Starts **3 containers**: Spring Boot app · PostgreSQL 16 · Redis 7

| Service | URL |
|---------|-----|
| REST API | `http://localhost:8080/api/v1` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |

**Environment Variables**

| Variable | Default | Required |
|----------|---------|----------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/learning_academy` | ✅ |
| `DB_USERNAME` | `app_user` | ✅ |
| `DB_PASSWORD` | `change_me` | ✅ |
| `JWT_SECRET` | dev placeholder | ✅ |
| `JWT_EXPIRATION` | `86400000` (24h) | ✅ |

```bash
openssl rand -base64 32   # generate a secure JWT secret
```

---

## Project Structure

```
src/main/java/.../
├── api/controller/        # 8 controllers — one per bounded context
├── application/services/  # Service interface + implementation per context
│   ├── auth/  course/  section/  lesson/
│   └── enrollment/  progress/  review/  user/
├── domain/model/          # 7 JPA entities — zero infrastructure imports
└── infrastructure/
    ├── config/            # RedisConfig · SwaggerConfig · JpaConfig
    ├── repository/        # 6 Spring Data JPA repositories
    └── security/          # SecurityConfig · JwtAuthFilter · JwtServiceImpl

src/main/resources/db/migration/
    ├── V1__init_schema.sql
    ├── V2__update_lessons_table.sql
    └── V3__create_lesson_progress_table.sql
```

---

## Testing

```bash
./mvnw test          # unit + integration
./mvnw verify        # full build including integration tests
```

| Layer | Approach |
|-------|----------|
| **Unit** | JUnit 5 + Mockito — service logic, repositories mocked |
| **Integration** | `@WebMvcTest` — HTTP, validation, `@PreAuthorize`, no DB required |
| **Context** | `@SpringBootTest` — full wiring and config validation |

---

## License

MIT © 2026 Ali Ragab Ghonim

---

## Author

<div align="center">

**Ali Ragab Ghonim**
*Backend Engineer — Java · Spring Boot · System Design*

[![Email](https://img.shields.io/badge/Email-alighonim78@gmail.com-D14836?style=for-the-badge&logo=gmail&logoColor=white)](mailto:alighonim78@gmail.com)
[![GitHub](https://img.shields.io/badge/GitHub-Ali--Ragab214-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/Ali-Ragab214)

</div>

---

<div align="center">
  <sub>Layered Architecture · Stateless JWT RBAC · Declarative Redis Caching · Flyway Migrations · Docker Compose</sub>
</div>
