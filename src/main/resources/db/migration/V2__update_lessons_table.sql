-- V2__update_lessons_table.sql
-- Ensure existing data complies with NOT NULL constraints
UPDATE lessons SET is_preview = false WHERE is_preview IS NULL;
UPDATE lessons SET order_index = 0 WHERE order_index IS NULL;

-- Add NOT NULL constraints
ALTER TABLE lessons
    ALTER COLUMN is_preview SET NOT NULL,
    ALTER COLUMN order_index SET NOT NULL;

-- Ensure there are no duplicate (title, section_id) rows before adding unique constraint
-- This uses a PostgreSQL DO block to abort migration if duplicates exist.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM (
            SELECT title, section_id FROM lessons GROUP BY title, section_id HAVING COUNT(*) > 1
        ) dup
    ) THEN
        RAISE EXCEPTION 'Duplicate lessons found for (title, section_id). Please resolve duplicates before running migration.';
    END IF;
END$$;

-- Add Unique Constraint on (title, section_id) if it doesn't already exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_lesson_title_section'
          AND conrelid = 'lessons'::regclass
    ) THEN
        ALTER TABLE lessons
            ADD CONSTRAINT uk_lesson_title_section UNIQUE (title, section_id);
    END IF;
END $$;

