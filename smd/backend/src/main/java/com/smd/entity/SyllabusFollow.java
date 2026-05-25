package com.smd.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "syllabus_follows",
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "syllabus_id"}))
public class SyllabusFollow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "syllabus_id", nullable = false)
    private Syllabus syllabus;

    private LocalDateTime createdAt;

    public SyllabusFollow() {}

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public User getStudent() { return student; }
    public Syllabus getSyllabus() { return syllabus; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setStudent(User student) { this.student = student; }
    public void setSyllabus(Syllabus syllabus) { this.syllabus = syllabus; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
