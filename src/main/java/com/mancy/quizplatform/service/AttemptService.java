package com.mancy.quizplatform.service;

import com.mancy.quizplatform.entity.AppUser;
import com.mancy.quizplatform.entity.Attempt;
import com.mancy.quizplatform.entity.AttemptAnswer;
import com.mancy.quizplatform.entity.Question;
import com.mancy.quizplatform.entity.QuestionOption;
import com.mancy.quizplatform.entity.Quiz;
import com.mancy.quizplatform.enums.AttemptStatus;
import com.mancy.quizplatform.enums.QuizStatus;
import com.mancy.quizplatform.exception.BusinessRuleException;
import com.mancy.quizplatform.exception.ResourceNotFoundException;
import com.mancy.quizplatform.repository.AppUserRepository;
import com.mancy.quizplatform.repository.AttemptRepository;
import com.mancy.quizplatform.repository.QuizRepository;
import com.mancy.quizplatform.util.ScoreCalculator;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Start, submit and grade quiz attempts. Rules enforced HERE on the server (never trusted to the browser):
 *  1. only APPROVED quizzes can be started
 *  2. an attempt can only be used by the participant who owns it
 *  3. the deadline (start + duration) is stored in the database and checked on every request
 *  4. the SCORE is calculated here from the stored correct options; the browser only sends option ids
 *  5. grading happens exactly once (row lock + status check), so double submits are harmless
 */
@Service
public class AttemptService {

    /** Seconds after the deadline in which a submit from the browser timer is still accepted (network delay). */
    public static final int GRACE_SECONDS = 20;

    private final AttemptRepository attempts;
    private final QuizRepository quizzes;
    private final AppUserRepository users;

    public AttemptService(AttemptRepository attempts, QuizRepository quizzes, AppUserRepository users) {
        this.attempts = attempts;
        this.quizzes = quizzes;
        this.users = users;
    }

    // ------------------------------------------------------------ start

