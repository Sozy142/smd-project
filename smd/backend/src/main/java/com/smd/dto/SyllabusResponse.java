package com.smd.dto;

import com.smd.entity.Syllabus;
import com.smd.entity.SyllabusStatus;
import java.time.LocalDateTime;

public class SyllabusResponse {

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
    private SyllabusStatus status;
    private Integer versionNumber;
    private String rejectionReason;
    private String createdByName;
    private String reviewedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SyllabusResponse() {}

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
    public String getCreatedByName() { return createdByName; }
    public String getReviewedByName() { return reviewedByName; }
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
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }
    public void setReviewedByName(String reviewedByName) { this.reviewedByName = reviewedByName; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static SyllabusResponse from(Syllabus s) {
        SyllabusResponse r = new SyllabusResponse();
        r.setId(s.getId());
        r.setCourseCode(s.getCourseCode());
        r.setCourseName(s.getCourseName());
        r.setDepartment(s.getDepartment());
        r.setCredits(s.getCredits());
        r.setAcademicYear(s.getAcademicYear());
        r.setSemester(s.getSemester());
        r.setDescription(s.getDescription());
        r.setLearningOutcomes(s.getLearningOutcomes());
        r.setAssessmentMethods(s.getAssessmentMethods());
        r.setPrerequisites(s.getPrerequisites());
        r.setMaterials(s.getMaterials());
        r.setPloOutcomes(s.getPloOutcomes());
        r.setCloMappings(s.getCloMappings());
        r.setStatus(s.getStatus());
        r.setVersionNumber(s.getVersionNumber());
        r.setRejectionReason(s.getRejectionReason());
        r.setCreatedByName(s.getCreatedBy() != null
                ? s.getCreatedBy().getFirstName() + " " + s.getCreatedBy().getLastName() : null);
        r.setReviewedByName(s.getReviewedBy() != null
                ? s.getReviewedBy().getFirstName() + " " + s.getReviewedBy().getLastName() : null);
        r.setCreatedAt(s.getCreatedAt());
        r.setUpdatedAt(s.getUpdatedAt());
        return r;
    }
}
