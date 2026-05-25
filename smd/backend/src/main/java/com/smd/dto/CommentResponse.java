package com.smd.dto;

import com.smd.entity.SyllabusComment;
import java.time.LocalDateTime;

public class CommentResponse {

    private Long id;
    private Long syllabusId;
    private Long authorId;
    private String authorName;
    private String authorRole;
    private String content;
    private String errorType;
    private boolean isOwner;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CommentResponse() {}

    public static CommentResponse from(SyllabusComment c, String currentUserEmail) {
        CommentResponse r = new CommentResponse();
        r.setId(c.getId());
        r.setSyllabusId(c.getSyllabus().getId());
        r.setAuthorId(c.getAuthor().getId());
        r.setAuthorName(c.getAuthor().getFirstName() + " " + c.getAuthor().getLastName());
        r.setAuthorRole(c.getAuthor().getRole().name());
        r.setContent(c.getContent());
        r.setErrorType(c.getErrorType() != null ? c.getErrorType().name() : null);
        r.setOwner(currentUserEmail != null && c.getAuthor().getEmail().equals(currentUserEmail));
        r.setCreatedAt(c.getCreatedAt());
        r.setUpdatedAt(c.getUpdatedAt());
        return r;
    }

    public Long getId() { return id; }
    public Long getSyllabusId() { return syllabusId; }
    public Long getAuthorId() { return authorId; }
    public String getAuthorName() { return authorName; }
    public String getAuthorRole() { return authorRole; }
    public String getContent() { return content; }
    public String getErrorType() { return errorType; }
    public boolean isOwner() { return isOwner; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setId(Long id) { this.id = id; }
    public void setSyllabusId(Long syllabusId) { this.syllabusId = syllabusId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public void setAuthorRole(String authorRole) { this.authorRole = authorRole; }
    public void setContent(String content) { this.content = content; }
    public void setErrorType(String errorType) { this.errorType = errorType; }
    public void setOwner(boolean owner) { this.isOwner = owner; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
