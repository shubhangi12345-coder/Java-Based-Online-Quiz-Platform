# Security

## Status
This is a college project. It has **not** been professionally reviewed or penetration tested. Do not run it on the
public internet without a proper review.

## What the code does
- Passwords are stored only as BCrypt hashes.
- URL access by role: `/admin/**` ADMIN, `/creator/**` CREATOR, `/participant/**` PARTICIPANT.
- Ownership checks in services (a creator only touches their own quizzes; a participant only their own attempts).
- CSRF protection on every POST form; registration can only create participants.
- Quiz timing and scoring happen on the server; correct answers are not sent while a quiz is running.
- No credentials in source code: database settings and the first admin come from `application-local.properties` or
  environment variables. That file is excluded by `.gitignore`.

## Known gaps
- No rate limiting or account lock-out after failed logins.
- Sessions are not invalidated when an admin changes a role or disables an account.
- No password reset, no email verification, no HTTPS configuration (use a reverse proxy in real deployments).

## Reporting a problem
Please do not open a public issue for a vulnerability. Contact the repository owner privately through their GitHub profile.
If you ever commit a real password by mistake, **change that password first**: deleting the file does not remove it from Git history.
