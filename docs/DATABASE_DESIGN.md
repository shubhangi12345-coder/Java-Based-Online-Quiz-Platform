# Database design

MySQL 8, schema `quiz_platform`. Hibernate creates the tables from the entities; `database/schema.sql` is the reference.

| Table | Purpose | Key columns |
|---|---|---|
| `app_users` | Accounts | id, full_name, email (UNIQUE), password_hash, role, enabled, created_at |
| `quizzes` | Quizzes | id, title, category, difficulty, duration_minutes, passing_percent, status, rejection_reason, creator_id -> app_users |
| `questions` | Questions | id, quiz_id -> quizzes, question_text, difficulty, marks, explanation |
| `question_options` | The 4 answer choices | id, question_id -> questions, option_text, is_correct, sort_order |
| `attempts` | One participant taking one quiz | id, participant_id, quiz_id, started_at, deadline, submitted_at, status, score, total_marks, percentage, passed |
| `attempt_answers` | Answer to one question in one attempt | id, attempt_id, question_id, selected_option_id (NULL = unanswered), is_correct; UNIQUE(attempt_id, question_id) |
| `feedback` | Rating and comment | id, quiz_id, participant_id, rating, comment_text; UNIQUE(quiz_id, participant_id) |
| `notifications` | In-app messages | id, recipient_id, message, link, is_read, created_at |
| `system_settings` | Key/value settings | setting_key (PK), setting_value |

## Relationships
- user 1-N quiz (creator); quiz 1-N question (cascade delete); question 1-N option (cascade delete)
- user N-M quiz through `attempts` (the junction table); attempt 1-N attempt_answers
- user 1-N notifications; quiz N-M user through `feedback`

## Rules enforced by the design
- Unique email; one answer row per question per attempt; one feedback per participant per quiz.
- `is_correct` is stored on each answer so editing a question later cannot change old results (questions of
  approved quizzes cannot be edited anyway).
- Unanswered is represented by `selected_option_id = NULL`; rank, averages and counts are calculated, not stored.
- Users are disabled rather than deleted, so history is kept.

Entity-relationship diagram: [ER_DIAGRAM.md](ER_DIAGRAM.md).
