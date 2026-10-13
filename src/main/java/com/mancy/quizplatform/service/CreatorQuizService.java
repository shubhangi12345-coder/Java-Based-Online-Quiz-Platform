package com.mancy.quizplatform.service;

import com.mancy.quizplatform.dto.CreatorStats;
import com.mancy.quizplatform.dto.QuestionForm;
import com.mancy.quizplatform.dto.QuizForm;
import com.mancy.quizplatform.dto.QuizStats;
import com.mancy.quizplatform.entity.AppUser;
import com.mancy.quizplatform.entity.Attempt;
import com.mancy.quizplatform.entity.Feedback;
import com.mancy.quizplatform.entity.Question;
import com.mancy.quizplatform.entity.QuestionOption;
import com.mancy.quizplatform.entity.Quiz;
import com.mancy.quizplatform.enums.AttemptStatus;
import com.mancy.quizplatform.enums.QuizStatus;
import com.mancy.quizplatform.exception.BusinessRuleException;
import com.mancy.quizplatform.exception.ResourceNotFoundException;
import com.mancy.quizplatform.repository.AttemptRepository;
import com.mancy.quizplatform.repository.FeedbackRepository;
import com.mancy.quizplatform.repository.QuizRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Everything a Quiz Creator can do. EVERY method first checks that the quiz belongs to the
 * logged-in creator (getOwnedQuiz), so a creator can never touch someone else's quiz.
 */
@Service
public class CreatorQuizService {

    /** A quiz needs at least this many questions before it can be submitted for approval. */
    public static final int MIN_QUESTIONS_TO_SUBMIT = 3;

    private final QuizRepository quizzes;
    private final AttemptRepository attempts;
    private final FeedbackRepository feedbacks;
    private final UserService userService;
    private final NotificationService notifications;

    public CreatorQuizService(QuizRepository quizzes, AttemptRepository attempts, FeedbackRepository feedbacks,
                              UserService userService, NotificationService notifications) {
        this.quizzes = quizzes;
        this.attempts = attempts;
        this.feedbacks = feedbacks;
        this.userService = userService;
        this.notifications = notifications;
    }

    // ------------------------------------------------------------ reading

    @Transactional(readOnly = true)
    public List<Quiz> listMine(String email) {
        AppUser creator = userService.findByEmail(email);
        return quizzes.findByCreatorIdOrderByCreatedAtDesc(creator.getId());
    }

    /** Loads a quiz and makes sure it belongs to this creator (404 if missing, 403 if someone else's). */
    @Transactional(readOnly = true)
    public Quiz getOwnedQuiz(Long quizId, String email) {
        Quiz quiz = quizzes.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Quiz not found."));
        if (!quiz.getCreator().getEmail().equalsIgnoreCase(email)) {
            throw new AccessDeniedException("You can only manage your own quizzes.");
        }
        return quiz;
    }

    @Transactional(readOnly = true)
    public CreatorStats getDashboardStats(String email) {
        AppUser creator = userService.findByEmail(email);
        Long id = creator.getId();
        CreatorStats stats = new CreatorStats();
        stats.setTotalQuizzes(quizzes.countByCreatorId(id));
        stats.setDrafts(quizzes.countByCreatorIdAndStatus(id, QuizStatus.DRAFT));
        stats.setPending(quizzes.countByCreatorIdAndStatus(id, QuizStatus.PENDING_APPROVAL));
        stats.setApproved(quizzes.countByCreatorIdAndStatus(id, QuizStatus.APPROVED));
        stats.setRejected(quizzes.countByCreatorIdAndStatus(id, QuizStatus.REJECTED));

        List<Attempt> finished = attempts.findByQuizCreatorIdAndStatusNot(id, AttemptStatus.IN_PROGRESS);
        stats.setTotalAttempts(finished.size());
        double average = finished.stream().mapToDouble(Attempt::getPercentage).average().orElse(0);
        stats.setAverageScore(Math.round(average * 10) / 10.0);
        return stats;
    }

    // ------------------------------------------------------------ quiz create / edit / delete

    @Transactional
    public Quiz createQuiz(QuizForm form, String email) {
        Quiz quiz = new Quiz();
        quiz.setCreator(userService.findByEmail(email));
        quiz.setStatus(QuizStatus.DRAFT);
        applyQuizForm(quiz, form);
        return quizzes.save(quiz);
    }

