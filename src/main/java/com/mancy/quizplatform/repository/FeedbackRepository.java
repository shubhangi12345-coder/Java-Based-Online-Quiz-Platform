package com.mancy.quizplatform.repository;

import com.mancy.quizplatform.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findByQuizIdOrderByCreatedAtDesc(Long quizId);

    Optional<Feedback> findByQuizIdAndParticipantId(Long quizId, Long participantId);
}
