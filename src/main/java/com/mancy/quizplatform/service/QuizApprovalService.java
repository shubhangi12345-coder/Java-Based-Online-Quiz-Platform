package com.mancy.quizplatform.service;

import com.mancy.quizplatform.entity.Quiz;
import com.mancy.quizplatform.enums.QuizStatus;
import com.mancy.quizplatform.exception.BusinessRuleException;
import com.mancy.quizplatform.exception.ResourceNotFoundException;
import com.mancy.quizplatform.repository.QuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** The admin's side of the quiz life cycle: list, view, approve, reject. */
@Service
public class QuizApprovalService {

    private final QuizRepository quizzes;
    private final NotificationService notifications;

    public QuizApprovalService(QuizRepository quizzes, NotificationService notifications) {
        this.quizzes = quizzes;
        this.notifications = notifications;
    }

    @Transactional(readOnly = true)
    public List<Quiz> list(QuizStatus status) {
        return status == null ? quizzes.findAllByOrderByCreatedAtDesc() : quizzes.findByStatusOrderByCreatedAtDesc(status);
    }

    @Transactional(readOnly = true)
    public Quiz get(Long id) {
        return quizzes.findById(id).orElseThrow(() -> new ResourceNotFoundException("Quiz not found."));
    }

    @Transactional
    public Quiz approve(Long id) {
        Quiz quiz = get(id);
        requirePending(quiz);
        if (quiz.getQuestions().isEmpty()) {
            throw new BusinessRuleException("A quiz without questions cannot be approved.");
        }
        quiz.setStatus(QuizStatus.APPROVED);
        quiz.setRejectionReason(null);
        quizzes.save(quiz);
        notifications.notifyUser(quiz.getCreator(),
                "Your quiz \"" + quiz.getTitle() + "\" was approved and is now visible to participants.",
                "/creator/quizzes/" + quiz.getId());
        return quiz;
    }

    @Transactional
    public Quiz reject(Long id, String reason) {
        Quiz quiz = get(id);
        requirePending(quiz);
        String cleanReason = reason == null ? "" : reason.trim();
        if (cleanReason.isEmpty()) {
            throw new BusinessRuleException("Please give a reason for rejecting the quiz.");
        }
        if (cleanReason.length() > 500) {
            throw new BusinessRuleException("The reason must be at most 500 characters.");
        }
        quiz.setStatus(QuizStatus.REJECTED);
        quiz.setRejectionReason(cleanReason);
        quizzes.save(quiz);
        notifications.notifyUser(quiz.getCreator(),
                "Your quiz \"" + quiz.getTitle() + "\" was rejected: " + cleanReason,
                "/creator/quizzes/" + quiz.getId());
        return quiz;
    }

    private void requirePending(Quiz quiz) {
        if (quiz.getStatus() != QuizStatus.PENDING_APPROVAL) {
            throw new BusinessRuleException("Only quizzes that are pending approval can be approved or rejected. "
                    + "This quiz is: " + quiz.getStatus().getLabel() + ".");
        }
    }
}
