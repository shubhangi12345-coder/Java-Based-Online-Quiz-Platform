package com.mancy.quizplatform.service;

import com.mancy.quizplatform.entity.Quiz;
import com.mancy.quizplatform.enums.Difficulty;
import com.mancy.quizplatform.enums.QuizStatus;
import com.mancy.quizplatform.exception.ResourceNotFoundException;
import com.mancy.quizplatform.repository.QuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/** What participants may see: ONLY approved quizzes. Anything else looks like it does not exist. */
@Service
public class ParticipantQuizService {

    private final QuizRepository quizzes;

    public ParticipantQuizService(QuizRepository quizzes) {
        this.quizzes = quizzes;
    }

    /** Approved quizzes that have questions, filtered by a search word and/or difficulty (both optional). */
    @Transactional(readOnly = true)
    public List<Quiz> browse(String keyword, Difficulty difficulty) {
        String word = keyword == null ? "" : keyword.trim().toLowerCase();
        List<Quiz> result = new ArrayList<>();
        for (Quiz quiz : quizzes.findByStatusOrderByCreatedAtDesc(QuizStatus.APPROVED)) {
            if (quiz.getQuestions().isEmpty()) {
                continue;
            }
            if (difficulty != null && quiz.getDifficulty() != difficulty) {
                continue;
            }
            if (!word.isEmpty() && !matches(quiz, word)) {
                continue;
            }
            result.add(quiz);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Quiz getApprovedQuiz(Long id) {
        return quizzes.findById(id)
                .filter(quiz -> quiz.getStatus() == QuizStatus.APPROVED)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found."));
    }

    private boolean matches(Quiz quiz, String word) {
        return quiz.getTitle().toLowerCase().contains(word)
                || quiz.getCategory().toLowerCase().contains(word)
                || (quiz.getDescription() != null && quiz.getDescription().toLowerCase().contains(word));
    }
}
