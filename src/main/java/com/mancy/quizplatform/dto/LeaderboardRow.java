package com.mancy.quizplatform.dto;

import com.mancy.quizplatform.enums.*;

/**
 * One line of the leaderboard.
 */
public class LeaderboardRow {

    private int rank;

    private Long userId;

    private String name;

    private long attempts;

    private double average;

    private double best;

    public LeaderboardRow() {
    }

    public LeaderboardRow(int rank, Long userId, String name, long attempts, double average, double best) {
        this.rank = rank;
        this.userId = userId;
        this.name = name;
        this.attempts = attempts;
        this.average = average;
        this.best = best;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getAttempts() {
        return attempts;
    }

    public void setAttempts(long attempts) {
        this.attempts = attempts;
    }

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }

    public double getBest() {
        return best;
    }

    public void setBest(double best) {
        this.best = best;
    }

}
