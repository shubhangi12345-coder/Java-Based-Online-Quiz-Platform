# Troubleshooting

| Problem | Likely cause and fix |
|---|---|
| `java: command not found` / wrong version | Install JDK 21 and reopen the terminal. `java -version` must say 21 |
| `./mvnw: Permission denied` (macOS/Linux) | `chmod +x mvnw` |
| `mvnw` cannot download Maven | No internet or a proxy. Check your connection, try again |
| `Access denied for user 'root'@'localhost'` | Wrong MySQL password in `application-local.properties` |
| `Unknown database 'quiz_platform'` | Run the `CREATE DATABASE` command from the installation guide |
| `Communications link failure` | MySQL is not running or not on port 3306 |
| `Port 8080 was already in use` | Stop the other program or add `server.port=8081` to `application-local.properties` |
| Login says invalid credentials for the admin | The admin is created only when the users table is empty, using the values in `application-local.properties`. To start over: `DROP DATABASE quiz_platform;`, create it again, restart |
| Log shows "No users exist and no admin was created" | `app.admin.email` is empty or the password is shorter than 8 characters |
| `release version 21 not supported` | Maven or VS Code uses an older JDK; set JAVA_HOME to JDK 21 |
| Page shows "Whitelabel Error Page" or a Thymeleaf error | Copy the first `Caused by:` line from the console and ask for help; this project has not been run end to end yet |
| Pages look unstyled | Hard refresh (Ctrl+F5); the Bootstrap file is in `static/vendor/bootstrap` |
| Role change does not apply | The user must log out and log in again |
| Tests fail with a database error | Tests use H2; make sure no `application-local.properties` forces another database URL |
