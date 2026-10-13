package com.mancy.quizplatform.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.mancy.quizplatform.enums.*;

/**
 * A quiz made by a Quiz Creator. One quiz has many questions.
 */
@Entity
@Table(name = "quizzes")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, length = 100)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Difficulty difficulty = Difficulty.MEDIUM;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Column(name = "passing_percent", nullable = false)
    private int passingPercent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QuizStatus status = QuizStatus.DRAFT;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    private AppUser creator;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Question> questions = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public int getPassingPercent() {
        return passingPercent;
    }

    public void setPassingPercent(int passingPercent) {
        this.passingPercent = passingPercent;
    }

    public QuizStatus getStatus() {
        return status;
    }

    public void setStatus(QuizStatus status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public AppUser getCreator() {
        return creator;
    }

    public void setCreator(AppUser creator) {
        this.creator = creator;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }


    /** Business rule kept inside the entity: only DRAFT or REJECTED quizzes can be edited by the creator. */
    public boolean isEditable() {
        return status == QuizStatus.DRAFT || status == QuizStatus.REJECTED;
    }

    /** Keeps both sides of the quiz <-> question relationship in sync. */
    public void addQuestion(Question question) {
        question.setQuiz(this);
        questions.add(question);
    }

    /** Sum of the marks of all questions. */
    public int getTotalMarks() {
        int total = 0;
        for (Question question : questions) {
            total += question.getMarks();
        }
        return total;
    }

}
