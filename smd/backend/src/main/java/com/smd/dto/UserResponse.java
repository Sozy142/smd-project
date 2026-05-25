package com.smd.dto;

import com.smd.entity.User;
import java.time.LocalDateTime;

public class UserResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String role;
    private String department;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    public UserResponse() {}

    // Getters
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public String getDepartment() { return department; }
    public Boolean getIsActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastLogin() { return lastLogin; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setRole(String role) { this.role = role; }
    public void setDepartment(String department) { this.department = department; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public static UserResponse from(User u) {
        UserResponse r = new UserResponse();
        r.setId(u.getId());
        r.setEmail(u.getEmail());
        r.setFirstName(u.getFirstName());
        r.setLastName(u.getLastName());
        r.setFullName(u.getFirstName() + " " + u.getLastName());
        r.setRole(u.getRole() != null ? u.getRole().name() : null);
        r.setDepartment(u.getDepartment());
        r.setIsActive(u.getIsActive());
        r.setCreatedAt(u.getCreatedAt());
        r.setLastLogin(u.getLastLogin());
        return r;
    }
}
