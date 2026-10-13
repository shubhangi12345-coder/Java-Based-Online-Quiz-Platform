-- ============================================================================
-- Java Online Quiz Platform - MySQL 8 schema (REFERENCE / MANUAL SETUP)
--
-- You normally do NOT need to run this file: when the application starts, Hibernate
-- creates the tables itself (spring.jpa.hibernate.ddl-auto=update).
-- Use this file if you want to create the tables by hand, or to study the design.
-- It matches the JPA entities in src/main/java/com/mancy/quizplatform/entity.
--
-- Run:  mysql -u root -p < database/schema.sql
-- ============================================================================

CREATE DATABASE IF NOT EXISTS quiz_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE quiz_platform;

CREATE TABLE IF NOT EXISTS app_users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,              -- BCrypt hash, never the password
    role          VARCHAR(20)  NOT NULL,              -- ADMIN | CREATOR | PARTICIPANT
    enabled       BIT(1)       NOT NULL,
    created_at    DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_app_users_email (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS quizzes (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    title            VARCHAR(200) NOT NULL,
    description      VARCHAR(1000),
    category         VARCHAR(100) NOT NULL,
    difficulty       VARCHAR(20)  NOT NULL,           -- EASY | MEDIUM | HARD
    duration_minutes INT          NOT NULL,
    passing_percent  INT          NOT NULL,
    status           VARCHAR(30)  NOT NULL,           -- DRAFT | PENDING_APPROVAL | APPROVED | REJECTED
    rejection_reason VARCHAR(500),
    created_at       DATETIME(6)  NOT NULL,
    creator_id       BIGINT       NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_quizzes_creator FOREIGN KEY (creator_id) REFERENCES app_users (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS questions (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    quiz_id       BIGINT        NOT NULL,
    question_text VARCHAR(1000) NOT NULL,
    difficulty    VARCHAR(20)   NOT NULL,
    marks         INT           NOT NULL,
    explanation   VARCHAR(1000),
    PRIMARY KEY (id),
    CONSTRAINT fk_questions_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS question_options (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    question_id BIGINT       NOT NULL,
    option_text VARCHAR(500) NOT NULL,
    is_correct  BIT(1)       NOT NULL,                -- exactly one option per question is correct
    sort_order  INT          NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_options_question FOREIGN KEY (question_id) REFERENCES questions (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS attempts (
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    participant_id BIGINT      NOT NULL,
    quiz_id        BIGINT      NOT NULL,
    started_at     DATETIME(6) NOT NULL,
    deadline       DATETIME(6) NOT NULL,              -- started_at + duration: enforced by the server
    submitted_at   DATETIME(6),
    status         VARCHAR(20) NOT NULL,              -- IN_PROGRESS | SUBMITTED | AUTO_SUBMITTED
    score          INT         NOT NULL,
    total_marks    INT         NOT NULL,
    percentage     DOUBLE      NOT NULL,
    passed         BIT(1)      NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_attempts_participant FOREIGN KEY (participant_id) REFERENCES app_users (id),
    CONSTRAINT fk_attempts_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS attempt_answers (
    id                 BIGINT NOT NULL AUTO_INCREMENT,
    attempt_id         BIGINT NOT NULL,
    question_id        BIGINT NOT NULL,
    selected_option_id BIGINT,                        -- NULL = question left unanswered
    is_correct         BIT(1) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_answer_per_question (attempt_id, question_id),
    CONSTRAINT fk_answers_attempt FOREIGN KEY (attempt_id) REFERENCES attempts (id),
    CONSTRAINT fk_answers_question FOREIGN KEY (question_id) REFERENCES questions (id),
    CONSTRAINT fk_answers_option FOREIGN KEY (selected_option_id) REFERENCES question_options (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS feedback (
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    quiz_id        BIGINT      NOT NULL,
    participant_id BIGINT      NOT NULL,
    rating         INT         NOT NULL,              -- 1 to 5
    comment_text   VARCHAR(1000),
    created_at     DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_feedback_quiz_participant (quiz_id, participant_id),
    CONSTRAINT fk_feedback_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes (id),
    CONSTRAINT fk_feedback_participant FOREIGN KEY (participant_id) REFERENCES app_users (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS notifications (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    recipient_id BIGINT       NOT NULL,
    message      VARCHAR(500) NOT NULL,
    link         VARCHAR(200),
    is_read      BIT(1)       NOT NULL,
    created_at   DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_id) REFERENCES app_users (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS system_settings (
    setting_key   VARCHAR(100) NOT NULL,
    setting_value VARCHAR(255) NOT NULL,
    PRIMARY KEY (setting_key)
) ENGINE=InnoDB;

-- Default settings (not secrets). The admin can change them on the Settings page.
INSERT IGNORE INTO system_settings (setting_key, setting_value) VALUES
    ('registration_open', 'true'),
    ('default_passing_percent', '40');

-- NOTE: there is no sample-data.sql on purpose. Demo users need password hashes, and hashes (or
-- passwords) must not be stored in source control. Demo data is created by the Java class
-- DataInitializer when app.demo.enabled=true (see README).
