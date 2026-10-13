package com.mancy.quizplatform.util;

import com.mancy.quizplatform.enums.Difficulty;

/**
 * RULE-BASED ADAPTIVE DIFFICULTY (a recommendation, not machine learning).
 *
 * The participant's recent average score decides which difficulty to suggest next:
 *   average >= 80%      -> one level harder
 *   average 50% - 79%   -> same level
 *   average <  50%      -> one level easier
 *
 * These are plain if/else rules. Nothing is trained and nothing is learned from data.
 */
public final class AdaptiveRules {

    public static final double INCREASE_FROM = 80;
    public static final double DECREASE_BELOW = 50;

    private AdaptiveRules() {
    }

    public static Difficulty next(Difficulty current, double averagePercent) {
        if (averagePercent >= INCREASE_FROM) {
            return current.harder();
        }
        if (averagePercent < DECREASE_BELOW) {
            return current.easier();
        }
        return current;
    }

    public static String explain(Difficulty current, double averagePercent, Difficulty next) {
        String avg = String.format("%.1f", averagePercent);
        if (averagePercent >= INCREASE_FROM) {
            return "Your recent average is " + avg + "% (80% or more), so we suggest a harder level: " + next + ".";
        }
        if (averagePercent < DECREASE_BELOW) {
            return "Your recent average is " + avg + "% (below 50%), so we suggest an easier level: " + next + ".";
        }
        return "Your recent average is " + avg + "% (50% to 79%), so we suggest staying at " + current + ".";
    }
}
