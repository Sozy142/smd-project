package com.smd.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    private LocalDateTime createdAt;

    private Long syllabusId;

    private String link;

    public Notification() {}

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public User getRecipient() { return recipient; }
    public String getMessage() { return message; }
    public boolean isRead() { return isRead; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Long getSyllabusId() { return syllabusId; }
    public String getLink() { return link; }

    public void setId(Long id) { this.id = id; }
    public void setRecipient(User recipient) { this.recipient = recipient; }
    public void setMessage(String message) { this.message = message; }
    public void setRead(boolean read) { this.isRead = read; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setSyllabusId(Long syllabusId) { this.syllabusId = syllabusId; }
    public void setLink(String link) { this.link = link; }
}
