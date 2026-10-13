# Contributing

Suggestions and improvements are welcome. This is a learning project.

1. Fork the repository, then create a branch: `git checkout -b feature/short-description`.
2. Follow the layers: controller -> service -> repository. No business rules in controllers.
3. Validate input on the server. Never trust the browser.
4. Never commit passwords, tokens or personal data. Use `application-local.properties` (ignored by Git).
5. Add or update a test when you change scoring, security rules, quiz workflow or adaptive rules.
6. Run `./mvnw test` before opening a pull request.
7. Commit messages: short and imperative, e.g. `Add question reordering`.

To report a security problem, see [SECURITY.md](SECURITY.md).
