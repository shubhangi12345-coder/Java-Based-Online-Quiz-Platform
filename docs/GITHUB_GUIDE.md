# Uploading to GitHub safely

1. On github.com create a repository (name it `java-online-quiz-platform`, or reuse your existing empty one).
   Leave "Add README / .gitignore / license" **unticked**.
2. **Check for secrets first.** `application-local.properties` is never uploaded because `.gitignore` excludes it, but verify:
   ```
   git init
   git add .
   git status
   ```
   The list must **not** contain `application-local.properties`, `target/` or `.idea/`.
3. Search for passwords before committing:
   `git grep --cached -n -i "password" -- src/main/resources/application.properties`
   It must show only `${DB_PASSWORD:}`-style placeholders.
4. Commit and push (replace YOUR_USERNAME and the repository name):
   ```
   git branch -M main
   git commit -m "Initial commit: Java Online Quiz Platform"
   git remote add origin https://github.com/YOUR_USERNAME/java-online-quiz-platform.git
   git push -u origin main
   ```
5. GitHub asks for a username and a **personal access token** (not your password). Create one under
   Settings -> Developer settings -> Personal access tokens. Never paste a token into chat or commit it.
6. If the push is rejected because the remote already has files:
   `git pull origin main --allow-unrelated-histories`, resolve conflicts, then push again.
7. Replace `[YOUR FULL NAME]` in `LICENSE`, add screenshots to `docs/screenshots/`, and set the repository About text.
8. If you ever push a real password by mistake, change that password immediately.
