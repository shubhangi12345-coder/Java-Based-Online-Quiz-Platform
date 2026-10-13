# Java Online Quiz Platform

A role-based online quiz platform for practising Java, built with **Spring Boot 3, MySQL, Spring Security and Thymeleaf**.
Creators write quizzes, an admin approves them, and participants take timed quizzes with automatic scoring.

> **Honest status:** this project was generated as one package and has **not yet been built or run on a real machine
> with Maven and MySQL**. See [Verification status](#verification-status) for exactly what was and was not checked.
> If something fails on your laptop, paste the error message and it can be fixed. Start with
> [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md).

## Problem statement and objectives
Learners need a place to take timed Java quizzes and see where they went wrong; teachers need to publish quizzes and see
results; an administrator must control quality and users.

Objectives: secure role-based access, an approval workflow for quizzes, trustworthy server-side timing and scoring,
simple analytics, and code that a second-year B.Tech student can read and explain.

## Features
| Role | What works |
|---|---|
| **Everyone** | Register (participants only), log in / log out, BCrypt password hashing, in-app notifications |
| **Admin** | Dashboard statistics, list users, change roles, enable/disable accounts, review quizzes, approve / reject (with reason), system settings (registration on/off, default passing %) |
| **Quiz Creator** | Dashboard, create / edit / delete quizzes, add / edit / delete questions with 4 options, duration and passing mark, submit for approval, results page per quiz (attempts, average, highest, lowest, pass rate, feedback) |
| **Participant** | Dashboard, browse and search approved quizzes, timed attempt, automatic submit at timeout, instant result with answer review and explanations, attempt history, leaderboard, rate a quiz |
| **Adaptive difficulty** | *Rule-based recommendation* of the next difficulty level (see below) |

### Quiz life cycle
`DRAFT -> PENDING_APPROVAL -> APPROVED` or `REJECTED -> (creator edits) -> PENDING_APPROVAL`.
Only the admin can approve. Only approved quizzes are visible to participants.

### Rule-based adaptive difficulty
Based on the average of your **last 3 finished attempts**: 80% or more suggests one level harder, 50-79% keeps the level,
below 50% suggests one level easier. The dashboard shows the suggestion and the quiz list marks matching quizzes
"Recommended". This is simple if/else logic, **not machine learning**. It recommends a quiz level; it does not change
questions *inside* a running quiz.

## Technology stack
Java 21, Spring Boot 3.3.5, Spring MVC, Spring Data JPA (Hibernate), Spring Security 6, MySQL 8, Thymeleaf,
Bootstrap 5.3 (bundled in the project, no CDN needed), plain JavaScript, Maven (wrapper included), JUnit 5 + H2 for tests.

## Prerequisites
1. **JDK 21** (check with `java -version`)
2. **MySQL 8** running locally (MySQL Workbench is handy)
3. **VS Code** with the *Extension Pack for Java* (and optionally *Spring Boot Extension Pack*)
4. Git (only for uploading to GitHub)

You do **not** need to install Maven: the included wrapper (`mvnw`) downloads it on first run. This needs internet.

## Installation
```bash
# 1. open a terminal in the project folder (the one containing pom.xml)

# 2. create the database (in MySQL Workbench or the mysql command line)
CREATE DATABASE quiz_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 3. create your local settings file (it is ignored by Git, so your password is never uploaded)
cp application-local.properties.example application-local.properties     # Windows: copy ...
#    then open application-local.properties and replace every CHANGE_ME

# 4. run the application
./mvnw spring-boot:run          # Windows PowerShell: .\mvnw.cmd spring-boot:run
```
Open **http://localhost:8080**. The first start creates all tables automatically.

### Environment configuration
Settings are read from `application-local.properties` (recommended) **or** from environment variables:

| Purpose | Property | Environment variable |
|---|---|---|
| DB user / password | `spring.datasource.username` / `.password` | `DB_USERNAME` / `DB_PASSWORD` |
| DB URL | `spring.datasource.url` | `DB_URL` |
| First admin | `app.admin.email`, `app.admin.password`, `app.admin.name` | `APP_ADMIN_EMAIL`, `APP_ADMIN_PASSWORD` |
| Demo data | `app.demo.enabled`, `app.demo.password` | `APP_DEMO_ENABLED`, `APP_DEMO_PASSWORD` |

### Accounts (no passwords are stored in the code)
- **Admin**: created automatically on first start from `app.admin.email` / `app.admin.password` (you choose them).
- **Creators**: log in as admin, or register and ask the admin to change the role on *Users*.
- **Participants**: use the Register page.
- **Optional demo data**: set `app.demo.enabled=true` and a `app.demo.password` before the first start to get
  `demo.creator@example.com`, `demo.student1@example.com`, `demo.student2@example.com` and one approved quiz,
  all with the demo password you chose.

## Build and test
```bash
./mvnw clean package        # compile + run tests + build the jar
./mvnw test                 # tests only (uses an in-memory H2 database, MySQL is not needed)
```
See [docs/TESTING_GUIDE.md](docs/TESTING_GUIDE.md).

## Project structure
```
java-online-quiz-platform/
├── pom.xml  mvnw  mvnw.cmd  .mvn/
├── application-local.properties.example
├── database/schema.sql                 reference schema (Hibernate creates tables automatically)
├── docs/                               project documentation
└── src/
    ├── main/java/com/mancy/quizplatform/
    │   ├── OnlineQuizPlatformApplication.java
    │   ├── configuration/   SecurityConfig, DataInitializer
    │   ├── controller/      Home, Auth, Admin, AdminQuiz, Creator, Participant, Notification
    │   ├── service/         business rules (UserService, AttemptService, CreatorQuizService ...)
    │   ├── repository/      Spring Data JPA interfaces
    │   ├── entity/          JPA entities
    │   ├── dto/             form objects and view data
    │   ├── security/        user loading, redirect after login
    │   ├── exception/       custom exceptions + global handler
    │   ├── enums/           Role, QuizStatus, Difficulty, AttemptStatus
    │   └── util/            ScoreCalculator, AdaptiveRules
    ├── main/resources/      application.properties, templates/, static/ (css, js, bootstrap)
    └── test/                unit, service and security tests
```

## How to use each role
See [docs/USER_GUIDE.md](docs/USER_GUIDE.md). In short: *creator* -> New quiz -> add 3+ questions -> Submit;
*admin* -> Quizzes -> Review -> Approve; *participant* -> Browse quizzes -> Start -> Submit -> read the review.

## Screenshots
Add your own images to `docs/screenshots/` and link them here.

| Screen | Placeholder |
|---|---|
| Login | _screenshot placeholder_ |
| Admin dashboard | _screenshot placeholder_ |
| Creator: manage quiz | _screenshot placeholder_ |
| Quiz attempt with timer | _screenshot placeholder_ |
| Result page | _screenshot placeholder_ |
| Leaderboard | _screenshot placeholder_ |

## Security notes
BCrypt password hashes; role rules on `/admin/**`, `/creator/**`, `/participant/**`; ownership checks in services;
CSRF tokens on all forms; the score is calculated on the server; correct answers are never sent to the browser while a
quiz is running. This is a student project that has **not had a professional security review**, so do not
expose it to the internet as-is. See [SECURITY.md](SECURITY.md).

## Verification status
| Check | Result |
|---|---|
| All Java files type-checked with `javac` against stub versions of the Spring/Jakarta APIs (everything except `SecurityConfig`) | Passed, 0 errors. Stubs only approximate the real libraries |
| Pure unit tests (`ScoreCalculatorTest`, `AdaptiveRulesTest`) actually executed | Passed, 9/9 |
| Service tests type-checked against the main code | Passed (not executed) |
| Template links and form actions compared with controller routes | 72 checked, all match |
| No secrets in the source tree | Checked |
| `./mvnw clean package` with the real dependencies | **Not run** (no access to Maven Central in the build environment) |
| Spring context start-up, JPA mappings, Thymeleaf rendering | **Not run** |
| Service / security tests with Spring and H2 | **Not run** |
| Running against a real MySQL server | **Not run** |
| `SecurityConfig` | Not type-checked, not run |

## Known limitations
- Not yet built or run end to end (see above); expect a few small fixes on first run.
- Answers are sent when you submit. They are not saved click by click, so a browser crash before submitting loses them.
- All questions are shown on one page; there is no question palette or flagging.
- Question reordering, admin "create user", charts, reminders, profile / password change and user-to-user messaging
  are **not implemented**. Only notifications (system -> user) and quiz feedback exist.
- Adaptive difficulty is a recommendation only.
- A role change or account disable takes full effect at the user's next login (existing sessions are not invalidated).
- No pagination; fine for classroom-sized data.
- `spring.jpa.hibernate.ddl-auto=update` is convenient for learning; use migrations (e.g. Flyway) for real deployments.

## Future improvements
Auto-save answers during the quiz, question palette, per-question adaptive serving, charts, user-to-user messages,
password reset, pagination, CSV import/export, Flyway migrations, a CI workflow.

## GitHub upload
See [docs/GITHUB_GUIDE.md](docs/GITHUB_GUIDE.md).

## License
MIT. See [LICENSE](LICENSE).
