# Testing guide

## Automated tests
```
./mvnw test
```
They use an in-memory H2 database (profile `test`), so MySQL is not needed.

| Test class | What it checks | Status in the build environment |
|---|---|---|
| `ScoreCalculatorTest` | percentage, passing rule, grade bands | **Executed, 4/4 passed** (with a stand-in test runner) |
| `AdaptiveRulesTest` | 80/50 thresholds, level limits | **Executed, 5/5 passed** |
| `AttemptServiceTest` | start, scoring, partial marks, foreign option ids, late submit, double submit, ownership, unapproved quiz | Type-checked only, **not executed** |
| `CreatorQuizServiceTest` | workflow, minimum questions, locking after submit, approval/rejection, ownership, duplicate options/questions | Type-checked only, **not executed** |
| `SecurityRulesTest` | role URL rules, CSRF, weak password, password mismatch, role injection on registration | **Not compiled, not executed** (needs real Spring test libraries) |

## Manual smoke test (about 10 minutes)
1. Start the app, log in as admin. Open Users, Quizzes, Settings.
2. Register a participant, log out. Log in as admin, change that user's role to Quiz Creator. Log in as that creator.
3. Create a quiz, add 3 questions, submit. Log in as admin, approve it.
4. Register another participant, take the quiz, submit, read the result, leave feedback.
5. Take a 1-minute quiz and wait: it should auto-submit at 00:00.
6. As the participant open `/admin/dashboard`: you should see **Access denied**.
7. As the creator open `/creator/quizzes/<id of another creator's quiz>`: **Access denied**.
8. Refresh the result page several times: no new attempts appear in History.
