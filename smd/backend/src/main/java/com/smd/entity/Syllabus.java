package com.smd.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "syllabi")
public class Syllabus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String courseCode;
    private String courseName;
    private String department;
    private Integer credits;
    private String academicYear;
    private String semester;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String learningOutcomes;

    @Column(columnDefinition = "TEXT")
    private String assessmentMethods;

    @Column(columnDefinition = "TEXT")
    private String prerequisites;

    @Column(columnDefinition = "TEXT")
    private String materials;

    @Column(columnDefinition = "TEXT")
    private String ploOutcomes;

    @Column(columnDefinition = "TEXT")
    private String cloMappings;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private SyllabusStatus status = SyllabusStatus.DRAFT;

    private Integer versionNumber = 1;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Syllabus() {}

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public String getCourseCode() { return courseCode; }
    public String getCourseName() { return courseName; }
    public String getDepartment() { return department; }
    public Integer getCredits() { return credits; }
    public String getAcademicYear() { return academicYear; }
    public String getSemester() { return semester; }
    public String getDescription() { return description; }
    public String getLearningOutcomes() { return learningOutcomes; }
    public String getAssessmentMethods() { return assessmentMethods; }
    public String getPrerequisites() { return prerequisites; }
    public String getMaterials() { return materials; }
    public String getPloOutcomes() { return ploOutcomes; }
    public String getCloMappings() { return cloMappings; }
    public SyllabusStatus getStatus() { return status; }
    public Integer getVersionNumber() { return versionNumber; }
    public String getRejectionReason() { return rejectionReason; }
    public User getCreatedBy() { return createdBy; }
    public User getReviewedBy() { return reviewedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public void setDepartment(String department) { this.department = department; }
    public void setCredits(Integer credits) { this.credits = credits; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public void setSemester(String semester) { this.semester = semester; }
    public void setDescription(String description) { this.description = description; }
    public void setLearningOutcomes(String learningOutcomes) { this.learningOutcomes = learningOutcomes; }
    public void setAssessmentMethods(String assessmentMethods) { this.assessmentMethods = assessmentMethods; }
    public void setPrerequisites(String prerequisites) { this.prerequisites = prerequisites; }
    public void setMaterials(String materials) { this.materials = materials; }
    public void setPloOutcomes(String ploOutcomes) { this.ploOutcomes = ploOutcomes; }
    public void setCloMappings(String cloMappings) { this.cloMappings = cloMappings; }
    public void setStatus(SyllabusStatus status) { this.status = status; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public void setReviewedBy(User reviewedBy) { this.reviewedBy = reviewedBy; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String courseCode;
        private String courseName;
        private String department;
        private Integer credits;
        private String academicYear;
        private String semester;
        private String description;
        private String learningOutcomes;
        private String assessmentMethods;
        private String prerequisites;
        private String materials;
        private String ploOutcomes;
        private String cloMappings;
        private SyllabusStatus status = SyllabusStatus.DRAFT;
        private Integer versionNumber = 1;
        private String rejectionReason;
        private User createdBy;
        private User reviewedBy;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder courseCode(String courseCode) { this.courseCode = courseCode; return this; }
        public Builder courseName(String courseName) { this.courseName = courseName; return this; }
        public Builder department(String department) { this.department = department; return this; }
        public Builder credits(Integer credits) { this.credits = credits; return this; }
        public Builder academicYear(String academicYear) { this.academicYear = academicYear; return this; }
        public Builder semester(String semester) { this.semester = semester; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder learningOutcomes(String learningOutcomes) { this.learningOutcomes = learningOutcomes; return this; }
        public Builder assessmentMethods(String assessmentMethods) { this.assessmentMethods = assessmentMethods; return this; }
        public Builder prerequisites(String prerequisites) { this.prerequisites = prerequisites; return this; }
        public Builder materials(String materials) { this.materials = materials; return this; }
        public Builder ploOutcomes(String ploOutcomes) { this.ploOutcomes = ploOutcomes; return this; }
        public Builder cloMappings(String cloMappings) { this.cloMappings = cloMappings; return this; }
        public Builder status(SyllabusStatus status) { this.status = status; return this; }
        public Builder versionNumber(Integer versionNumber) { this.versionNumber = versionNumber; return this; }
        public Builder rejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; return this; }
        public Builder createdBy(User createdBy) { this.createdBy = createdBy; return this; }
        public Builder reviewedBy(User reviewedBy) { this.reviewedBy = reviewedBy; return this; }

        public Syllabus build() {
            Syllabus s = new Syllabus();
            s.setId(id);
            s.setCourseCode(courseCode);
            s.setCourseName(courseName);
            s.setDepartment(department);
            s.setCredits(credits);
            s.setAcademicYear(academicYear);
            s.setSemester(semester);
            s.setDescription(description);
            s.setLearningOutcomes(learningOutcomes);
            s.setAssessmentMethods(assessmentMethods);
            s.setPrerequisites(prerequisites);
            s.setMaterials(materials);
            s.setPloOutcomes(ploOutcomes);
            s.setCloMappings(cloMappings);
            s.setStatus(status);
            s.setVersionNumber(versionNumber);
            s.setRejectionReason(rejectionReason);
            s.setCreatedBy(createdBy);
            s.setReviewedBy(reviewedBy);
            return s;
        }
    }
}
