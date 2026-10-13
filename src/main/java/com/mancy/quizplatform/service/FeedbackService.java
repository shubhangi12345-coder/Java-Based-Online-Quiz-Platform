package com.mancy.quizplatform.service;

import com.mancy.quizplatform.dto.FeedbackForm;
import com.mancy.quizplatform.entity.AppUser;
import com.mancy.quizplatform.entity.Feedback;
import com.mancy.quizplatform.entity.Quiz;
import com.mancy.quizplatform.exception.BusinessRuleException;
import com.mancy.quizplatform.repository.FeedbackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/** A participant can rate a quiz and leave a comment, but only after finishing it at least once. */
@Service
public class FeedbackService {

    private final FeedbackRepository feedbacks;
    private final UserService userService;
    private final ParticipantQuizService quizService;
    private final AttemptService attemptService;
    private final NotificationService notifications;

    public FeedbackService(FeedbackRepository feedbacks, UserService userService, ParticipantQuizService quizService,
                           AttemptService attemptService, NotificationService notifications) {
        this.feedbacks = feedbacks;
        this.userService = userService;
        this.quizService = quizService;
        this.attemptService = attemptService;
        this.notifications = notifications;
    }

    @Transactional
    public Feedback submit(Long quizId, String email, FeedbackForm form) {
        Quiz quiz = quizService.getApprovedQuiz(quizId);
        if (!attemptService.hasFinishedQuiz(email, quizId)) {
            throw new BusinessRuleException("Finish the quiz at least once before giving feedback.");
        }
        AppUser participant = userService.findByEmail(email);

        // one feedback per participant per quiz: a second submit updates the first
        Optional<Feedback> existing = feedbacks.findByQuizIdAndParticipantId(quizId, participant.getId());
        Feedback feedback = existing.orElseGet(Feedback::new);
        feedback.setQuiz(quiz);
        feedback.setParticipant(participant);
        feedback.setRating(form.getRating());
        String comment = form.getComment() == null ? "" : form.getComment().trim();
        feedback.setComment(comment.isEmpty() ? null : comment);
        Feedback saved = feedbacks.save(feedback);

        if (existing.isEmpty()) {
            notifications.notifyUser(quiz.getCreator(),
                    participant.getFullName() + " rated your quiz \"" + quiz.getTitle() + "\" "
                            + form.getRating() + "/5.", "/creator/quizzes/" + quiz.getId() + "/results");
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<Feedback> findMine(Long quizId, String email) {
        return userService.findOptionalByEmail(email)
                .flatMap(user -> feedbacks.findByQuizIdAndParticipantId(quizId, user.getId()));
    }
}
