package com.mancy.quizplatform.dto;


/**
 * Performance numbers of one quiz (shown to its creator).
 */
public class QuizStats {

    private long attempts;

    private double average;

    private double highest;

    private double lowest;

    private double passRate;

    private long feedbackCount;

    private double averageRating;

    public QuizStats() {
    }

    public QuizStats(long attempts, double average, double highest, double lowest, double passRate, long feedbackCount, double averageRating) {
        this.attempts = attempts;
        this.average = average;
        this.highest = highest;
        this.lowest = lowest;
        this.passRate = passRate;
        this.feedbackCount = feedbackCount;
        this.averageRating = averageRating;
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

    public double getHighest() {
        return highest;
    }

    public void setHighest(double highest) {
        this.highest = highest;
    }

    public double getLowest() {
        return lowest;
    }

    public void setLowest(double lowest) {
        this.lowest = lowest;
    }

    public double getPassRate() {
        return passRate;
    }

    public void setPassRate(double passRate) {
        this.passRate = passRate;
    }

    public long getFeedbackCount() {
        return feedbackCount;
    }

    public void setFeedbackCount(long feedbackCount) {
        this.feedbackCount = feedbackCount;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

}
