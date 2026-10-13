package com.mancy.quizplatform.enums;

/**
 * Life cycle of a quiz:
 * DRAFT -> PENDING_APPROVAL -> APPROVED (visible to participants)
 *                           -> REJECTED -> (creator edits) -> PENDING_APPROVAL
 */
public enum QuizStatus {
    DRAFT("Draft"),
    PENDING_APPROVAL("Pending approval"),
    APPROVED("Approved"),
    REJECTED("Rejected");

    private final String label;

    QuizStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
