package com.smd.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "approval_history")
public class ApprovalHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "syllabus_id")
    private Syllabus syllabus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @Enumerated(EnumType.STRING)
    private Role actorRole;

    private String action;

    @Column(columnDefinition = "TEXT")
    private String comments;

    private LocalDateTime actionDate;

    public ApprovalHistory() {}

    @PrePersist
    void prePersist() {
        actionDate = LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public Syllabus getSyllabus() { return syllabus; }
    public User getActor() { return actor; }
    public Role getActorRole() { return actorRole; }
    public String getAction() { return action; }
    public String getComments() { return comments; }
    public LocalDateTime getActionDate() { return actionDate; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setSyllabus(Syllabus syllabus) { this.syllabus = syllabus; }
    public void setActor(User actor) { this.actor = actor; }
    public void setActorRole(Role actorRole) { this.actorRole = actorRole; }
    public void setAction(String action) { this.action = action; }
    public void setComments(String comments) { this.comments = comments; }
    public void setActionDate(LocalDateTime actionDate) { this.actionDate = actionDate; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Syllabus syllabus;
        private User actor;
        private Role actorRole;
        private String action;
        private String comments;

        public Builder syllabus(Syllabus syllabus) { this.syllabus = syllabus; return this; }
        public Builder actor(User actor) { this.actor = actor; return this; }
        public Builder actorRole(Role actorRole) { this.actorRole = actorRole; return this; }
        public Builder action(String action) { this.action = action; return this; }
        public Builder comments(String comments) { this.comments = comments; return this; }

        public ApprovalHistory build() {
            ApprovalHistory h = new ApprovalHistory();
            h.setSyllabus(syllabus);
            h.setActor(actor);
            h.setActorRole(actorRole);
            h.setAction(action);
            h.setComments(comments);
            return h;
        }
    }
}
