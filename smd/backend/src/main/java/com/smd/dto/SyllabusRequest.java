package com.smd.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SyllabusRequest {

    @NotBlank private String courseCode;
    @NotBlank private String courseName;
    @NotNull  private Integer credits;
    private String department;
    private String academicYear;
    private String semester;
    private String description;
    private String learningOutcomes;
    private String assessmentMethods;
    private String prerequisites;
    private String materials;
    private String ploOutcomes;
    private String cloMappings;

    public SyllabusRequest() {}

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLearningOutcomes() { return learningOutcomes; }
    public void setLearningOutcomes(String learningOutcomes) { this.learningOutcomes = learningOutcomes; }

    public String getAssessmentMethods() { return assessmentMethods; }
    public void setAssessmentMethods(String assessmentMethods) { this.assessmentMethods = assessmentMethods; }

    public String getPrerequisites() { return prerequisites; }
    public void setPrerequisites(String prerequisites) { this.prerequisites = prerequisites; }

    public String getMaterials() { return materials; }
    public void setMaterials(String materials) { this.materials = materials; }

    public String getPloOutcomes() { return ploOutcomes; }
    public void setPloOutcomes(String ploOutcomes) { this.ploOutcomes = ploOutcomes; }

    public String getCloMappings() { return cloMappings; }
    public void setCloMappings(String cloMappings) { this.cloMappings = cloMappings; }
}
