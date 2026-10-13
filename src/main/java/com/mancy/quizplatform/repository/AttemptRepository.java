package com.mancy.quizplatform.repository;

import com.mancy.quizplatform.entity.Attempt;
import com.mancy.quizplatform.enums.AttemptStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {

    List<Attempt> findByParticipantIdOrderByStartedAtDesc(Long participantId);

    List<Attempt> findByQuizIdAndStatusNotOrderByScoreDesc(Long quizId, AttemptStatus status);

    List<Attempt> findByStatusNot(AttemptStatus status);

    List<Attempt> findByQuizCreatorIdAndStatusNot(Long creatorId, AttemptStatus status);

    Optional<Attempt> findFirstByParticipantIdAndQuizIdAndStatus(Long participantId, Long quizId, AttemptStatus status);

    long countByQuizId(Long quizId);

    boolean existsByParticipantIdAndQuizIdAndStatusNot(Long participantId, Long quizId, AttemptStatus status);

    List<Attempt> findByStatusAndDeadlineBefore(AttemptStatus status, LocalDateTime time);

    // Loads one attempt and locks its row, so submit / auto-submit cannot grade it twice at the same time.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Attempt a where a.id = :id")
    Optional<Attempt> findByIdForUpdate(@Param("id") Long id);
}
