package com.mancy.quizplatform.service;

import com.mancy.quizplatform.dto.AdminStats;
import com.mancy.quizplatform.entity.Attempt;
import com.mancy.quizplatform.enums.AttemptStatus;
import com.mancy.quizplatform.enums.QuizStatus;
import com.mancy.quizplatform.enums.Role;
import com.mancy.quizplatform.repository.AppUserRepository;
import com.mancy.quizplatform.repository.AttemptRepository;
import com.mancy.quizplatform.repository.QuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Platform-wide numbers for the admin dashboard. */
@Service
public class AdminService {

    private final AppUserRepository users;
    private final QuizRepository quizzes;
    private final AttemptRepository attempts;

    public AdminService(AppUserRepository users, QuizRepository quizzes, AttemptRepository attempts) {
        this.users = users;
        this.quizzes = quizzes;
        this.attempts = attempts;
    }

    @Transactional(readOnly = true)
    public AdminStats getStats() {
        AdminStats stats = new AdminStats();
        stats.setTotalUsers(users.count());
        stats.setAdmins(users.countByRole(Role.ADMIN));
        stats.setCreators(users.countByRole(Role.CREATOR));
        stats.setParticipants(users.countByRole(Role.PARTICIPANT));

        stats.setTotalQuizzes(quizzes.count());
        stats.setDraftQuizzes(quizzes.countByStatus(QuizStatus.DRAFT));
        stats.setPendingQuizzes(quizzes.countByStatus(QuizStatus.PENDING_APPROVAL));
        stats.setApprovedQuizzes(quizzes.countByStatus(QuizStatus.APPROVED));
        stats.setRejectedQuizzes(quizzes.countByStatus(QuizStatus.REJECTED));

        // Only finished attempts count: an unfinished attempt has no score yet.
        List<Attempt> finished = attempts.findByStatusNot(AttemptStatus.IN_PROGRESS);
        stats.setTotalAttempts(finished.size());
        double average = finished.stream().mapToDouble(Attempt::getPercentage).average().orElse(0);
        stats.setAverageScore(Math.round(average * 10) / 10.0);
        return stats;
    }
}
