package com.smd.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "syllabus_comments")
public class SyllabusComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "syllabus_id", nullable = false)
    private Syllabus syllabus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    private CommentErrorType errorType;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SyllabusComment() {}

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Syllabus getSyllabus() { return syllabus; }
    public User getAuthor() { return author; }
    public String getContent() { return content; }
    public CommentErrorType getErrorType() { return errorType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setId(Long id) { this.id = id; }
    public void setSyllabus(Syllabus syllabus) { this.syllabus = syllabus; }
    public void setAuthor(User author) { this.author = author; }
    public void setContent(String content) { this.content = content; }
    public void setErrorType(CommentErrorType errorType) { this.errorType = errorType; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
