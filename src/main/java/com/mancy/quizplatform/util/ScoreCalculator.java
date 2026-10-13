package com.mancy.quizplatform.util;

/**
 * The scoring rules in one small class (easy to read and to unit test).
 *  - a correct answer earns the question's marks, a wrong or missing answer earns 0 (no negative marking)
 *  - percentage = score / total marks x 100, rounded to one decimal
 *  - passed = percentage >= the quiz's passing percentage
 */
public final class ScoreCalculator {

    private ScoreCalculator() {
    }

    public static double percentage(int score, int totalMarks) {
        if (totalMarks <= 0) {
            return 0;
        }
        return Math.round(score * 1000.0 / totalMarks) / 10.0;
    }

    public static boolean isPassed(double percentage, int passingPercent) {
        return percentage >= passingPercent;
    }

    /** Letter grade: A >= 90, B >= 75, C >= 60, D >= 40, otherwise F. */
    public static String grade(double percentage) {
        if (percentage >= 90) {
            return "A";
        }
        if (percentage >= 75) {
            return "B";
        }
        if (percentage >= 60) {
            return "C";
        }
        return percentage >= 40 ? "D" : "F";
    }
}
