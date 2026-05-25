package com.smd.dto;

public class AuthResponse {

    private String token;
    private Long userId;
    private String email;
    private String fullName;
    private String role;
    private String department;

    public AuthResponse() {}

    public AuthResponse(String token, Long userId, String email,
                        String fullName, String role, String department) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.department = department;
    }

    // Getters
    public String getToken() { return token; }
    public Long getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public String getDepartment() { return department; }

    // Setters
    public void setToken(String token) { this.token = token; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setEmail(String email) { this.email = email; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setRole(String role) { this.role = role; }
    public void setDepartment(String department) { this.department = department; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String token;
        private Long userId;
        private String email;
        private String fullName;
        private String role;
        private String department;

        public Builder token(String token) { this.token = token; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder fullName(String fullName) { this.fullName = fullName; return this; }
        public Builder role(String role) { this.role = role; return this; }
        public Builder department(String department) { this.department = department; return this; }

        public AuthResponse build() {
            return new AuthResponse(token, userId, email, fullName, role, department);
        }
    }
}
