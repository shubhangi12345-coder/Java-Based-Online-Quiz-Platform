package com.mancy.quizplatform.enums;

/** State of one quiz attempt. */
public enum AttemptStatus {
    IN_PROGRESS,
    SUBMITTED,
    /** Closed automatically because the time ran out. */
    AUTO_SUBMITTED
}
