# Routes

All pages are server-rendered. There is no separate REST API. POST routes need the CSRF token (forms add it automatically).

## Public
| Method | Path | Purpose |
|---|---|---|
| GET | `/` | Landing page (logged-in users are redirected to their dashboard) |
| GET / POST | `/login` | Login page / handled by Spring Security |
| POST | `/logout` | Log out |
| GET / POST | `/register` | Registration (always creates a participant) |
| GET | `/access-denied` | Shown when a role may not open a page |

## Any logged-in user
| GET | `/notifications` | Notification list |
|---|---|---|
| POST | `/notifications/read-all` | Mark all as read |

## Admin (`/admin/**`, role ADMIN)
| Method | Path | Purpose |
|---|---|---|
| GET | `/admin/dashboard` | Statistics and pending quizzes |
| GET | `/admin/users?role=` | User list with optional role filter |
| POST | `/admin/users/{id}/role` | Change a user's role (not your own) |
| POST | `/admin/users/{id}/toggle` | Enable / disable an account (not your own) |
| GET | `/admin/quizzes?status=` | All quizzes |
| GET | `/admin/quizzes/{id}` | Quiz detail with correct answers |
| POST | `/admin/quizzes/{id}/approve` | Approve a pending quiz |
| POST | `/admin/quizzes/{id}/reject` | Reject with a reason |
| GET / POST | `/admin/settings` | System settings |

## Quiz Creator (`/creator/**`, role CREATOR; ownership checked in the service)
| Method | Path | Purpose |
|---|---|---|
| GET | `/creator/dashboard` | Statistics and recent quizzes |
| GET | `/creator/quizzes` | My quizzes |
| GET / POST | `/creator/quizzes/new` | Create a quiz (saved as draft) |
| GET | `/creator/quizzes/{id}` | Manage one quiz |
| GET / POST | `/creator/quizzes/{id}/edit` | Edit (draft or rejected only) |
| POST | `/creator/quizzes/{id}/submit` | Submit for approval (needs 3+ questions) |
| POST | `/creator/quizzes/{id}/delete` | Delete (not while pending, not if attempted) |
| GET / POST | `/creator/quizzes/{id}/questions/new` | Add a question |
| GET / POST | `/creator/quizzes/{id}/questions/{qid}/edit` | Edit a question |
| POST | `/creator/quizzes/{id}/questions/{qid}/delete` | Delete a question |
| GET | `/creator/quizzes/{id}/results` | Attempts, statistics, feedback |

## Participant (`/participant/**`, role PARTICIPANT)
| Method | Path | Purpose |
|---|---|---|
| GET | `/participant/dashboard` | Statistics, suggested difficulty, recent attempts |
| GET | `/participant/quizzes?keyword=&difficulty=` | Browse approved quizzes |
| GET | `/participant/quizzes/{id}` | Quiz details |
| POST | `/participant/quizzes/{id}/start` | Start (or resume) an attempt |
| GET | `/participant/attempts/{id}/take` | Quiz-taking page with timer |
| POST | `/participant/attempts/{id}/submit` | Submit option ids (`q_<questionId>=<optionId>`) |
| GET | `/participant/attempts/{id}/result` | Result and review (finished attempts only) |
| GET | `/participant/attempts` | Attempt history |
| GET | `/participant/leaderboard` | Leaderboard |
| POST | `/participant/quizzes/{id}/feedback` | Rate a quiz (after finishing it) |
