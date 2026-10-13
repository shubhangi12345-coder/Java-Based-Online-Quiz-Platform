package com.mancy.quizplatform.dto;

import jakarta.validation.constraints.*;
import com.mancy.quizplatform.enums.*;

/**
 * Rating and comment a participant gives about a quiz.
 */
public class FeedbackForm {

    @NotNull(message = "Choose a rating from 1 to 5")
    @Min(value = 1, message = "Rating must be 1 to 5")
    @Max(value = 5, message = "Rating must be 1 to 5")
    private Integer rating;

    @Size(max = 1000, message = "Comment must be at most 1000 characters")
    private String comment;

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}
