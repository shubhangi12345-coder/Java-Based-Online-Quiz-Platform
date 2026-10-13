package com.mancy.quizplatform.service;

import com.mancy.quizplatform.dto.LeaderboardRow;
import com.mancy.quizplatform.entity.AppUser;
import com.mancy.quizplatform.entity.Attempt;
import com.mancy.quizplatform.enums.AttemptStatus;
import com.mancy.quizplatform.repository.AttemptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the leaderboard from FINISHED attempts of enabled participants.
 * Order: higher average score, then higher best score, then more attempts.
 * Participants equal on all three share a rank (1, 2, 2, 4).
 */
@Service
public class LeaderboardService {

    private final AttemptRepository attempts;

    public LeaderboardService(AttemptRepository attempts) {
        this.attempts = attempts;
    }

    @Transactional(readOnly = true)
    public List<LeaderboardRow> getRanking() {
        Map<Long, List<Attempt>> byParticipant = new LinkedHashMap<>();
        for (Attempt attempt : attempts.findByStatusNot(AttemptStatus.IN_PROGRESS)) {
            AppUser participant = attempt.getParticipant();
            if (participant.isEnabled()) {
                byParticipant.computeIfAbsent(participant.getId(), id -> new ArrayList<>()).add(attempt);
            }
        }

        List<LeaderboardRow> rows = new ArrayList<>();
        for (List<Attempt> list : byParticipant.values()) {
            double average = list.stream().mapToDouble(Attempt::getPercentage).average().orElse(0);
            double best = list.stream().mapToDouble(Attempt::getPercentage).max().orElse(0);
            AppUser participant = list.get(0).getParticipant();
            rows.add(new LeaderboardRow(0, participant.getId(), participant.getFullName(), list.size(),
                    Math.round(average * 10) / 10.0, best));
        }

        rows.sort(Comparator.comparingDouble(LeaderboardRow::getAverage).reversed()
                .thenComparing(Comparator.comparingDouble(LeaderboardRow::getBest).reversed())
                .thenComparing(Comparator.comparingLong(LeaderboardRow::getAttempts).reversed())
                .thenComparing(LeaderboardRow::getName, String.CASE_INSENSITIVE_ORDER));

        LeaderboardRow previous = null;
        int rank = 0;
        for (int i = 0; i < rows.size(); i++) {
            LeaderboardRow row = rows.get(i);
            boolean tied = previous != null
                    && row.getAverage() == previous.getAverage()
                    && row.getBest() == previous.getBest()
                    && row.getAttempts() == previous.getAttempts();
            if (!tied) {
                rank = i + 1;
            }
            row.setRank(rank);
            previous = row;
        }
        return rows;
    }

    /** The rank of one participant, or null if they have no finished attempt yet. */
    public Integer rankOf(Long userId) {
        for (LeaderboardRow row : getRanking()) {
            if (row.getUserId().equals(userId)) {
                return row.getRank();
            }
        }
        return null;
    }
}
