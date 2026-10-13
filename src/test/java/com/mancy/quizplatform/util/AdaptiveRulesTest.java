package com.mancy.quizplatform.util;

import com.mancy.quizplatform.enums.Difficulty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdaptiveRulesTest {

    @Test
    void highAverageSuggestsHarderLevel() {
        assertEquals(Difficulty.MEDIUM, AdaptiveRules.next(Difficulty.EASY, 80));
        assertEquals(Difficulty.HARD, AdaptiveRules.next(Difficulty.MEDIUM, 95));
    }

    @Test
    void middleAverageKeepsTheLevel() {
        assertEquals(Difficulty.MEDIUM, AdaptiveRules.next(Difficulty.MEDIUM, 79.9));
        assertEquals(Difficulty.MEDIUM, AdaptiveRules.next(Difficulty.MEDIUM, 50));
    }

    @Test
    void lowAverageSuggestsEasierLevel() {
        assertEquals(Difficulty.EASY, AdaptiveRules.next(Difficulty.MEDIUM, 49.9));
        assertEquals(Difficulty.MEDIUM, AdaptiveRules.next(Difficulty.HARD, 10));
    }

    @Test
    void levelNeverLeavesTheEasyToHardRange() {
        assertEquals(Difficulty.HARD, AdaptiveRules.next(Difficulty.HARD, 100));
        assertEquals(Difficulty.EASY, AdaptiveRules.next(Difficulty.EASY, 0));
    }

    @Test
    void explanationMentionsTheSuggestedLevel() {
        String text = AdaptiveRules.explain(Difficulty.EASY, 90, Difficulty.MEDIUM);
        assertTrue(text.contains("MEDIUM"));
    }
}
