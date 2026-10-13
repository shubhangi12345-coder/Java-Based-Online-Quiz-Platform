package com.mancy.quizplatform.dto;


/**
 * Numbers shown on the admin dashboard.
 */
public class AdminStats {

    private long totalUsers;

    private long admins;

    private long creators;

    private long participants;

    private long totalQuizzes;

    private long draftQuizzes;

    private long pendingQuizzes;

    private long approvedQuizzes;

    private long rejectedQuizzes;

    private long totalAttempts;

    private double averageScore;

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getAdmins() {
        return admins;
    }

    public void setAdmins(long admins) {
        this.admins = admins;
    }

    public long getCreators() {
        return creators;
    }

    public void setCreators(long creators) {
        this.creators = creators;
    }

    public long getParticipants() {
        return participants;
    }

    public void setParticipants(long participants) {
        this.participants = participants;
    }

    public long getTotalQuizzes() {
        return totalQuizzes;
    }

    public void setTotalQuizzes(long totalQuizzes) {
        this.totalQuizzes = totalQuizzes;
    }

    public long getDraftQuizzes() {
        return draftQuizzes;
    }

    public void setDraftQuizzes(long draftQuizzes) {
        this.draftQuizzes = draftQuizzes;
    }

    public long getPendingQuizzes() {
        return pendingQuizzes;
    }

    public void setPendingQuizzes(long pendingQuizzes) {
        this.pendingQuizzes = pendingQuizzes;
    }

    public long getApprovedQuizzes() {
        return approvedQuizzes;
    }

    public void setApprovedQuizzes(long approvedQuizzes) {
        this.approvedQuizzes = approvedQuizzes;
    }

    public long getRejectedQuizzes() {
        return rejectedQuizzes;
    }

    public void setRejectedQuizzes(long rejectedQuizzes) {
        this.rejectedQuizzes = rejectedQuizzes;
    }

    public long getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(long totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

}