    @Transactional(readOnly = true)
    public QuizForm toForm(Quiz quiz) {
        QuizForm form = new QuizForm();
        form.setTitle(quiz.getTitle());
        form.setDescription(quiz.getDescription());
        form.setCategory(quiz.getCategory());
        form.setDifficulty(quiz.getDifficulty());
        form.setDurationMinutes(quiz.getDurationMinutes());
        form.setPassingPercent(quiz.getPassingPercent());
        return form;
    }

    @Transactional
    public Quiz updateQuiz(Long quizId, QuizForm form, String email) {
        Quiz quiz = getOwnedQuiz(quizId, email);
        requireEditable(quiz);
        applyQuizForm(quiz, form);
        return quizzes.save(quiz);
    }

    /** A quiz can be deleted unless it waits for review or participants have already attempted it. */
    @Transactional
    public void deleteQuiz(Long quizId, String email) {
        Quiz quiz = getOwnedQuiz(quizId, email);
        if (quiz.getStatus() == QuizStatus.PENDING_APPROVAL) {
            throw new BusinessRuleException("This quiz is waiting for admin review and cannot be deleted right now.");
        }
        long attemptCount = attempts.countByQuizId(quizId);
        if (attemptCount > 0) {
            throw new BusinessRuleException("This quiz has " + attemptCount
                    + " participant attempt(s). Deleting it would erase their results, so it cannot be deleted.");
        }
        quizzes.delete(quiz);
    }

    /** DRAFT / REJECTED -> PENDING_APPROVAL. Creators can never approve or publish a quiz themselves. */
    @Transactional
    public Quiz submitForApproval(Long quizId, String email) {
        Quiz quiz = getOwnedQuiz(quizId, email);
        requireEditable(quiz);
        int count = quiz.getQuestions().size();
        if (count < MIN_QUESTIONS_TO_SUBMIT) {
            throw new BusinessRuleException("Add at least " + MIN_QUESTIONS_TO_SUBMIT
                    + " questions before submitting. This quiz has " + count + ".");
        }
        quiz.setStatus(QuizStatus.PENDING_APPROVAL);
        quiz.setRejectionReason(null);
        quizzes.save(quiz);
        notifications.notifyAdmins("Quiz \"" + quiz.getTitle() + "\" was submitted for approval by "
                + quiz.getCreator().getFullName() + ".", "/admin/quizzes/" + quiz.getId());
        return quiz;
    }

    // ------------------------------------------------------------ questions

    @Transactional
    public Question addQuestion(Long quizId, QuestionForm form, String email) {
        Quiz quiz = getOwnedQuiz(quizId, email);
        requireEditable(quiz);
        validateQuestion(quiz, form, null);
        Question question = new Question();
        applyQuestionForm(question, form);
        quiz.addQuestion(question);
        quizzes.save(quiz);   // cascade saves the question and its options
        return question;
    }

