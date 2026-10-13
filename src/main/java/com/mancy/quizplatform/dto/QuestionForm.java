package com.mancy.quizplatform.dto;

import jakarta.validation.constraints.*;
import com.mancy.quizplatform.enums.*;
import java.util.List;

/**
 * Data typed in the add / edit question form: four options and the index (0-3) of the correct one.
 */
public class QuestionForm {

    @NotBlank(message = "Question text is required")
    @Size(max = 1000, message = "Question must be at most 1000 characters")
    private String text;

    @NotNull(message = "Choose a difficulty")
    private Difficulty difficulty = Difficulty.MEDIUM;

    @NotNull(message = "Marks are required")
    @Min(value = 1, message = "Marks must be at least 1")
    @Max(value = 10, message = "Marks cannot exceed 10")
    private Integer marks = 1;

    @NotBlank(message = "Option A is required")
    @Size(max = 500, message = "Option A is too long")
    private String option1;

    @NotBlank(message = "Option B is required")
    @Size(max = 500, message = "Option B is too long")
    private String option2;

    @NotBlank(message = "Option C is required")
    @Size(max = 500, message = "Option C is too long")
    private String option3;

    @NotBlank(message = "Option D is required")
    @Size(max = 500, message = "Option D is too long")
    private String option4;

    @NotNull(message = "Select the correct answer")
    @Min(value = 0, message = "Invalid correct answer")
    @Max(value = 3, message = "Invalid correct answer")
    private Integer correctIndex;

    @Size(max = 1000, message = "Explanation must be at most 1000 characters")
    private String explanation;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getMarks() {
        return marks;
    }

    public void setMarks(Integer marks) {
        this.marks = marks;
    }

    public String getOption1() {
        return option1;
    }

    public void setOption1(String option1) {
        this.option1 = option1;
    }

    public String getOption2() {
        return option2;
    }

    public void setOption2(String option2) {
        this.option2 = option2;
    }

    public String getOption3() {
        return option3;
    }

    public void setOption3(String option3) {
        this.option3 = option3;
    }

    public String getOption4() {
        return option4;
    }

    public void setOption4(String option4) {
        this.option4 = option4;
    }

    public Integer getCorrectIndex() {
        return correctIndex;
    }

    public void setCorrectIndex(Integer correctIndex) {
        this.correctIndex = correctIndex;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }


    /** The four option texts in order A, B, C, D. */
    public List<String> optionTexts() {
        return List.of(option1, option2, option3, option4);
    }

}