    /**
     * Starts the quiz and returns the attempt. If the participant already has a running attempt
     * on this quiz, that one is returned instead (so refreshing or double clicking never makes two).
     */
    @Transactional
    public Attempt start(Long quizId, String email) {
        // lock the participant's row: a parallel "start" request waits here and then finds our attempt
        AppUser participant = users.findByEmailForUpdate(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        if (!participant.isEnabled()) {
            throw new AccessDeniedException("Your account is disabled.");
        }

        Quiz quiz = quizzes.findById(quizId)
                .filter(q -> q.getStatus() == QuizStatus.APPROVED)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found."));
        if (quiz.getQuestions().isEmpty()) {
            throw new BusinessRuleException("This quiz has no questions yet.");
        }

        LocalDateTime now = LocalDateTime.now();
        Attempt running = attempts.findFirstByParticipantIdAndQuizIdAndStatus(
                participant.getId(), quizId, AttemptStatus.IN_PROGRESS).orElse(null);
        if (running != null) {
            if (!now.isAfter(running.getDeadline())) {
                return running;                       // still running: resume it
            }
            grade(running, Map.of(), now);            // time ran out earlier: close it, then start a new one
        }

        Attempt attempt = new Attempt();
        attempt.setParticipant(participant);
        attempt.setQuiz(quiz);
        attempt.setStartedAt(now);
        attempt.setDeadline(now.plusMinutes(quiz.getDurationMinutes()));
        attempt.setStatus(AttemptStatus.IN_PROGRESS);
        attempt.setTotalMarks(quiz.getTotalMarks());
        return attempts.save(attempt);
    }

    // ------------------------------------------------------------ taking the quiz

    /**
     * Returns the attempt for the quiz-taking page. If its time has already run out, it is closed
     * (graded with no answers) and the returned attempt is finished: the controller then shows the result.
     */
    @Transactional
    public Attempt openForTaking(Long attemptId, String email) {
        Attempt attempt = loadOwned(attemptId, email, true);
        if (attempt.getStatus() == AttemptStatus.IN_PROGRESS && LocalDateTime.now().isAfter(attempt.getDeadline())) {
            grade(attempt, Map.of(), LocalDateTime.now());
        }
        return attempt;
    }

    /**
     * Final submit. selections = question id -> chosen option id (as sent by the browser).
     * Late submits (more than GRACE_SECONDS after the deadline) are graded with NO answers.
     */
    @Transactional
    public Attempt submit(Long attemptId, String email, Map<Long, Long> selections) {
        Attempt attempt = loadOwned(attemptId, email, true);
        if (attempt.isFinished()) {
            return attempt;                           // already graded: do nothing (double submit)
        }
        LocalDateTime now = LocalDateTime.now();
        boolean tooLate = now.isAfter(attempt.getDeadline().plusSeconds(GRACE_SECONDS));
        return grade(attempt, tooLate ? Map.of() : selections, now);
    }

    /** Grades the attempt once. The caller must already hold the row lock. */
    private Attempt grade(Attempt attempt, Map<Long, Long> selections, LocalDateTime now) {
        int score = 0;
        int total = 0;
        List<AttemptAnswer> answers = new ArrayList<>();

        for (Question question : attempt.getQuiz().getQuestions()) {
            total += question.getMarks();

            // Only an option that really belongs to THIS question is accepted
            QuestionOption chosen = null;
            Long chosenId = selections.get(question.getId());
            if (chosenId != null) {
                for (QuestionOption option : question.getOptions()) {
                    if (option.getId().equals(chosenId)) {
                        chosen = option;
                    }
                }
            }
            boolean correct = chosen != null && chosen.isCorrect();
            if (correct) {
                score += question.getMarks();
            }

            AttemptAnswer answer = new AttemptAnswer();
            answer.setAttempt(attempt);
            answer.setQuestion(question);
            answer.setSelectedOption(chosen);
            answer.setCorrect(correct);
            answers.add(answer);
        }

        double percentage = ScoreCalculator.percentage(score, total);
        attempt.getAnswers().addAll(answers);
        attempt.setScore(score);
        attempt.setTotalMarks(total);
        attempt.setPercentage(percentage);
        attempt.setPassed(ScoreCalculator.isPassed(percentage, attempt.getQuiz().getPassingPercent()));
        attempt.setSubmittedAt(now.isAfter(attempt.getDeadline()) ? attempt.getDeadline() : now);
        attempt.setStatus(now.isAfter(attempt.getDeadline()) ? AttemptStatus.AUTO_SUBMITTED : AttemptStatus.SUBMITTED);
        return attempts.save(attempt);
    }

    // ------------------------------------------------------------ results and history

    /** The result page. Correct answers are only revealed AFTER the attempt is finished. */
    @Transactional(readOnly = true)
    public Attempt getFinishedAttempt(Long attemptId, String email) {
        Attempt attempt = loadOwned(attemptId, email, false);
        if (!attempt.isFinished()) {
            throw new BusinessRuleException("This attempt is still in progress.");
        }
        return attempt;
    }

    @Transactional(readOnly = true)
    public List<Attempt> history(String email) {
        AppUser user = users.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found."));
        return attempts.findByParticipantIdOrderByStartedAtDesc(user.getId());
    }

    @Transactional(readOnly = true)
    public boolean hasFinishedQuiz(String email, Long quizId) {
        return users.findByEmail(email)
                .map(user -> attempts.existsByParticipantIdAndQuizIdAndStatusNot(
                        user.getId(), quizId, AttemptStatus.IN_PROGRESS))
                .orElse(false);
    }

    // ------------------------------------------------------------ used by the background sweeper

    @Transactional(readOnly = true)
    public List<Long> findExpiredAttemptIds() {
        LocalDateTime limit = LocalDateTime.now().minusSeconds(GRACE_SECONDS);
        List<Long> ids = new ArrayList<>();
        for (Attempt attempt : attempts.findByStatusAndDeadlineBefore(AttemptStatus.IN_PROGRESS, limit)) {
            ids.add(attempt.getId());
        }
        return ids;
    }

    @Transactional
    public void closeExpiredAttempt(Long attemptId) {
        attempts.findByIdForUpdate(attemptId).ifPresent(attempt -> {
            if (attempt.getStatus() == AttemptStatus.IN_PROGRESS) {
                grade(attempt, Map.of(), LocalDateTime.now());
            }
        });
    }

    // ------------------------------------------------------------ helpers

    private Attempt loadOwned(Long attemptId, String email, boolean lock) {
        Attempt attempt = (lock ? attempts.findByIdForUpdate(attemptId) : attempts.findById(attemptId))
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found."));
        if (!attempt.getParticipant().getEmail().equalsIgnoreCase(email)) {
            throw new AccessDeniedException("You can only open your own quiz attempts.");
        }
        return attempt;
    }
}
