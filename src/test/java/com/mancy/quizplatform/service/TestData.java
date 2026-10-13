package com.mancy.quizplatform.service;

import com.mancy.quizplatform.dto.QuestionForm;
import com.mancy.quizplatform.entity.Question;
import com.mancy.quizplatform.entity.QuestionOption;
import com.mancy.quizplatform.enums.Difficulty;

/** Small helpers shared by the service tests. */
final class TestData {

    private TestData() {
    }

    /** A question with options "A".."D" where option number correctIndex (0-3) is correct. */
    static Question question(String text, int marks, int correctIndex) {
        Question question = new Question();
        question.setText(text);
        question.setMarks(marks);
        question.setDifficulty(Difficulty.EASY);
        String[] texts = {"A", "B", "C", "D"};
        for (int i = 0; i < texts.length; i++) {
            QuestionOption option = new QuestionOption();
            option.setText(texts[i] + " for " + text);
            option.setCorrect(i == correctIndex);
            option.setSortOrder(i);
            question.addOption(option);
        }
        return question;
    }

    static QuestionForm questionForm(String text, String o1, String o2, String o3, String o4) {
        QuestionForm form = new QuestionForm();
        form.setText(text);
        form.setOption1(o1);
        form.setOption2(o2);
        form.setOption3(o3);
        form.setOption4(o4);
        form.setCorrectIndex(0);
        form.setMarks(1);
        form.setDifficulty(Difficulty.EASY);
        return form;
    }

    static Long optionId(Question question, boolean correct) {
        for (QuestionOption option : question.getOptions()) {
            if (option.isCorrect() == correct) {
                return option.getId();
            }
        }
        throw new IllegalStateException("no such option");
    }
}
