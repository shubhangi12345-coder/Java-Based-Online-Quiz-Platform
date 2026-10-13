# Installation guide (Windows / macOS / Linux)

## 1. Install
1. **JDK 21**, for example Eclipse Temurin 21. Check: `java -version` shows 21.
2. **MySQL 8** (Community Server). Remember the `root` password you set. MySQL Workbench is optional but useful.
3. **VS Code** and the *Extension Pack for Java*.

## 2. Open the project
Extract the ZIP, then in VS Code use **File -> Open Folder** and choose `java-online-quiz-platform` (the folder that contains `pom.xml`).
Wait until VS Code finishes importing the Maven project (bottom-right progress).

## 3. Create the database
```sql
CREATE DATABASE quiz_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## 4. Local settings
Copy `application-local.properties.example` to `application-local.properties` (same folder) and replace every `CHANGE_ME`:
your MySQL password, the admin password you want (8+ characters, a letter and a number) and optionally the demo settings.
This file is ignored by Git.

## 5. Run
In the VS Code terminal (Terminal -> New Terminal):
```
./mvnw spring-boot:run            (Windows PowerShell: .\mvnw.cmd spring-boot:run)
```
The first run downloads Maven and the libraries (a few minutes, internet needed). When you see
`Started OnlineQuizPlatformApplication`, open http://localhost:8080 and log in with the admin email and password you chose.

If something fails see [TROUBLESHOOTING.md](TROUBLESHOOTING.md).
