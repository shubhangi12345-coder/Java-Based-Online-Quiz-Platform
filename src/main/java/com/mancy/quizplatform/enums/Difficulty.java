package com.mancy.quizplatform.enums;

/** Difficulty levels. The order EASY < MEDIUM < HARD is used by the adaptive recommendation. */
public enum Difficulty {
    EASY, MEDIUM, HARD;

    /** One level harder (HARD stays HARD). */
    public Difficulty harder() {
        Difficulty[] all = values();
        return all[Math.min(all.length - 1, ordinal() + 1)];
    }

    /** One level easier (EASY stays EASY). */
    public Difficulty easier() {
        Difficulty[] all = values();
        return all[Math.max(0, ordinal() - 1)];
    }
}
