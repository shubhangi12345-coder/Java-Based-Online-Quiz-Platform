package com.mancy.quizplatform.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScoreCalculatorTest {

    @Test
    void percentageIsRoundedToOneDecimal() {
        assertEquals(66.7, ScoreCalculator.percentage(6, 9));
        assertEquals(100.0, ScoreCalculator.percentage(5, 5));
        assertEquals(0.0, ScoreCalculator.percentage(0, 5));
    }

    @Test
    void zeroTotalMarksGivesZeroPercent() {
        assertEquals(0.0, ScoreCalculator.percentage(0, 0));
    }

    @Test
    void passingIsInclusive() {
        assertTrue(ScoreCalculator.isPassed(40.0, 40));
        assertTrue(ScoreCalculator.isPassed(75.5, 40));
        assertFalse(ScoreCalculator.isPassed(39.9, 40));
    }

    @Test
    void gradeBands() {
        assertEquals("A", ScoreCalculator.grade(90));
        assertEquals("B", ScoreCalculator.grade(89.9));
        assertEquals("B", ScoreCalculator.grade(75));
        assertEquals("C", ScoreCalculator.grade(60));
        assertEquals("D", ScoreCalculator.grade(40));
        assertEquals("F", ScoreCalculator.grade(39.9));
    }
}
