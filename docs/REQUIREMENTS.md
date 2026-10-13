# Requirements

## Functional requirements
| ID | Requirement | Status |
|---|---|---|
| FR1 | Register (participants only), log in, log out | Implemented |
| FR2 | Role-based access; users cannot open other roles' pages | Implemented (URL rules + service checks) |
| FR3 | Admin lists users, changes roles, enables/disables accounts | Implemented |
| FR4 | Admin creates users directly | **Not implemented** (admin changes the role of a registered user instead) |
| FR5 | Creator creates, edits, deletes quizzes and questions (4 options, marks, difficulty, explanation) | Implemented |
| FR6 | Creator sets duration and passing percentage | Implemented |
| FR7 | Creator submits a quiz for approval; admin approves or rejects with a reason | Implemented |
| FR8 | Participants see only approved quizzes; search and difficulty filter | Implemented |
| FR9 | Timed attempt, automatic submit at timeout, server-side deadline | Implemented (answers are sent at submit time) |
| FR10 | Server-side scoring, result page with correct answers and explanations | Implemented |
| FR11 | Attempt history and leaderboard | Implemented |
| FR12 | Creator results report: attempts, average, highest, lowest, pass rate, feedback | Implemented |
| FR13 | Quiz feedback (rating + comment) and in-app notifications | Implemented |
| FR14 | Admin system settings (registration on/off, default passing %) | Implemented |
| FR15 | Rule-based adaptive difficulty | Implemented as a **recommendation** only |
| FR16 | Reorder questions, charts, reminders, user-to-user messages, profile page | **Not implemented** |

## Non-functional requirements
| Area | Requirement |
|---|---|
| Security | BCrypt hashes, CSRF, server-side authorization, no secrets in Git |
| Integrity | Deadline stored in the database; each attempt is graded once; one running attempt per quiz and participant |
| Usability | Responsive Bootstrap layout, form validation messages, confirmation for risky actions |
| Maintainability | Layered architecture, DTO/form classes, unit and service tests |
| Portability | Java 21 + Maven wrapper + MySQL 8 on a normal laptop |
| Scale | Classroom size; no pagination or caching |
