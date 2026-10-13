package com.mancy.quizplatform.dto;

import com.mancy.quizplatform.enums.*;

/**
 * Numbers shown on the participant dashboard.
 */
public class ParticipantStats {

    private long attempts;

    private double averageScore;

    private double bestScore;

    private long passedCount;

    private Integer rank;

    private Difficulty recommendedDifficulty;

    private String recommendationReason;

    public long getAttempts() {
        return attempts;
    }

    public void setAttempts(long attempts) {
        this.attempts = attempts;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public double getBestScore() {
        return bestScore;
    }

    public void setBestScore(double bestScore) {
        this.bestScore = bestScore;
    }

    public long getPassedCount() {
        return passedCount;
    }

    public void setPassedCount(long passedCount) {
        this.passedCount = passedCount;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Difficulty getRecommendedDifficulty() {
        return recommendedDifficulty;
    }

    public void setRecommendedDifficulty(Difficulty recommendedDifficulty) {
        this.recommendedDifficulty = recommendedDifficulty;
    }

    public String getRecommendationReason() {
        return recommendationReason;
    }

    public void setRecommendationReason(String recommendationReason) {
        this.recommendationReason = recommendationReason;
    }

}
