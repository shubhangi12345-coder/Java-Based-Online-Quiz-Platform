package com.mancy.quizplatform.enums;

/** The three kinds of users. The database stores the enum name (ADMIN, CREATOR, PARTICIPANT). */
public enum Role {
    ADMIN("Admin"),
    CREATOR("Quiz Creator"),
    PARTICIPANT("Participant");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    /** Friendly name shown on pages. */
    public String getLabel() {
        return label;
    }
}
