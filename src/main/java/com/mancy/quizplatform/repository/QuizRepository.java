package com.mancy.quizplatform.repository;

import com.mancy.quizplatform.entity.Quiz;
import com.mancy.quizplatform.enums.QuizStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    List<Quiz> findAllByOrderByCreatedAtDesc();

    List<Quiz> findByStatusOrderByCreatedAtDesc(QuizStatus status);

    List<Quiz> findByCreatorIdOrderByCreatedAtDesc(Long creatorId);

    long countByStatus(QuizStatus status);

    long countByCreatorId(Long creatorId);

    long countByCreatorIdAndStatus(Long creatorId, QuizStatus status);
}
