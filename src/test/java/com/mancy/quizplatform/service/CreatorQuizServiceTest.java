package com.mancy.quizplatform.service;

import com.mancy.quizplatform.dto.QuizForm;
import com.mancy.quizplatform.entity.Quiz;
import com.mancy.quizplatform.enums.Difficulty;
import com.mancy.quizplatform.enums.QuizStatus;
import com.mancy.quizplatform.enums.Role;
import com.mancy.quizplatform.exception.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CreatorQuizServiceTest {

    private static final String OWNER = "owner@test.example";

    @Autowired private UserService userService;
    @Autowired private CreatorQuizService creatorService;
    @Autowired private QuizApprovalService approvalService;

    private Quiz quiz;

    @BeforeEach
    void setUp() {
        userService.create("Owner", OWNER, "Password1", Role.CREATOR);
        userService.create("Intruder", "intruder@test.example", "Password1", Role.CREATOR);

        QuizForm form = new QuizForm();
        form.setTitle("My quiz");
        form.setCategory("Java");
        form.setDifficulty(Difficulty.EASY);
        form.setDurationMinutes(10);
        form.setPassingPercent(50);
        quiz = creatorService.createQuiz(form, OWNER);
    }

    private void addQuestions(int count) {
        for (int i = 1; i <= count; i++) {
            creatorService.addQuestion(quiz.getId(),
                    TestData.questionForm("Question " + i, "a" + i, "b" + i, "c" + i, "d" + i), OWNER);
        }
    }

    @Test
    void newQuizStartsAsDraft() {
        assertEquals(QuizStatus.DRAFT, quiz.getStatus());
    }

    @Test
    void quizWithTooFewQuestionsCannotBeSubmitted() {
        addQuestions(CreatorQuizService.MIN_QUESTIONS_TO_SUBMIT - 1);
        assertThrows(BusinessRuleException.class, () -> creatorService.submitForApproval(quiz.getId(), OWNER));
    }

    @Test
    void submittedQuizIsPendingAndLocked() {
        addQuestions(CreatorQuizService.MIN_QUESTIONS_TO_SUBMIT);
        Quiz submitted = creatorService.submitForApproval(quiz.getId(), OWNER);
        assertEquals(QuizStatus.PENDING_APPROVAL, submitted.getStatus());

        QuizForm form = creatorService.toForm(submitted);
        assertThrows(BusinessRuleException.class, () -> creatorService.updateQuiz(quiz.getId(), form, OWNER));
        assertThrows(BusinessRuleException.class, () -> creatorService.addQuestion(quiz.getId(),
                TestData.questionForm("Extra", "1", "2", "3", "4"), OWNER));
    }

    @Test
    void onlyTheAdminDecisionMakesAQuizApproved() {
        addQuestions(CreatorQuizService.MIN_QUESTIONS_TO_SUBMIT);
        creatorService.submitForApproval(quiz.getId(), OWNER);
        assertEquals(QuizStatus.APPROVED, approvalService.approve(quiz.getId()).getStatus());
    }

    @Test
    void rejectionNeedsAReasonAndAllowsEditingAgain() {
        addQuestions(CreatorQuizService.MIN_QUESTIONS_TO_SUBMIT);
        creatorService.submitForApproval(quiz.getId(), OWNER);

        assertThrows(BusinessRuleException.class, () -> approvalService.reject(quiz.getId(), "  "));
        Quiz rejected = approvalService.reject(quiz.getId(), "Add clearer questions");
        assertEquals(QuizStatus.REJECTED, rejected.getStatus());

        creatorService.updateQuiz(quiz.getId(), creatorService.toForm(rejected), OWNER);   // editable again
    }

    @Test
    void anotherCreatorCannotTouchMyQuiz() {
        assertThrows(AccessDeniedException.class, () -> creatorService.getOwnedQuiz(quiz.getId(), "intruder@test.example"));
        assertThrows(AccessDeniedException.class, () -> creatorService.deleteQuiz(quiz.getId(), "intruder@test.example"));
    }

    @Test
    void optionsMustAllBeDifferent() {
        assertThrows(BusinessRuleException.class, () -> creatorService.addQuestion(quiz.getId(),
                TestData.questionForm("Q", "same", "same", "c", "d"), OWNER));
    }

    @Test
    void duplicateQuestionTextIsRejected() {
        creatorService.addQuestion(quiz.getId(), TestData.questionForm("Same text", "a", "b", "c", "d"), OWNER);
        assertThrows(BusinessRuleException.class, () -> creatorService.addQuestion(quiz.getId(),
                TestData.questionForm("same text", "e", "f", "g", "h"), OWNER));
    }
}
