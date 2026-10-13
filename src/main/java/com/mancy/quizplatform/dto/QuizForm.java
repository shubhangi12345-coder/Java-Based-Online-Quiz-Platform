package com.mancy.quizplatform.dto;

import jakarta.validation.constraints.*;
import com.mancy.quizplatform.enums.*;

/**
 * Data typed in the create / edit quiz form.
 */
public class QuizForm {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be at most 200 characters")
    private String title;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must be at most 100 characters")
    private String category;

    @NotNull(message = "Choose a difficulty")
    private Difficulty difficulty = Difficulty.MEDIUM;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 180, message = "Duration cannot exceed 180 minutes")
    private Integer durationMinutes;

    @NotNull(message = "Passing percentage is required")
    @Min(value = 1, message = "Passing percentage must be at least 1")
    @Max(value = 100, message = "Passing percentage cannot exceed 100")
    private Integer passingPercent = 40;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Integer getPassingPercent() {
        return passingPercent;
    }

    public void setPassingPercent(Integer passingPercent) {
        this.passingPercent = passingPercent;
    }

}
