package com.mancy.quizplatform.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.mancy.quizplatform.enums.*;

/**
 * A participant's rating (1-5) and comment about a quiz. One feedback per participant per quiz.
 */
@Entity
@Table(name = "feedback", uniqueConstraints = @UniqueConstraint(columnNames = {"quiz_id", "participant_id"}))
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(optional = false)
    @JoinColumn(name = "participant_id", nullable = false)
    private AppUser participant;

    @Column(nullable = false)
    private int rating;

    @Column(name = "comment_text", length = 1000)
    private String comment;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public AppUser getParticipant() {
        return participant;
    }

    public void setParticipant(AppUser participant) {
        this.participant = participant;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

}
