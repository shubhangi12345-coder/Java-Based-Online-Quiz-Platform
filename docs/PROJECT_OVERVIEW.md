# Project overview

**Java Online Quiz Platform** is a monolithic Spring Boot web application with server-rendered pages.
Three roles share one codebase and one MySQL database.

| Role | Purpose |
|---|---|
| Admin | Controls users, approves or rejects quizzes, changes system settings, sees platform statistics |
| Quiz Creator | Writes quizzes and questions, submits them for approval, studies results and feedback |
| Participant | Takes approved quizzes against a timer, reviews results, tracks history, sees the leaderboard |

Design goals: readable code (controller -> service -> repository), server-side security and scoring, and honest
documentation of what is and is not finished. Technology: Java 21, Spring Boot 3.3, Spring Security, Spring Data JPA,
MySQL 8, Thymeleaf, Bootstrap 5 (bundled), Maven.

Related documents: [REQUIREMENTS](REQUIREMENTS.md), [ARCHITECTURE](ARCHITECTURE.md), [DATABASE_DESIGN](DATABASE_DESIGN.md),
[API_AND_ROUTES](API_AND_ROUTES.md), [INSTALLATION_GUIDE](INSTALLATION_GUIDE.md), [USER_GUIDE](USER_GUIDE.md),
[TESTING_GUIDE](TESTING_GUIDE.md), [TROUBLESHOOTING](TROUBLESHOOTING.md), [GITHUB_GUIDE](GITHUB_GUIDE.md).
