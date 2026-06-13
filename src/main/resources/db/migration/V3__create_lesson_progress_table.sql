CREATE TABLE lesson_progress (
    id          BIGSERIAL PRIMARY KEY,
    student_id  BIGINT NOT NULL REFERENCES users(id),
    lesson_id   BIGINT NOT NULL REFERENCES lessons(id),
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP NOT NULL,
    CONSTRAINT uk_lesson_progress_student_lesson UNIQUE (student_id, lesson_id)
);