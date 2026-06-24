# Skill Hub Learning Platform — API Documentation

> **Base URL:** `http://localhost:8080`  
> **Swagger UI:** `http://localhost:8080/swagger-ui.html`  
> **Health Check:** `http://localhost:8080/actuator/health`

---

## Table of Contents

1. [Authentication](#1-authentication)
2. [Courses](#2-courses)
3. [Sections](#3-sections)
4. [Lessons](#4-lessons)
5. [Enrollments](#5-enrollments)
6. [Reviews](#6-reviews)
7. [Lesson Progress](#7-lesson-progress)
8. [Global Error Responses](#8-global-error-responses)
9. [API Response Types](#9-api-response-types)
10. [Folder Structure Suggestions](#10-folder-structure-suggestions)

---

## 1. Authentication

### POST `/api/v1/auth/register`

- **Description:** Create a new user account.
- **Authentication Required:** No
- **Required Role(s):** None (public)

#### Request Body

```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "StrongP@ss1",
  "role": "Student"
}
```

| Field    | Type   | Required | Description                                            |
| -------- | ------ | -------- | ------------------------------------------------------ |
| name     | string | Yes      | 2–50 characters                                        |
| email    | string | Yes      | Valid email format                                     |
| password | string | Yes      | 8+ chars, uppercase, lowercase, digit, special char    |
| role     | string | No       | `Student` (default), `Instructor`, or `Admin`          |

#### Success Response

**HTTP 201 Created**

```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1,
      "name": "John Doe",
      "email": "john@example.com",
      "role": "Student"
    }
  },
  "timestamp": "2026-06-24T10:30:00",
  "statusCode": 201
}
```

#### Error Responses

| Scenario                    | Status | Message                                         |
| --------------------------- | ------ | ----------------------------------------------- |
| Duplicate email             | 409    | `Email already registered`                      |
| Weak password               | 400    | `Password must contain at least one uppercase letter` |
| Validation errors           | 400    | Field-level validation details                  |
| Invalid role                | 400    | Validation error on role field                  |

#### React Integration Notes

| Aspect            | Guidance                                                           |
| ----------------- | ------------------------------------------------------------------ |
| Hook              | `useMutation` (React Query)                                        |
| Frontend state    | `registerForm: { name, email, password, role }`                    |
| Loading state     | Show spinner on submit button, disable form fields                 |
| Error handling    | Display field-level errors from `errors` map; show API message in toast |
| Post-success      | Store token in `localStorage`/`httpOnly` cookie; redirect to dashboard |

#### Business Rules

- Email must be unique across all users.
- Password must match `^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$`.
- Default role is `Student` if not provided.
- User is created with `enabled = true`.

#### Database Entities Involved

- `User` (insert)

#### Frontend Routes

| Route           | Component          | Access  |
| --------------- | ------------------ | ------- |
| `/register`     | RegisterPage       | Public  |
| `/login`        | LoginPage          | Public  |

---

### POST `/api/v1/auth/login`

- **Description:** Authenticate a user and receive a JWT.
- **Authentication Required:** No
- **Required Role(s):** None (public)

#### Request Body

```json
{
  "email": "john@example.com",
  "password": "StrongP@ss1"
}
```

| Field    | Type   | Required | Description |
| -------- | ------ | -------- | ----------- |
| email    | string | Yes      | Valid email |
| password | string | Yes      | User password |

#### Success Response

**HTTP 200 OK**

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1,
      "name": "John Doe",
      "email": "john@example.com",
      "role": "Student"
    }
  },
  "timestamp": "2026-06-24T10:30:00",
  "statusCode": 200
}
```

#### Error Responses

| Scenario              | Status | Message                                       |
| --------------------- | ------ | --------------------------------------------- |
| Invalid credentials   | 401    | `Bad credentials`                             |
| Account disabled      | 401    | `User account is disabled`                    |
| Validation errors     | 400    | Field-level validation details                |

#### React Integration Notes

| Aspect            | Guidance                                                       |
| ----------------- | -------------------------------------------------------------- |
| Hook              | `useMutation` (React Query)                                    |
| Frontend state    | `loginForm: { email, password }`                               |
| Loading state     | Show spinner on submit button                                  |
| Error handling    | Display "Invalid email or password" for 401; field errors for 400 |
| Post-success      | Store JWT; redirect based on role (`/instructor`, `/admin`, `/dashboard`) |

#### Business Rules

- JWT expires after 24 hours (configurable via `JWT_EXPIRATION`).
- Token claims: `sub` = email, `iat`, `exp`.

---

## 2. Courses

### GET `/api/v1/courses`

- **Description:** List all published courses.
- **Authentication Required:** No
- **Required Role(s):** None (public)

#### Success Response

**HTTP 200 OK**

```json
{
  "success": true,
  "message": "Courses retrieved successfully",
  "data": [
    {
      "id": 1,
      "title": "Java Masterclass",
      "description": "Learn Java from scratch",
      "price": 49.99,
      "level": "BEGINNER",
      "status": "PUBLISHED",
      "instructor": {
        "id": 2,
        "name": "Jane Instructor",
        "email": "jane@example.com",
        "role": "Instructor"
      },
      "sections": [
        {
          "id": 10,
          "title": "Getting Started",
          "orderIndex": 0,
          "lessons": [
            {
              "id": 100,
              "title": "Introduction",
              "videoUrl": "https://example.com/video1",
              "duration": 15,
              "isPreview": true,
              "orderIndex": 0,
              "sectionId": 10
            }
          ]
        }
      ],
      "totalEnrollments": 150,
      "averageRating": 4.5,
      "createdAt": "2026-01-15T08:00:00",
      "updatedAt": "2026-06-20T14:30:00"
    }
  ],
  "timestamp": "2026-06-24T10:30:00",
  "statusCode": 200
}
```

#### Error Responses

| Scenario | Status | Message      |
| -------- | ------ | ------------ |
| None     | —      | Always returns 200 with possibly empty array |

---

### GET `/api/v1/courses/{id}`

- **Description:** Get a single course by ID with all details (sections, lessons, instructor, ratings). If user is authenticated and enrolled, includes progress and review status.
- **Authentication Required:** No (optional — pass `Authorization` header to get enriched response)
- **Required Role(s):** None (public)

#### Path Variables

| Name | Type | Description          |
| ---- | ---- | -------------------- |
| id   | Long | Course ID (1–N)     |

#### Success Response

**HTTP 200 OK**

Same shape as array item in GET `/api/v1/courses`.

#### Error Responses

| Scenario           | Status | Message                              |
| ------------------ | ------ | ------------------------------------ |
| Course not found   | 404    | `Course not found with id: {id}`     |

---

### GET `/api/v1/courses/instructor/{instructorId}`

- **Description:** List all published courses by a specific instructor.
- **Authentication Required:** No
- **Required Role(s):** None (public)

#### Path Variables

| Name          | Type | Description            |
| ------------- | ---- | ---------------------- |
| instructorId  | Long | Instructor user ID     |

#### Success Response

**HTTP 200 OK**

Array of `CourseResponse` objects.

---

### GET `/api/v1/courses/level?level=`

- **Description:** Filter published courses by difficulty level.
- **Authentication Required:** No

#### Query Parameters

| Name  | Type   | Required | Description                   |
| ----- | ------ | -------- | ----------------------------- |
| level | string | Yes      | `BEGINNER`, `INTERMEDIATE`, `ADVANCED` |

#### Success Response

**HTTP 200 OK** — Array of `CourseResponse` objects.

#### Error Responses

| Scenario            | Status | Message                       |
| ------------------- | ------ | ----------------------------- |
| Invalid level value | 400    | `Failed to convert value of type 'java.lang.String' to required type 'com.example.Skill_Hub_Learning_Platform.domain.enums.CourseLevel'` |

---

### GET `/api/v1/courses/status?status=`

- **Description:** List courses by status (typically for instructors to see drafts/published/archived).
- **Authentication Required:** Yes
- **Required Role(s):** `INSTRUCTOR`, `ADMIN`

#### Query Parameters

| Name   | Type   | Required | Description                 |
| ------ | ------ | -------- | --------------------------- |
| status | string | Yes      | `DRAFT`, `PUBLISHED`, `ARCHIVED` |

#### Success Response

**HTTP 200 OK** — Array of `CourseResponse` objects.

#### Error Responses

| Scenario                | Status | Message                         |
| ----------------------- | ------ | ------------------------------- |
| Missing/invalid JWT     | 401    | `Full authentication is required to access this resource` |
| Insufficient role       | 403    | `Access Denied`                 |

---

### GET `/api/v1/courses/my-courses`

- **Description:** Get all courses owned by the authenticated instructor.
- **Authentication Required:** Yes
- **Required Role(s):** `INSTRUCTOR`

#### Success Response

**HTTP 200 OK** — Array of `CourseResponse` objects.

---

### GET `/api/v1/courses/admin/all`

- **Description:** Get every course in the system (all statuses).
- **Authentication Required:** Yes
- **Required Role(s):** `ADMIN`

#### Success Response

**HTTP 200 OK** — Array of `CourseResponse` objects.

---

### GET `/api/v1/courses/{courseId}/enrollments/count`

- **Description:** Get the total number of enrolled students for a course.
- **Authentication Required:** No

#### Path Variables

| Name     | Type | Description |
| -------- | ---- | ----------- |
| courseId | Long | Course ID   |

#### Success Response

**HTTP 200 OK**

```json
{
  "success": true,
  "message": "Enrollment count retrieved successfully",
  "data": 150,
  "timestamp": "2026-06-24T10:30:00",
  "statusCode": 200
}
```

---

### POST `/api/v1/courses`

- **Description:** Create a new course.
- **Authentication Required:** Yes
- **Required Role(s):** `INSTRUCTOR`, `ADMIN`

#### Request Body

```json
{
  "title": "Java Masterclass",
  "description": "Learn Java from scratch to advanced",
  "price": 49.99,
  "level": "BEGINNER",
  "status": "DRAFT"
}
```

| Field       | Type    | Required | Description                                 |
| ----------- | ------- | -------- | ------------------------------------------- |
| title       | string  | Yes      | 4–100 characters, must be unique            |
| description | string  | No       | Free text                                   |
| price       | number  | Yes      | >= 0                                        |
| level       | string  | Yes      | `BEGINNER`, `INTERMEDIATE`, `ADVANCED`      |
| status      | string  | No       | `DRAFT` (default), `PUBLISHED`, `ARCHIVED`  |

#### Success Response

**HTTP 201 Created** — `CourseResponse` object.

#### Error Responses

| Scenario                 | Status | Message                             |
| ------------------------ | ------ | ----------------------------------- |
| Duplicate title          | 409    | `Course with title '...' already exists` |
| Title too short/long     | 400    | Validation error                    |
| Negative price           | 400    | `Price cannot be negative`          |

---

### PUT `/api/v1/courses/{id}`

- **Description:** Update an existing course. Only the owning instructor or an admin can update.
- **Authentication Required:** Yes
- **Required Role(s):** `INSTRUCTOR`, `ADMIN`

#### Path Variables

| Name | Type | Description |
| ---- | ---- | ----------- |
| id   | Long | Course ID   |

#### Request Body

```json
{
  "title": "Java Masterclass 2026",
  "description": "Updated description",
  "price": 59.99,
  "level": "INTERMEDIATE",
  "status": "PUBLISHED"
}
```

All fields are optional — only provided fields are updated.

| Field       | Type    | Required | Description                                  |
| ----------- | ------- | -------- | -------------------------------------------- |
| title       | string  | No       | 4–100 characters, must be unique if changed |
| description | string  | No       | Max 2000 characters                         |
| price       | number  | No       | >= 0                                         |
| level       | string  | No       | `BEGINNER`, `INTERMEDIATE`, `ADVANCED`       |
| status      | string  | No       | `DRAFT`, `PUBLISHED`, `ARCHIVED`             |

#### Success Response

**HTTP 200 OK** — `CourseResponse` object.

#### Error Responses

| Scenario               | Status | Message                              |
| ---------------------- | ------ | ------------------------------------ |
| Course not found       | 404    | `Course not found with id: {id}`     |
| Not the owner          | 403    | `You are not the instructor of this course` |
| Duplicate title        | 409    | `Course with title '...' already exists` |

---

### PATCH `/api/v1/courses/{id}/publish`

- **Description:** Publish a draft course. Changes status from `DRAFT` to `PUBLISHED`.
- **Authentication Required:** Yes
- **Required Role(s):** `INSTRUCTOR`, `ADMIN`

#### Path Variables

| Name | Type | Description |
| ---- | ---- | ----------- |
| id   | Long | Course ID   |

#### Success Response

**HTTP 200 OK** — Published `CourseResponse`.

#### Error Responses

| Scenario                 | Status | Message                                          |
| ------------------------ | ------ | ------------------------------------------------ |
| Course not found         | 404    | `Course not found with id: {id}`                 |
| Already published        | 400    | `Course is already published`                    |
| Not the owner            | 403    | `You are not the instructor of this course`      |

---

### DELETE `/api/v1/courses/{id}`

- **Description:** Delete a course and all its sections, lessons, enrollments, reviews, and progress records (cascade).
- **Authentication Required:** Yes
- **Required Role(s):** `INSTRUCTOR`, `ADMIN`

#### Path Variables

| Name | Type | Description |
| ---- | ---- | ----------- |
| id   | Long | Course ID   |

#### Success Response

**HTTP 204 No Content** — Empty body.

#### Error Responses

| Scenario            | Status | Message                              |
| ------------------- | ------ | ------------------------------------ |
| Course not found    | 404    | `Course not found with id: {id}`     |
| Not the owner       | 403    | `You are not the instructor of this course` |

---

#### Course Endpoints — React Integration Notes

| Endpoint                         | Hook                     | Frontend State                    | Loading State              | Error Handling Strategy                                     |
| -------------------------------- | ------------------------ | --------------------------------- | -------------------------- | ----------------------------------------------------------- |
| `GET /`                          | `useQuery(['courses'])`  | `courses: CourseResponse[]`       | Skeleton cards             | Show empty state with message on error                      |
| `GET /{id}`                      | `useQuery(['course', id])`| `course: CourseResponse \| null` | Full-page spinner          | Redirect to 404 page on not found                           |
| `GET /level`                     | `useQuery(['courses', {level}])` | as above               | Skeleton cards             | Empty state with "no courses at this level"                 |
| `POST /`                         | `useMutation`            | `courseForm` state               | Button spinner             | Field-level validation errors; toast on 409 (duplicate title) |
| `PUT /{id}`                      | `useMutation`            | `editForm` state                 | Button spinner             | Field-level validation errors; toast on 409                 |
| `PATCH /{id}/publish`            | `useMutation`            | —                                | Button spinner on publish  | Toast error if already published                            |
| `DELETE /{id}`                   | `useMutation`            | —                                | Confirm dialog + spinner   | Toast error; remove from list on success                    |
| `GET /my-courses`                | `useQuery(['my-courses'])`| `courses: CourseResponse[]`      | Skeleton list              | Redirect if not logged in (401)                             |
| `GET /admin/all`                 | `useQuery(['admin-courses'])`| as above                     | Skeleton table             | Redirect if not admin (403)                                 |
| `GET /.../enrollments/count`     | `useQuery(['enroll-count', id])` | `count: number`         | Inline number skeleton     | Show 0 on error                                             |

#### Business Rules — Courses

- Title must be unique across all courses.
- Only the course owner (instructor) can modify or delete the course.
- The public listing endpoint (`GET /`) returns only courses with `status = PUBLISHED`.
- Deleting a course cascades to sections, lessons, enrollments, reviews, and lesson progress.
- Courses default to `DRAFT` status when created.
- Courses are cached in Redis for 10 minutes (cache names: `courses`, `courses:all`, `courses:published`).

#### Database Entities Involved

- `Course` (CRUD)
- `Section` (cascade delete)
- `Lesson` (cascade delete)
- `Enrollment` (cascade delete)
- `Review` (cascade delete)
- `LessonProgress` (cascade delete via queries)

#### Frontend Routes — Courses

| Route                  | Component           | Access                   | Required Role            |
| ---------------------- | ------------------- | ------------------------ | ------------------------ |
| `/courses`             | CoursesPage         | Public                   | —                        |
| `/courses/:id`         | CourseDetailsPage   | Public                   | —                        |
| `/courses/new`         | CreateCoursePage    | Protected                | INSTRUCTOR, ADMIN        |
| `/courses/:id/edit`    | EditCoursePage      | Protected                | INSTRUCTOR, ADMIN        |
| `/instructor/courses`  | MyCoursesPage       | Protected                | INSTRUCTOR               |
| `/admin/courses`       | AdminCoursesPage    | Protected                | ADMIN                    |

---

## 3. Sections

### POST `/api/courses/{courseId}/sections`

- **Description:** Add a section to a course.
- **Authentication Required:** Yes
- **Required Role(s):** `INSTRUCTOR`

#### Path Variables

| Name     | Type | Description              |
| -------- | ---- | ------------------------ |
| courseId | Long | Parent course ID         |

#### Request Body

```json
{
  "title": "Getting Started",
  "orderIndex": 0
}
```

| Field      | Type    | Required | Description                       |
| ---------- | ------- | -------- | --------------------------------- |
| title      | string  | Yes      | 2–100 characters, unique per course |
| orderIndex | number  | No       | >= 0, defaults to 0              |

#### Success Response

**HTTP 201 Created**

```json
{
  "success": true,
  "message": "Section created successfully",
  "data": {
    "id": 10,
    "title": "Getting Started",
    "orderIndex": 0,
    "lessons": []
  },
  "timestamp": "2026-06-24T10:30:00",
  "statusCode": 201
}
```

#### Error Responses

| Scenario                     | Status | Message                                   |
| ---------------------------- | ------ | ----------------------------------------- |
| Course not found             | 404    | `Course not found with id: {id}`          |
| Duplicate title in course    | 409    | `Section with title '...' already exists in this course` |
| Not the course owner         | 403    | `You are not the instructor of this course` |

---

### GET `/api/courses/{courseId}/sections`

- **Description:** List sections for a course with pagination.
- **Authentication Required:** No
- **Required Role(s):** None (public)

#### Path Variables

| Name     | Type | Description      |
| -------- | ---- | ---------------- |
| courseId | Long | Parent course ID |

#### Query Parameters

| Name | Type | Required | Default | Description  |
| ---- | ---- | -------- | ------- | ------------ |
| page | int  | No       | 0       | Zero-based   |
| size | int  | No       | 10      | Items per page |

#### Success Response

**HTTP 200 OK**

```json
{
  "success": true,
  "message": "Sections retrieved successfully",
  "data": {
    "content": [
      {
        "id": 10,
        "title": "Getting Started",
        "orderIndex": 0,
        "lessons": [
          {
            "id": 100,
            "title": "Introduction",
            "videoUrl": "https://example.com/video1",
            "duration": 15,
            "isPreview": true,
            "orderIndex": 0,
            "sectionId": 10
          }
        ]
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 5,
    "totalPages": 1,
    "last": true,
    "first": true
  },
  "timestamp": "2026-06-24T10:30:00",
  "statusCode": 200
}
```

---

### GET `/api/courses/{courseId}/sections/search?title=`

- **Description:** Search for a section by title within a course.
- **Authentication Required:** No

#### Path Variables

| Name     | Type | Description      |
| -------- | ---- | ---------------- |
| courseId | Long | Parent course ID |

#### Query Parameters

| Name  | Type   | Required | Description       |
| ----- | ------ | -------- | ----------------- |
| title | string | Yes      | Section title     |

#### Success Response

**HTTP 200 OK** — Single `SectionResponse`.

---

### GET `/api/courses/{courseId}/sections/{id}`

- **Description:** Get a specific section by ID.
- **Authentication Required:** No

#### Path Variables

| Name     | Type | Description       |
| -------- | ---- | ----------------- |
| courseId | Long | Parent course ID  |
| id       | Long | Section ID        |

#### Success Response

**HTTP 200 OK** — `SectionResponse` with lessons.

#### Error Responses

| Scenario             | Status | Message |
| -------------------- | ------ | ------- |
| Section not found    | 404    | `Section not found` |

---

### PUT `/api/courses/{courseId}/sections/{id}`

- **Description:** Update a section.
- **Authentication Required:** Yes
- **Required Role(s):** `INSTRUCTOR`

#### Path Variables

| Name     | Type | Description      |
| -------- | ---- | ---------------- |
| courseId | Long | Parent course ID |
| id       | Long | Section ID       |

#### Request Body

Same shape as POST body.

#### Success Response

**HTTP 200 OK** — Updated `SectionResponse`.

#### Error Responses

| Scenario                     | Status | Message |
| ---------------------------- | ------ | ------- |
| Section not found            | 404    | `Section not found` |
| Duplicate title              | 409    | `Section with title '...' already exists in this course` |

---

### DELETE `/api/courses/{courseId}/sections/{id}`

- **Description:** Delete a section and all its lessons.
- **Authentication Required:** Yes
- **Required Role(s):** `INSTRUCTOR`

#### Path Variables

| Name     | Type | Description      |
| -------- | ---- | ---------------- |
| courseId | Long | Parent course ID |
| id       | Long | Section ID       |

#### Success Response

**HTTP 204 No Content**

---

#### Sections — React Integration Notes

| Endpoint                             | Hook                              | Frontend State        | Loading State         | Error Handling Strategy                    |
| ------------------------------------ | --------------------------------- | --------------------- | --------------------- | ------------------------------------------ |
| `GET /sections` (paginated)          | `useQuery(['sections', courseId, page])` | `sections, pagination` | Skeleton list   | Empty state with "no sections"              |
| `GET /sections/search`               | `useQuery` (enabled by search)    | `searchResult`        | Inline spinner        | "No section found"                         |
| `POST /sections`                     | `useMutation`                     | `sectionForm`         | Button spinner        | Toast on duplicate title                    |
| `PUT /sections/{id}`                 | `useMutation`                     | `editForm`            | Button spinner        | Toast on conflict                           |
| `DELETE /sections/{id}`              | `useMutation`                     | —                     | Confirm + spinner     | Toast; remove from list on success          |

#### Business Rules — Sections

- Section title must be unique within a course.
- Only the course instructor can manage sections.
- Deleting a section cascades to its lessons.
- Order index is user-managed (no auto-reordering).

#### Database Entities Involved

- `Section` (CRUD)
- `Lesson` (cascade delete)

#### Frontend Routes — Sections

| Route                                      | Component             | Access    | Required Role |
| ------------------------------------------ | --------------------- | --------- | ------------- |
| `/courses/:courseId/sections`              | SectionListPage       | Public    | —             |
| `/instructor/courses/:courseId/sections`   | ManageSectionsPage    | Protected | INSTRUCTOR    |

---

## 4. Lessons

### POST `/api/courses/{courseId}/sections/{sectionId}/lessons`

- **Description:** Add a lesson to a section.
- **Authentication Required:** Yes
- **Required Role(s):** `INSTRUCTOR`

#### Path Variables

| Name      | Type | Description       |
| --------- | ---- | ----------------- |
| courseId  | Long | Parent course ID  |
| sectionId | Long | Parent section ID |

#### Request Body

```json
{
  "title": "Introduction to Java",
  "videoUrl": "https://example.com/java-intro.mp4",
  "duration": 15,
  "isPreview": true,
  "orderIndex": 0
}
```

| Field      | Type    | Required | Description                                  |
| ---------- | ------- | -------- | -------------------------------------------- |
| title      | string  | Yes      | 2–100 characters, unique per section         |
| videoUrl   | string  | Yes      | Must start with `http://` or `https://`      |
| duration   | number  | Yes      | 1–300 minutes                               |
| isPreview  | boolean | No       | Default `false`                              |
| orderIndex | number  | No       | >= 0, defaults to 0                         |

#### Success Response

**HTTP 201 Created**

```json
{
  "success": true,
  "message": "Lesson created successfully",
  "data": {
    "id": 100,
    "title": "Introduction to Java",
    "videoUrl": "https://example.com/java-intro.mp4",
    "duration": 15,
    "isPreview": true,
    "orderIndex": 0,
    "sectionId": 10
  },
  "timestamp": "2026-06-24T10:30:00",
  "statusCode": 201
}
```

#### Error Responses

| Scenario                     | Status | Message |
| ---------------------------- | ------ | ------- |
| Section not found            | 404    | `Section not found` |
| Duplicate title in section   | 409    | `Lesson with title '...' already exists in this section` |

---

### GET `/api/courses/{courseId}/sections/{sectionId}/lessons`

- **Description:** List lessons in a section with pagination (full details for enrolled/authenticated users).
- **Authentication Required:** Yes
- **Required Role(s):** `STUDENT`, `INSTRUCTOR`, `ADMIN`

#### Path Variables

| Name      | Type | Description       |
| --------- | ---- | ----------------- |
| courseId  | Long | Parent course ID  |
| sectionId | Long | Parent section ID |

#### Query Parameters

| Name | Type | Required | Default | Description  |
| ---- | ---- | -------- | ------- | ------------ |
| page | int  | No       | 0       | Zero-based   |
| size | int  | No       | 10      | 1–100        |

#### Success Response

**HTTP 200 OK** — Paginated array of `LessonResponse`.

---

### GET `/api/courses/{courseId}/sections/{sectionId}/lessons/preview`

- **Description:** Get only preview lessons for a section (public, no auth required).
- **Authentication Required:** No

#### Path Variables

| Name      | Type | Description       |
| --------- | ---- | ----------------- |
| courseId  | Long | Parent course ID  |
| sectionId | Long | Parent section ID |

#### Success Response

**HTTP 200 OK** — Array of `LessonResponse` (only lessons where `isPreview = true`).

---

### GET `/api/courses/{courseId}/sections/{sectionId}/lessons/{id}`

- **Description:** Get a single lesson by ID.
- **Authentication Required:** No

#### Path Variables

| Name      | Type | Description       |
| --------- | ---- | ----------------- |
| courseId  | Long | Parent course ID  |
| sectionId | Long | Parent section ID |
| id        | Long | Lesson ID         |

#### Success Response

**HTTP 200 OK** — `LessonResponse`.

---

### PUT `/api/courses/{courseId}/sections/{sectionId}/lessons/{id}`

- **Description:** Update a lesson.
- **Authentication Required:** Yes
- **Required Role(s):** `INSTRUCTOR`

#### Path Variables

| Name      | Type | Description       |
| --------- | ---- | ----------------- |
| courseId  | Long | Parent course ID  |
| sectionId | Long | Parent section ID |
| id        | Long | Lesson ID         |

#### Request Body

Same shape as POST body.

#### Success Response

**HTTP 200 OK** — Updated `LessonResponse`.

#### Error Responses

| Scenario                     | Status | Message |
| ---------------------------- | ------ | ------- |
| Lesson not found             | 404    | `Lesson not found` |
| Duplicate title in section   | 409    | `Lesson with title '...' already exists in this section` |

---

### DELETE `/api/courses/{courseId}/sections/{sectionId}/lessons/{id}`

- **Description:** Delete a lesson.
- **Authentication Required:** Yes
- **Required Role(s):** `INSTRUCTOR`

#### Path Variables

| Name      | Type | Description       |
| --------- | ---- | ----------------- |
| courseId  | Long | Parent course ID  |
| sectionId | Long | Parent section ID |
| id        | Long | Lesson ID         |

#### Success Response

**HTTP 204 No Content**

---

#### Lessons — React Integration Notes

| Endpoint                         | Hook                                | Frontend State          | Loading State         | Error Handling Strategy                   |
| -------------------------------- | ----------------------------------- | ----------------------- | --------------------- | ----------------------------------------- |
| `GET /lessons` (paginated)       | `useQuery(['lessons', sectionId, page])` | `lessons, pagination` | Skeleton list   | Empty state                                |
| `GET /lessons/preview`           | `useQuery(['preview-lessons', sectionId])` | `previewLessons` | Inline spinner        | Show nothing on error                      |
| `POST /lessons`                  | `useMutation`                       | `lessonForm`            | Button spinner        | Toast on duplicate title                   |
| `PUT /lessons/{id}`              | `useMutation`                       | `editForm`              | Button spinner        | Toast on conflict                          |
| `DELETE /lessons/{id}`           | `useMutation`                       | —                       | Confirm + spinner     | Toast; remove from list                    |

#### Business Rules — Lessons

- Lesson title must be unique within a section.
- Video URL must be a valid HTTP(S) URL.
- Duration is limited to 1–300 minutes.
- Preview lessons are publicly accessible without authentication.
- Only the course instructor can manage lessons.

#### Database Entities Involved

- `Lesson` (CRUD)
- `LessonProgress` (cascade delete)

#### Frontend Routes — Lessons

| Route                                                                  | Component              | Access    | Required Role |
| ---------------------------------------------------------------------- | ---------------------- | --------- | ------------- |
| `/courses/:courseId/sections/:sectionId/lessons`                       | LessonListPage         | Protected | STUDENT       |
| `/courses/:courseId/sections/:sectionId/lessons/:id`                   | LessonDetailPage       | Protected | STUDENT       |
| `/instructor/courses/:courseId/sections/:sectionId/lessons`            | ManageLessonsPage      | Protected | INSTRUCTOR    |
| `/instructor/courses/:courseId/sections/:sectionId/lessons/new`        | CreateLessonPage       | Protected | INSTRUCTOR    |
| `/instructor/courses/:courseId/sections/:sectionId/lessons/:id/edit`   | EditLessonPage         | Protected | INSTRUCTOR    |

---

## 5. Enrollments

### POST `/api/enrollments`

- **Description:** Enroll the authenticated student in a course.
- **Authentication Required:** Yes
- **Required Role(s):** `STUDENT`

#### Request Body

```json
{
  "courseId": 1
}
```

| Field    | Type | Required | Description |
| -------- | ---- | -------- | ----------- |
| courseId | Long | Yes      | Course to enroll in |

#### Success Response

**HTTP 201 Created**

```json
{
  "success": true,
  "message": "Enrolled successfully",
  "data": {
    "id": 50,
    "courseId": 1,
    "courseTitle": "Java Masterclass",
    "progress": 0,
    "enrolledAt": "2026-06-24T10:30:00"
  },
  "timestamp": "2026-06-24T10:30:00",
  "statusCode": 201
}
```

#### Error Responses

| Scenario                  | Status | Message                                               |
| ------------------------- | ------ | ----------------------------------------------------- |
| Already enrolled          | 409    | `Already enrolled in this course`                     |
| Course not found          | 404    | `Course not found with id: {id}`                      |
| Course not published      | 400    | `Course is not published`                             |
| Student cannot self-enroll| 400    | `Instructors cannot enroll in their own courses`      |

---

### DELETE `/api/enrollments/{courseId}`

- **Description:** Unenroll from a course.
- **Authentication Required:** Yes
- **Required Role(s):** `STUDENT`

#### Path Variables

| Name     | Type | Description |
| -------- | ---- | ----------- |
| courseId | Long | Course ID   |

#### Success Response

**HTTP 204 No Content**

#### Error Responses

| Scenario               | Status | Message |
| ---------------------- | ------ | ------- |
| Not enrolled           | 404    | `Enrollment not found` |

---

### GET `/api/enrollments/my`

- **Description:** Get all enrolled courses for the authenticated student with pagination.
- **Authentication Required:** Yes
- **Required Role(s):** `STUDENT`

#### Query Parameters

| Name | Type | Required | Default | Description  |
| ---- | ---- | -------- | ------- | ------------ |
| page | int  | No       | 0       | Zero-based   |
| size | int  | No       | 10      | 1–100        |

#### Success Response

**HTTP 200 OK** — Paginated `EnrollmentResponse` array.

---

### GET `/api/enrollments/{courseId}`

- **Description:** Get enrollment details for a specific course.
- **Authentication Required:** Yes
- **Required Role(s):** `STUDENT`

#### Path Variables

| Name     | Type | Description |
| -------- | ---- | ----------- |
| courseId | Long | Course ID   |

#### Success Response

**HTTP 200 OK** — Single `EnrollmentResponse`.

#### Error Responses

| Scenario     | Status | Message |
| ------------ | ------ | ------- |
| Not enrolled | 404    | `Enrollment not found` |

---

### GET `/api/enrollments/{courseId}/status`

- **Description:** Check whether the student is enrolled in a course.
- **Authentication Required:** Yes
- **Required Role(s):** `STUDENT`

#### Path Variables

| Name     | Type | Description |
| -------- | ---- | ----------- |
| courseId | Long | Course ID   |

#### Success Response

**HTTP 200 OK**

```json
{
  "success": true,
  "message": "Enrollment status retrieved successfully",
  "data": {
    "enrolled": true
  },
  "timestamp": "2026-06-24T10:30:00",
  "statusCode": 200
}
```

---

#### Enrollments — React Integration Notes

| Endpoint                         | Hook                              | Frontend State             | Loading State        | Error Handling Strategy                          |
| -------------------------------- | --------------------------------- | -------------------------- | -------------------- | ------------------------------------------------ |
| `POST /enrollments`              | `useMutation`                     | —                          | Button spinner       | Toast "Already enrolled" on 409; redirect on 404 |
| `DELETE /enrollments/{courseId}` | `useMutation`                     | —                          | Confirm + spinner    | Toast if not enrolled                            |
| `GET /my`                        | `useQuery(['my-enrollments', page])` | `enrollments, pagination` | Skeleton cards       | Empty state "You haven't enrolled in any courses" |
| `GET /{courseId}`                | `useQuery(['enrollment', courseId])` | `enrollment`             | Inline spinner       | Show "Not enrolled" state                        |
| `GET /{courseId}/status`         | `useQuery(['enrollment-status', courseId])` | `isEnrolled: boolean` | Inline badge spinner | Default to not enrolled                          |

#### Business Rules — Enrollments

- A student can enroll in a course only once.
- A student cannot enroll in their own course (instructor vs student).
- Only published courses can be enrolled in.
- Unenrolling removes the enrollment record and associated lesson progress.
- Progress percentage (0–100) is stored on the enrollment and recalculated via a scheduled or triggered job.

#### Database Entities Involved

- `Enrollment` (CRUD)
- `LessonProgress` (cascade delete on unenroll)
- `Course` (read)
- `User` (read)

#### Frontend Routes — Enrollments

| Route                  | Component               | Access    | Required Role |
| ---------------------- | ----------------------- | --------- | ------------- |
| `/my-learning`         | MyLearningPage          | Protected | STUDENT       |
| `/courses/:id`         | CourseDetailsPage       | Public    | —             |

---

## 6. Reviews

### POST `/api/courses/{courseId}/reviews`

- **Description:** Submit a review for a course (rating + optional comment).
- **Authentication Required:** Yes
- **Required Role(s):** `STUDENT`

#### Path Variables

| Name     | Type | Description |
| -------- | ---- | ----------- |
| courseId | Long | Course ID   |

#### Request Body

```json
{
  "rating": 5,
  "comment": "Excellent course! Very well structured."
}
```

| Field   | Type    | Required | Description                       |
| ------- | ------- | -------- | --------------------------------- |
| rating  | number  | Yes      | 1–5                               |
| comment | string  | No       | Max 1000 characters               |

#### Success Response

**HTTP 201 Created**

```json
{
  "success": true,
  "message": "Review submitted successfully",
  "data": {
    "id": 30,
    "courseId": 1,
    "courseTitle": "Java Masterclass",
    "studentId": 1,
    "studentName": "John Doe",
    "rating": 5,
    "comment": "Excellent course! Very well structured.",
    "createdAt": "2026-06-24T10:30:00",
    "updatedAt": "2026-06-24T10:30:00"
  },
  "timestamp": "2026-06-24T10:30:00",
  "statusCode": 201
}
```

#### Error Responses

| Scenario             | Status | Message                                           |
| -------------------- | ------ | ------------------------------------------------- |
| Not enrolled         | 400    | `You must be enrolled in this course to review it` |
| Already reviewed     | 409    | `You have already reviewed this course`           |
| Invalid rating       | 400    | `Rating must be between 1 and 5`                  |

---

### PUT `/api/courses/{courseId}/reviews`

- **Description:** Update the authenticated student's existing review.
- **Authentication Required:** Yes
- **Required Role(s):** `STUDENT`

#### Path Variables

| Name     | Type | Description |
| -------- | ---- | ----------- |
| courseId | Long | Course ID   |

#### Request Body

```json
{
  "rating": 4,
  "comment": "Updated: still great, but room for improvement."
}
```

#### Success Response

**HTTP 200 OK** — Updated `ReviewResponse`.

#### Error Responses

| Scenario          | Status | Message |
| ----------------- | ------ | ------- |
| Review not found  | 404    | `Review not found` |

---

### DELETE `/api/courses/{courseId}/reviews`

- **Description:** Delete the authenticated student's review.
- **Authentication Required:** Yes
- **Required Role(s):** `STUDENT`

#### Path Variables

| Name     | Type | Description |
| -------- | ---- | ----------- |
| courseId | Long | Course ID   |

#### Success Response

**HTTP 204 No Content**

#### Error Responses

| Scenario          | Status | Message |
| ----------------- | ------ | ------- |
| Review not found  | 404    | `Review not found` |

---

### GET `/api/courses/{courseId}/reviews/my`

- **Description:** Get the authenticated student's review for a course.
- **Authentication Required:** Yes
- **Required Role(s):** `STUDENT`

#### Path Variables

| Name     | Type | Description |
| -------- | ---- | ----------- |
| courseId | Long | Course ID   |

#### Success Response

**HTTP 200 OK** — `ReviewResponse` or empty.

---

### GET `/api/courses/{courseId}/reviews`

- **Description:** List all reviews for a course with pagination.
- **Authentication Required:** Yes
- **Required Role(s):** `STUDENT`, `INSTRUCTOR`, `ADMIN`

#### Path Variables

| Name     | Type | Description |
| -------- | ---- | ----------- |
| courseId | Long | Course ID   |

#### Query Parameters

| Name | Type | Required | Default | Description  |
| ---- | ---- | -------- | ------- | ------------ |
| page | int  | No       | 0       | Zero-based   |
| size | int  | No       | 10      | 1–100        |

#### Success Response

**HTTP 200 OK** — Paginated `ReviewResponse` array.

---

### GET `/api/courses/{courseId}/reviews/average-rating`

- **Description:** Get the average rating for a course.
- **Authentication Required:** No

#### Path Variables

| Name     | Type | Description |
| -------- | ---- | ----------- |
| courseId | Long | Course ID   |

#### Success Response

**HTTP 200 OK**

```json
{
  "success": true,
  "message": "Average rating retrieved successfully",
  "data": 4.5,
  "timestamp": "2026-06-24T10:30:00",
  "statusCode": 200
}
```

---

#### Reviews — React Integration Notes

| Endpoint                              | Hook                                      | Frontend State          | Loading State         | Error Handling Strategy                          |
| ------------------------------------- | ----------------------------------------- | ----------------------- | --------------------- | ------------------------------------------------ |
| `GET /reviews` (paginated)            | `useQuery(['reviews', courseId, page])`   | `reviews, pagination`   | Skeleton review cards | Empty state                                       |
| `GET /reviews/my`                     | `useQuery(['my-review', courseId])`       | `myReview \| null`     | Inline spinner        | Show "Write a review" CTA if null                 |
| `GET /average-rating`                 | `useQuery(['avg-rating', courseId])`      | `averageRating`         | Star skeleton         | Show 0 on error                                   |
| `POST /reviews`                       | `useMutation`                             | `reviewForm`            | Button spinner        | Toast "Already reviewed" on 409; enforce 1–5 range |
| `PUT /reviews`                        | `useMutation`                             | `editForm`              | Button spinner        | Toast on not found                                |
| `DELETE /reviews`                     | `useMutation`                             | —                       | Confirm + spinner     | Toast on not found                                |

#### Business Rules — Reviews

- A student must be enrolled in a course to leave a review.
- A student can leave at most one review per course (unique constraint on `student_id + course_id`).
- Rating must be 1–5.
- Comment is optional, max 1000 characters.
- Average rating is computed via SQL `AVG()` and stored on the `Course` entity for fast reads.
- Review, update, and delete are scoped to the authenticated student's own review only.

#### Database Entities Involved

- `Review` (CRUD)
- `Course` (average rating updated)
- `User` (read student name)

#### Frontend Routes — Reviews

| Route                          | Component            | Access    | Required Role |
| ------------------------------ | -------------------- | --------- | ------------- |
| `/courses/:id`                 | CourseDetailsPage    | Public    | —             |
| `/courses/:id/reviews`         | CourseReviewsPage    | Protected | STUDENT       |

---

## 7. Lesson Progress

### POST `/api/lessons/{lessonId}/progress`

- **Description:** Mark a lesson as completed by the authenticated student.
- **Authentication Required:** Yes
- **Required Role(s):** `STUDENT`

#### Path Variables

| Name     | Type | Description |
| -------- | ---- | ----------- |
| lessonId | Long | Lesson ID   |

#### Success Response

**HTTP 201 Created / 200 OK**

```json
{
  "success": true,
  "message": "Lesson marked as completed",
  "data": {
    "id": 200,
    "lessonId": 100,
    "lessonTitle": "Introduction to Java",
    "completed": true,
    "completedAt": "2026-06-24T10:30:00"
  },
  "timestamp": "2026-06-24T10:30:00",
  "statusCode": 201
}
```

#### Error Responses

| Scenario               | Status | Message |
| ---------------------- | ------ | ------- |
| Lesson not found       | 404    | `Lesson not found with id: {id}` |
| Not enrolled in course | 400    | `You are not enrolled in this course` |

---

### DELETE `/api/lessons/{lessonId}/progress`

- **Description:** Mark a lesson as incomplete (remove completion status).
- **Authentication Required:** Yes
- **Required Role(s):** `STUDENT`

#### Path Variables

| Name     | Type | Description |
| -------- | ---- | ----------- |
| lessonId | Long | Lesson ID   |

#### Success Response

**HTTP 200 OK** — `LessonProgressResponse` with `completed: false`.

#### Error Responses

| Scenario               | Status | Message |
| ---------------------- | ------ | ------- |
| Progress not found     | 404    | `Progress not found` |

---

### GET `/api/lessons/{lessonId}/progress`

- **Description:** Get the authenticated student's progress for a specific lesson.
- **Authentication Required:** Yes
- **Required Role(s):** `STUDENT`

#### Path Variables

| Name     | Type | Description |
| -------- | ---- | ----------- |
| lessonId | Long | Lesson ID   |

#### Success Response

**HTTP 200 OK** — `LessonProgressResponse` with current status.

---

### GET `/api/courses/{courseId}/progress`

- **Description:** Get all lesson progress records for the authenticated student in a course.
- **Authentication Required:** Yes
- **Required Role(s):** `STUDENT`

#### Path Variables

| Name     | Type | Description |
| -------- | ---- | ----------- |
| courseId | Long | Course ID   |

#### Success Response

**HTTP 200 OK**

```json
{
  "success": true,
  "message": "Course progress retrieved successfully",
  "data": [
    {
      "id": 200,
      "lessonId": 100,
      "lessonTitle": "Introduction to Java",
      "completed": true,
      "completedAt": "2026-06-24T10:30:00"
    },
    {
      "id": 201,
      "lessonId": 101,
      "lessonTitle": "Variables and Data Types",
      "completed": false,
      "completedAt": null
    }
  ],
  "timestamp": "2026-06-24T10:30:00",
  "statusCode": 200
}
```

---

#### Lesson Progress — React Integration Notes

| Endpoint                              | Hook                                       | Frontend State                   | Loading State     | Error Handling Strategy                           |
| ------------------------------------- | ------------------------------------------ | -------------------------------- | ----------------- | ------------------------------------------------- |
| `POST /lessons/{id}/progress`         | `useMutation`                              | —                                | Checkbox spinner  | Toast "Not enrolled" if 400                       |
| `DELETE /lessons/{id}/progress`       | `useMutation`                              | —                                | Checkbox spinner  | Toast if not found                                |
| `GET /lessons/{id}/progress`          | `useQuery(['lesson-progress', lessonId])`  | `isCompleted: boolean`           | Inline icon       | Default to uncompleted                            |
| `GET /courses/{id}/progress`          | `useQuery(['course-progress', courseId])`  | `completedLessons: Set<Long>`    | Progress bar      | Show 0% on error                                  |

#### Business Rules — Lesson Progress

- A student must be enrolled in the course to track progress.
- Each student can have at most one progress record per lesson (upsert — create or update).
- `completedAt` is set when `isCompleted = true` and cleared when reset.
- Enrollment progress percentage is calculated as `(completedLessons / totalLessons) * 100`.

#### Database Entities Involved

- `LessonProgress` (CRUD)
- `Lesson` (read)
- `Enrollment` (progress percentage updated)

#### Frontend Routes — Lesson Progress

| Route                                                        | Component             | Access    | Required Role |
| ------------------------------------------------------------ | --------------------- | --------- | ------------- |
| `/my-learning/courses/:courseId/lessons/:lessonId`           | LessonViewerPage      | Protected | STUDENT       |
| `/my-learning/courses/:courseId`                             | CourseProgressPage    | Protected | STUDENT       |

---

## 8. Global Error Responses

All errors follow this standard envelope:

```json
{
  "success": false,
  "message": "Human-readable error message",
  "data": null,
  "timestamp": "2026-06-24T10:30:00",
  "statusCode": 400
}
```

For validation errors, `data` contains a map of field → message:

```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "title": "Title must be between 4 and 100 characters",
    "price": "Price cannot be negative"
  },
  "timestamp": "2026-06-24T10:30:00",
  "statusCode": 400
}
```

### Common HTTP Status Codes

| Status | Meaning           | Typical Cause                                    |
| ------ | ----------------- | ------------------------------------------------ |
| 200    | OK                | Successful read or update                        |
| 201    | Created           | Successful resource creation                     |
| 204    | No Content        | Successful deletion                              |
| 400    | Bad Request       | Validation error, business rule violation        |
| 401    | Unauthorized      | Missing or invalid JWT                           |
| 403    | Forbidden         | Insufficient role                                |
| 404    | Not Found         | Resource does not exist                          |
| 409    | Conflict          | Duplicate resource (email, title, enrollment)    |
| 500    | Internal Server   | Unexpected server error                          |

### Global Exception Map

| Exception / Condition                              | Status | Message Pattern                                       |
| -------------------------------------------------- | ------ | ----------------------------------------------------- |
| `ResourceNotFoundException`                         | 404    | `{Entity} not found with id: {id}`                    |
| `BadRequestException`                               | 400    | Custom message                                        |
| `BusinessException`                                 | 400    | Custom message                                        |
| `DuplicateEmailException`                           | 409    | `Email already registered`                            |
| `DuplicateResourceException`                        | 409    | Custom message                                        |
| `UnauthorizedException`                             | 401    | Custom message                                        |
| `AccessDeniedException`                             | 403    | `Access Denied`                                       |
| `MethodArgumentNotValidException`                   | 400    | Field-level validation errors in `data` map           |
| `DataIntegrityViolationException`                   | 409    | Database constraint violation                         |
| `ExpiredJwtException` / `MalformedJwtException`     | 401    | JWT-specific error messages                           |
| `HttpMessageNotReadableException`                   | 400    | Malformed JSON body                                   |
| `MissingServletRequestParameterException`           | 400    | `Required parameter '{name}' is not present`          |

---

## 9. API Response Types

### 9.1 Generic Wrappers

```typescript
// ApiResponse<T> — every endpoint response
interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T | null;
  timestamp: string;   // ISO-8601
  statusCode: number;
}

// PaginationResponse<T> — paginated list endpoints
interface PaginationResponse<T> {
  content: T[];
  page: number;           // zero-based
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
}
```

### 9.2 Auth Types

```typescript
// POST /api/v1/auth/register
// POST /api/v1/auth/login
interface RegisterRequest {
  name: string;           // 2–50 chars
  email: string;          // valid email
  password: string;       // strong password
  role?: 'Student' | 'Instructor' | 'Admin';
}

interface LoginRequest {
  email: string;
  password: string;
}

// Shared user profile returned in AuthResponse
interface UserResponse {
  id: number;
  name: string;
  email: string;
  role: 'Student' | 'Instructor' | 'Admin';
}

interface AuthResponse {
  token: string;
  user: UserResponse;
}
```

### 9.3 Course Types

```typescript
// POST /api/v1/courses
interface CourseRequest {
  title: string;                    // 4–100 chars
  description?: string;
  price: number;                    // >= 0
  level: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
  status?: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
}

// PUT /api/v1/courses/{id}
interface UpdateCourseRequest {
  title?: string;
  description?: string;             // max 2000 chars
  price?: number;
  level?: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
  status?: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
}

interface CourseResponse {
  id: number;
  title: string;
  description: string;
  price: number;
  level: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
  instructor: UserResponse;
  sections: SectionResponse[];
  totalEnrollments: number;
  averageRating: number;
  createdAt: string;                // ISO-8601
  updatedAt: string;                // ISO-8601
}
```

### 9.4 Section Types

```typescript
// POST /api/courses/{courseId}/sections
// PUT /api/courses/{courseId}/sections/{id}
interface SectionRequest {
  title: string;                    // 2–100 chars
  orderIndex?: number;              // >= 0
}

interface SectionResponse {
  id: number;
  title: string;
  orderIndex: number;
  lessons: LessonResponse[];
}
```

### 9.5 Lesson Types

```typescript
// POST /api/courses/{courseId}/sections/{sectionId}/lessons
// PUT /api/courses/{courseId}/sections/{sectionId}/lessons/{id}
interface LessonRequest {
  title: string;                    // 2–100 chars
  videoUrl: string;                 // must start with http(s)://
  duration: number;                 // 1–300 minutes
  isPreview?: boolean;
  orderIndex?: number;
}

interface LessonResponse {
  id: number;
  title: string;
  videoUrl: string;
  duration: number;
  isPreview: boolean;
  orderIndex: number;
  sectionId: number;
}
```

### 9.6 Enrollment Types

```typescript
// POST /api/enrollments
interface EnrollmentRequest {
  courseId: number;
}

interface EnrollmentResponse {
  id: number;
  courseId: number;
  courseTitle: string;
  progress: number;                 // 0–100
  enrolledAt: string;               // ISO-8601
}
```

### 9.7 Review Types

```typescript
// POST /api/courses/{courseId}/reviews
interface ReviewRequest {
  rating: number;                   // 1–5
  comment?: string;                 // max 1000 chars
}

// PUT /api/courses/{courseId}/reviews
interface UpdateReviewRequest {
  rating?: number;
  comment?: string;
}

interface ReviewResponse {
  id: number;
  courseId: number;
  courseTitle: string;
  studentId: number;
  studentName: string;
  rating: number;
  comment: string;
  createdAt: string;                // ISO-8601
  updatedAt: string;                // ISO-8601
}
```

### 9.8 Lesson Progress Types

```typescript
interface LessonProgressResponse {
  id: number;
  lessonId: number;
  lessonTitle: string;
  completed: boolean;
  completedAt: string | null;       // ISO-8601 or null
}
```

---

## 10. Folder Structure Suggestions

### React Project Structure

```
frontend/
├── public/
├── src/
│   ├── api/
│   │   ├── client.ts                  // Axios instance, JWT interceptor
│   │   ├── auth.api.ts                // register(), login()
│   │   ├── course.api.ts              // CRUD + queries
│   │   ├── section.api.ts
│   │   ├── lesson.api.ts
│   │   ├── enrollment.api.ts
│   │   ├── review.api.ts
│   │   └── progress.api.ts
│   │
│   ├── hooks/
│   │   ├── useAuth.ts                 // useQuery + localStorage for session
│   │   ├── useCourses.ts              // React Query hooks per endpoint
│   │   ├── useSections.ts
│   │   ├── useLessons.ts
│   │   ├── useEnrollments.ts
│   │   ├── useReviews.ts
│   │   └── useProgress.ts
│   │
│   ├── types/
│   │   ├── api.ts                     // ApiResponse<T>, PaginationResponse<T>
│   │   ├── auth.ts                    // AuthResponse, UserResponse, RegisterRequest, LoginRequest
│   │   ├── course.ts                  // CourseResponse, CourseRequest, UpdateCourseRequest
│   │   ├── section.ts                 // SectionResponse, SectionRequest
│   │   ├── lesson.ts                  // LessonResponse, LessonRequest
│   │   ├── enrollment.ts             // EnrollmentResponse, EnrollmentRequest
│   │   ├── review.ts                  // ReviewResponse, ReviewRequest, UpdateReviewRequest
│   │   └── progress.ts               // LessonProgressResponse
│   │
│   ├── pages/
│   │   ├── auth/
│   │   │   ├── LoginPage.tsx
│   │   │   └── RegisterPage.tsx
│   │   │
│   │   ├── courses/
│   │   │   ├── CoursesPage.tsx            // Public course listing
│   │   │   ├── CourseDetailsPage.tsx      // Single course view
│   │   │   ├── CreateCoursePage.tsx       // Instructor
│   │   │   └── EditCoursePage.tsx         // Instructor
│   │   │
│   │   ├── instructor/
│   │   │   ├── MyCoursesPage.tsx
│   │   │   ├── ManageSectionsPage.tsx
│   │   │   ├── ManageLessonsPage.tsx
│   │   │   ├── CreateLessonPage.tsx
│   │   │   └── EditLessonPage.tsx
│   │   │
│   │   ├── admin/
│   │   │   └── AdminCoursesPage.tsx
│   │   │
│   │   ├── learning/
│   │   │   ├── MyLearningPage.tsx         // Student enrolled courses
│   │   │   ├── CourseProgressPage.tsx     // Progress dashboard
│   │   │   └── LessonViewerPage.tsx       // Video player + progress
│   │   │
│   │   └── reviews/
│   │       └── CourseReviewsPage.tsx
│   │
│   ├── components/
│   │   ├── layout/
│   │   │   ├── Navbar.tsx
│   │   │   ├── Sidebar.tsx
│   │   │   ├── Footer.tsx
│   │   │   └── ProtectedRoute.tsx
│   │   │
│   │   ├── common/
│   │   │   ├── Spinner.tsx
│   │   │   ├── SkeletonCard.tsx
│   │   │   ├── Pagination.tsx
│   │   │   ├── StarRating.tsx
│   │   │   ├── ErrorMessage.tsx
│   │   │   └── ConfirmDialog.tsx
│   │   │
│   │   ├── course/
│   │   │   ├── CourseCard.tsx
│   │   │   ├── CourseForm.tsx
│   │   │   └── CourseFilterBar.tsx
│   │   │
│   │   ├── section/
│   │   │   ├── SectionList.tsx
│   │   │   └── SectionForm.tsx
│   │   │
│   │   ├── lesson/
│   │   │   ├── LessonList.tsx
│   │   │   ├── LessonForm.tsx
│   │   │   └── VideoPlayer.tsx
│   │   │
│   │   ├── enrollment/
│   │   │   ├── EnrollButton.tsx
│   │   │   └── EnrollmentCard.tsx
│   │   │
│   │   └── review/
│   │       ├── ReviewForm.tsx
│   │       ├── ReviewCard.tsx
│   │       └── AverageRatingBadge.tsx
│   │
│   ├── context/
│   │   └── AuthContext.tsx
│   │
│   ├── router/
│   │   └── index.tsx                    // React Router config with route protection
│   │
│   ├── utils/
│   │   ├── formatters.ts                // Date, currency formatters
│   │   └── validators.ts                // Client-side validation helpers
│   │
│   ├── App.tsx
│   ├── main.tsx
│   └── index.css
│
├── package.json
├── tsconfig.json
├── vite.config.ts
└── .env.local                           // VITE_API_BASE_URL=http://localhost:8080
```

### Key Design Decisions for the React App

- **HTTP Client:** Axios instance with request interceptor that attaches `Authorization: Bearer <token>` and a response interceptor that redirects to `/login` on 401.
- **State Management:** React Query (TanStack Query) for all server state; React Context only for auth (token + user profile).
- **Route Protection:** `ProtectedRoute` component checks auth context and role; redirects to `/login` or shows 403 page as appropriate.
- **Error Handling:** `useMutation` `onError` callbacks show toast notifications; `useQuery` `onError` sets an error state for the fallback UI.
- **Optimistic Updates:** Toggle lesson completion optimistically; roll back on API error.
- **Folder Co-location:** Each feature has its own API module, hooks, types, pages, and components for clear separation.
