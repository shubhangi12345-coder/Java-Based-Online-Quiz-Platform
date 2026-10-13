package com.mancy.quizplatform.service;

import com.mancy.quizplatform.dto.ParticipantStats;
import com.mancy.quizplatform.entity.AppUser;
import com.mancy.quizplatform.entity.Attempt;
import com.mancy.quizplatform.enums.Difficulty;
import com.mancy.quizplatform.util.AdaptiveRules;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Numbers for the participant dashboard, plus the rule-based difficulty recommendation:
 * the average of the last 3 finished attempts decides whether to suggest an easier, same or harder quiz.
 */
@Service
public class ParticipantDashboardService {

    private static final int RECENT_ATTEMPTS = 3;

    private final AttemptService attemptService;
    private final UserService userService;
    private final LeaderboardService leaderboard;

    public ParticipantDashboardService(AttemptService attemptService, UserService userService,
                                       LeaderboardService leaderboard) {
        this.attemptService = attemptService;
        this.userService = userService;
        this.leaderboard = leaderboard;
    }

    @Transactional(readOnly = true)
    public ParticipantStats getStats(String email) {
        AppUser user = userService.findByEmail(email);
        List<Attempt> finished = new ArrayList<>();
        for (Attempt attempt : attemptService.history(email)) {   // newest first
            if (attempt.isFinished()) {
                finished.add(attempt);
            }
        }

        ParticipantStats stats = new ParticipantStats();
        stats.setAttempts(finished.size());
        double average = finished.stream().mapToDouble(Attempt::getPercentage).average().orElse(0);
        stats.setAverageScore(Math.round(average * 10) / 10.0);
        stats.setBestScore(finished.stream().mapToDouble(Attempt::getPercentage).max().orElse(0));
        stats.setPassedCount(finished.stream().filter(Attempt::isPassed).count());
        stats.setRank(leaderboard.rankOf(user.getId()));

        if (finished.isEmpty()) {
            stats.setRecommendedDifficulty(Difficulty.EASY);
            stats.setRecommendationReason("You have not finished a quiz yet. Start with an EASY quiz.");
        } else {
            List<Attempt> recent = finished.subList(0, Math.min(RECENT_ATTEMPTS, finished.size()));
            double recentAverage = recent.stream().mapToDouble(Attempt::getPercentage).average().orElse(0);
            Difficulty current = recent.get(0).getQuiz().getDifficulty();   // level of the latest quiz
            Difficulty next = AdaptiveRules.next(current, recentAverage);
            stats.setRecommendedDifficulty(next);
            stats.setRecommendationReason(AdaptiveRules.explain(current, recentAverage, next));
        }
        return stats;
    }
}
