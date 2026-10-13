package com.mancy.quizplatform.service;

import com.mancy.quizplatform.entity.AppUser;
import com.mancy.quizplatform.entity.Attempt;
import com.mancy.quizplatform.entity.Question;
import com.mancy.quizplatform.entity.Quiz;
import com.mancy.quizplatform.enums.AttemptStatus;
import com.mancy.quizplatform.enums.Difficulty;
import com.mancy.quizplatform.enums.QuizStatus;
import com.mancy.quizplatform.enums.Role;
import com.mancy.quizplatform.exception.ResourceNotFoundException;
import com.mancy.quizplatform.repository.AttemptRepository;
import com.mancy.quizplatform.repository.QuizRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests quiz attempts end to end against an in-memory H2 database. Every test is rolled back afterwards. */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AttemptServiceTest {

    @Autowired private UserService userService;
    @Autowired private QuizRepository quizzes;
    @Autowired private AttemptRepository attempts;
    @Autowired private AttemptService attemptService;

    private AppUser student;
    private Quiz quiz;
    private Question q1;   // 2 marks
    private Question q2;   // 3 marks

    @BeforeEach
    void setUp() {
        AppUser creator = userService.create("Creator", "creator@test.example", "Password1", Role.CREATOR);
        student = userService.create("Student", "student@test.example", "Password1", Role.PARTICIPANT);

        quiz = new Quiz();
        quiz.setTitle("Test quiz");
        quiz.setCategory("Testing");
        quiz.setDifficulty(Difficulty.EASY);
        quiz.setDurationMinutes(10);
        quiz.setPassingPercent(40);
        quiz.setStatus(QuizStatus.APPROVED);
        quiz.setCreator(creator);
        q1 = TestData.question("Question one", 2, 0);
        q2 = TestData.question("Question two", 3, 1);
        quiz.addQuestion(q1);
        quiz.addQuestion(q2);
        quiz = quizzes.saveAndFlush(quiz);
    }

    private Map<Long, Long> answers(Long first, Long second) {
        Map<Long, Long> map = new HashMap<>();
        map.put(q1.getId(), first);
        map.put(q2.getId(), second);
        return map;
    }

    @Test
    void allCorrectAnswersGiveFullMarks() {
        Attempt attempt = attemptService.start(quiz.getId(), student.getEmail());
        Attempt graded = attemptService.submit(attempt.getId(), student.getEmail(),
                answers(TestData.optionId(q1, true), TestData.optionId(q2, true)));

        assertEquals(5, graded.getScore());
        assertEquals(5, graded.getTotalMarks());
        assertEquals(100.0, graded.getPercentage());
        assertTrue(graded.isPassed());
        assertEquals(AttemptStatus.SUBMITTED, graded.getStatus());
    }

    @Test
    void wrongAndMissingAnswersEarnNothing() {
        Attempt attempt = attemptService.start(quiz.getId(), student.getEmail());
        Map<Long, Long> selections = new HashMap<>();
        selections.put(q1.getId(), TestData.optionId(q1, false));   // wrong, and q2 is left unanswered
        Attempt graded = attemptService.submit(attempt.getId(), student.getEmail(), selections);

        assertEquals(0, graded.getScore());
        assertFalse(graded.isPassed());
    }

    @Test
    void partialScoreUsesQuestionMarks() {
        Attempt attempt = attemptService.start(quiz.getId(), student.getEmail());
        Attempt graded = attemptService.submit(attempt.getId(), student.getEmail(),
                answers(TestData.optionId(q1, true), TestData.optionId(q2, false)));

        assertEquals(2, graded.getScore());          // only the 2-mark question is right
        assertEquals(40.0, graded.getPercentage());
        assertTrue(graded.isPassed());               // 40% meets the 40% passing mark
    }

    @Test
    void anOptionFromAnotherQuestionIsNotAccepted() {
        Attempt attempt = attemptService.start(quiz.getId(), student.getEmail());
        // the browser claims the correct option of q2 as the answer to q1
        Attempt graded = attemptService.submit(attempt.getId(), student.getEmail(),
                answers(TestData.optionId(q2, true), null));

        assertEquals(0, graded.getScore());
    }

    @Test
    void submitAfterTheDeadlineIsGradedWithoutAnswers() {
        Attempt attempt = attemptService.start(quiz.getId(), student.getEmail());
        attempt.setDeadline(LocalDateTime.now().minusMinutes(5));
        attempts.saveAndFlush(attempt);

        Attempt graded = attemptService.submit(attempt.getId(), student.getEmail(),
                answers(TestData.optionId(q1, true), TestData.optionId(q2, true)));

        assertEquals(0, graded.getScore());
        assertEquals(AttemptStatus.AUTO_SUBMITTED, graded.getStatus());
    }

    @Test
    void submittingTwiceDoesNotGradeTwice() {
        Attempt attempt = attemptService.start(quiz.getId(), student.getEmail());
        Map<Long, Long> good = answers(TestData.optionId(q1, true), TestData.optionId(q2, true));
        attemptService.submit(attempt.getId(), student.getEmail(), good);
        Attempt second = attemptService.submit(attempt.getId(), student.getEmail(), answers(null, null));

        assertEquals(5, second.getScore());   // the first grading stays
        assertEquals(2, second.getAnswers().size());
    }

    @Test
    void startingTwiceResumesTheSameAttempt() {
        Attempt first = attemptService.start(quiz.getId(), student.getEmail());
        Attempt second = attemptService.start(quiz.getId(), student.getEmail());
        assertEquals(first.getId(), second.getId());
    }

    @Test
    void anotherParticipantCannotUseMyAttempt() {
        Attempt attempt = attemptService.start(quiz.getId(), student.getEmail());
        userService.create("Other", "other@test.example", "Password1", Role.PARTICIPANT);

        assertThrows(AccessDeniedException.class,
                () -> attemptService.submit(attempt.getId(), "other@test.example", answers(null, null)));
        assertThrows(AccessDeniedException.class,
                () -> attemptService.getFinishedAttempt(attempt.getId(), "other@test.example"));
    }

    @Test
    void unapprovedQuizzesCannotBeStarted() {
        quiz.setStatus(QuizStatus.DRAFT);
        quizzes.saveAndFlush(quiz);
        assertThrows(ResourceNotFoundException.class, () -> attemptService.start(quiz.getId(), student.getEmail()));
    }
}
