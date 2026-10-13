package com.mancy.quizplatform.configuration;

import com.mancy.quizplatform.entity.AppUser;
import com.mancy.quizplatform.entity.Question;
import com.mancy.quizplatform.entity.QuestionOption;
import com.mancy.quizplatform.entity.Quiz;
import com.mancy.quizplatform.enums.Difficulty;
import com.mancy.quizplatform.enums.QuizStatus;
import com.mancy.quizplatform.enums.Role;
import com.mancy.quizplatform.repository.AppUserRepository;
import com.mancy.quizplatform.repository.QuizRepository;
import com.mancy.quizplatform.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Runs once at start-up.
 *  1. Creates the FIRST ADMIN if the database has no users. The email and password come from
 *     application-local.properties (or environment variables), never from the source code.
 *  2. Optionally creates demo data (app.demo.enabled=true) so you can try the app quickly.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserService userService;
    private final AppUserRepository users;
    private final QuizRepository quizzes;

    @Value("${app.admin.name:Administrator}")
    private String adminName;
    @Value("${app.admin.email:}")
    private String adminEmail;
    @Value("${app.admin.password:}")
    private String adminPassword;
    @Value("${app.demo.enabled:false}")
    private boolean demoEnabled;
    @Value("${app.demo.password:}")
    private String demoPassword;

    public DataInitializer(UserService userService, AppUserRepository users, QuizRepository quizzes) {
        this.userService = userService;
        this.users = users;
        this.quizzes = quizzes;
    }

    @Override
    public void run(ApplicationArguments args) {
        createFirstAdmin();
        if (demoEnabled) {
            createDemoData();
        }
    }

    private void createFirstAdmin() {
        if (users.count() > 0) {
            return;
        }
        if (adminEmail.isBlank() || adminPassword.length() < 8) {
            log.warn("No users exist and no admin was created. Set app.admin.email and app.admin.password "
                    + "(at least 8 characters) in application-local.properties, then restart.");
            return;
        }
        userService.create(adminName, adminEmail, adminPassword, Role.ADMIN);
        log.info("First admin account created for {}", adminEmail);
    }

    private void createDemoData() {
        if (demoPassword.length() < 8) {
            log.warn("Demo data skipped: app.demo.password must be at least 8 characters.");
            return;
        }
        if (users.existsByEmail("demo.creator@example.com")) {
            return;   // already created on an earlier start
        }
        AppUser creator = userService.create("Demo Creator", "demo.creator@example.com", demoPassword, Role.CREATOR);
        userService.create("Demo Student One", "demo.student1@example.com", demoPassword, Role.PARTICIPANT);
        userService.create("Demo Student Two", "demo.student2@example.com", demoPassword, Role.PARTICIPANT);

        Quiz quiz = new Quiz();
        quiz.setTitle("Java Basics (demo)");
        quiz.setDescription("A short demo quiz about core Java.");
        quiz.setCategory("Java Basics");
        quiz.setDifficulty(Difficulty.EASY);
        quiz.setDurationMinutes(5);
        quiz.setPassingPercent(40);
        quiz.setStatus(QuizStatus.APPROVED);
        quiz.setCreator(creator);

        addQuestion(quiz, "Which keyword creates a new object in Java?", Difficulty.EASY, 1,
                "new", "create", "make", "object", 0, "The 'new' keyword allocates a new object.");
        addQuestion(quiz, "Which method is the entry point of a Java program?", Difficulty.EASY, 1,
                "start()", "main()", "run()", "init()", 1, "The JVM starts execution at main().");
        addQuestion(quiz, "Which type stores true or false?", Difficulty.EASY, 2,
                "int", "String", "boolean", "char", 2, "boolean holds exactly true or false.");
        quizzes.save(quiz);
        log.info("Demo data created (demo.creator@example.com, demo.student1@example.com, demo.student2@example.com).");
    }

    private void addQuestion(Quiz quiz, String text, Difficulty difficulty, int marks,
                             String a, String b, String c, String d, int correctIndex, String explanation) {
        Question question = new Question();
        question.setText(text);
        question.setDifficulty(difficulty);
        question.setMarks(marks);
        question.setExplanation(explanation);
        String[] texts = {a, b, c, d};
        for (int i = 0; i < texts.length; i++) {
            QuestionOption option = new QuestionOption();
            option.setText(texts[i]);
            option.setCorrect(i == correctIndex);
            option.setSortOrder(i);
            question.addOption(option);
        }
        quiz.addQuestion(question);
    }
}
