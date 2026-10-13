# Architecture

```
Browser -> Controller -> Service -> Repository -> MySQL
              |             |
        forms / view data   entities
```

| Package (`com.mancy.quizplatform`) | Responsibility |
|---|---|
| `controller` | Receive requests, validate forms, choose the template, redirect after POST. No business rules |
| `service` | Business rules and transactions (status workflow, scoring, ownership checks, leaderboard) |
| `repository` | Spring Data JPA interfaces (derived queries; two use `SELECT ... FOR UPDATE`) |
| `entity` | JPA entities; a few rules about their own state (`Quiz.isEditable()`) |
| `dto` | Form objects with validation annotations and small view-data classes |
| `security` / `configuration` | Login, role redirect, URL rules, first-run data |
| `exception` | Custom exceptions and a handler that shows friendly error pages |
| `util` | Pure logic that is easy to test: `ScoreCalculator`, `AdaptiveRules` |

## Security model
1. Form login; `CustomUserDetailsService` loads the user; BCrypt compares the password; disabled users cannot log in.
2. `SecurityConfig` URL rules by role. Everything except landing, login, register and static files needs login.
3. Services check ownership (`getOwnedQuiz`, `loadOwned`) and throw `AccessDeniedException`, which shows `/access-denied`.
4. CSRF token on every POST. Logout is a POST.

## Key workflows
**Quiz approval:** creator saves a DRAFT, adds at least 3 questions, submits (PENDING_APPROVAL, locked). Admin approves
(APPROVED, visible) or rejects with a reason (REJECTED, editable again). Creator and admin get notifications.

**Quiz attempt:**
1. `POST /participant/quizzes/{id}/start` locks the participant row, then creates the attempt with `deadline = now + duration`
   (or returns the running one, so refresh/double click never makes two attempts).
2. `GET .../take` shows all questions (no correct answers in the HTML) and a JavaScript countdown.
3. `POST .../submit` sends only chosen option ids. The server checks each option belongs to its question, adds up
   the marks, stores the answers and the result, and redirects to the result page.
4. Late submit (more than 20 seconds after the deadline) is graded with no answers. A scheduler closes abandoned
   attempts every minute. The attempt row is locked while grading, so it can only be graded once.

**Scoring:** correct answer = the question's marks, wrong or blank = 0. `percentage = score / total x 100` (1 decimal).
Passed when `percentage >= passingPercent`. Grade: A >= 90, B >= 75, C >= 60, D >= 40, else F.

**Leaderboard:** finished attempts of enabled participants; order by average, then best, then number of attempts;
ties share a rank.

**Adaptive recommendation:** average of the last 3 finished attempts: >= 80 harder, 50-79 same, < 50 easier
(`AdaptiveRules`).

## Design decisions
- Open-in-view is on so templates can read lazy relations; this is simple but means the DB session stays open while rendering.
- `ddl-auto=update` creates tables automatically; `database/schema.sql` documents the intended schema.
- No Flyway/Liquibase, no Docker, no external services, to keep the project easy to run.