    @Transactional(readOnly = true)
    public QuestionForm getQuestionForm(Long quizId, Long questionId, String email) {
        Quiz quiz = getOwnedQuiz(quizId, email);
        requireEditable(quiz);
        Question question = findQuestion(quiz, questionId);
        QuestionForm form = new QuestionForm();
        form.setText(question.getText());
        form.setDifficulty(question.getDifficulty());
        form.setMarks(question.getMarks());
        form.setExplanation(question.getExplanation());
        List<QuestionOption> options = question.getOptions();
        form.setOption1(options.get(0).getText());
        form.setOption2(options.get(1).getText());
        form.setOption3(options.get(2).getText());
        form.setOption4(options.get(3).getText());
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).isCorrect()) {
                form.setCorrectIndex(i);
            }
        }
        return form;
    }

    @Transactional
    public void updateQuestion(Long quizId, Long questionId, QuestionForm form, String email) {
        Quiz quiz = getOwnedQuiz(quizId, email);
        requireEditable(quiz);
        Question question = findQuestion(quiz, questionId);
        validateQuestion(quiz, form, questionId);
        applyQuestionForm(question, form);
        quizzes.save(quiz);
    }

    @Transactional
    public void deleteQuestion(Long quizId, Long questionId, String email) {
        Quiz quiz = getOwnedQuiz(quizId, email);
        requireEditable(quiz);
        Question question = findQuestion(quiz, questionId);
        quiz.getQuestions().remove(question);   // orphanRemoval deletes the question and its options
        quizzes.save(quiz);
    }

    // ------------------------------------------------------------ results for the creator

    @Transactional(readOnly = true)
    public List<Attempt> getFinishedAttempts(Long quizId, String email) {
        getOwnedQuiz(quizId, email);
        return attempts.findByQuizIdAndStatusNotOrderByScoreDesc(quizId, AttemptStatus.IN_PROGRESS);
    }

    @Transactional(readOnly = true)
    public QuizStats getQuizStats(Long quizId, String email) {
        List<Attempt> finished = getFinishedAttempts(quizId, email);
        List<Feedback> feedback = feedbacks.findByQuizIdOrderByCreatedAtDesc(quizId);

        double average = finished.stream().mapToDouble(Attempt::getPercentage).average().orElse(0);
        double highest = finished.stream().mapToDouble(Attempt::getPercentage).max().orElse(0);
        double lowest = finished.stream().mapToDouble(Attempt::getPercentage).min().orElse(0);
        long passed = finished.stream().filter(Attempt::isPassed).count();
        double passRate = finished.isEmpty() ? 0 : passed * 100.0 / finished.size();
        double rating = feedback.stream().mapToInt(Feedback::getRating).average().orElse(0);
        return new QuizStats(finished.size(), round1(average), highest, lowest, round1(passRate),
                feedback.size(), round1(rating));
    }

    @Transactional(readOnly = true)
    public List<Feedback> getFeedback(Long quizId, String email) {
        getOwnedQuiz(quizId, email);
        return feedbacks.findByQuizIdOrderByCreatedAtDesc(quizId);
    }

    // ------------------------------------------------------------ helpers

    private void requireEditable(Quiz quiz) {
        if (!quiz.isEditable()) {
            throw new BusinessRuleException("Only draft or rejected quizzes can be edited. This quiz is: "
                    + quiz.getStatus().getLabel() + ".");
        }
    }

    private Question findQuestion(Quiz quiz, Long questionId) {
        for (Question question : quiz.getQuestions()) {
            if (question.getId().equals(questionId)) {
                return question;
            }
        }
        throw new ResourceNotFoundException("Question not found in this quiz.");
    }

    /** Rules the annotations cannot express: four different options and no duplicate question text. */
    private void validateQuestion(Quiz quiz, QuestionForm form, Long ignoreQuestionId) {
        Set<String> seen = new HashSet<>();   // a Set rejects duplicates
        for (String option : form.optionTexts()) {
            if (!seen.add(option.trim().toLowerCase())) {
                throw new BusinessRuleException("The four options must all be different.");
            }
        }
        String text = form.getText().trim();
        for (Question existing : quiz.getQuestions()) {
            boolean same = existing.getId() != null && existing.getId().equals(ignoreQuestionId);
            if (!same && existing.getText().trim().equalsIgnoreCase(text)) {
                throw new BusinessRuleException("This quiz already has a question with the same text.");
            }
        }
    }

    private void applyQuestionForm(Question question, QuestionForm form) {
        question.setText(form.getText().trim());
        question.setDifficulty(form.getDifficulty());
        question.setMarks(form.getMarks());
        String explanation = form.getExplanation() == null ? "" : form.getExplanation().trim();
        question.setExplanation(explanation.isEmpty() ? null : explanation);

        question.getOptions().clear();   // old options are deleted (orphanRemoval), new ones are inserted
        List<String> texts = form.optionTexts();
        for (int i = 0; i < texts.size(); i++) {
            QuestionOption option = new QuestionOption();
            option.setText(texts.get(i).trim());
            option.setCorrect(i == form.getCorrectIndex());
            option.setSortOrder(i);
            question.addOption(option);
        }
    }

    private void applyQuizForm(Quiz quiz, QuizForm form) {
        quiz.setTitle(form.getTitle().trim());
        String description = form.getDescription() == null ? "" : form.getDescription().trim();
        quiz.setDescription(description.isEmpty() ? null : description);
        quiz.setCategory(form.getCategory().trim());
        quiz.setDifficulty(form.getDifficulty());
        quiz.setDurationMinutes(form.getDurationMinutes());
        quiz.setPassingPercent(form.getPassingPercent());
    }

    private double round1(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
