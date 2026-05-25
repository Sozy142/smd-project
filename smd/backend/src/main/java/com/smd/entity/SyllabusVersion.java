package com.smd.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "syllabus_versions")
public class SyllabusVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long syllabusId;

    private String courseCode;

    private Integer versionNumber;

    @Column(columnDefinition = "TEXT")
    private String contentJson;

    private LocalDateTime snapshotAt;

    public SyllabusVersion() {}

    @PrePersist
    void prePersist() {
        snapshotAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getSyllabusId() { return syllabusId; }
    public String getCourseCode() { return courseCode; }
    public Integer getVersionNumber() { return versionNumber; }
    public String getContentJson() { return contentJson; }
    public LocalDateTime getSnapshotAt() { return snapshotAt; }

    public void setId(Long id) { this.id = id; }
    public void setSyllabusId(Long syllabusId) { this.syllabusId = syllabusId; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }
    public void setContentJson(String contentJson) { this.contentJson = contentJson; }
    public void setSnapshotAt(LocalDateTime snapshotAt) { this.snapshotAt = snapshotAt; }
}
