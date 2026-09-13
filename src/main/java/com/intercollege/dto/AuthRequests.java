package com.intercollege.dto;

import java.util.List;

public class AuthRequests {

    public static class RegisterStudentRequest {
        private String fullName;
        private String username;
        private String password;
        private String confirmPassword;
        private String college;
        private String location;
        private List<String> interests;

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getConfirmPassword() { return confirmPassword; }
        public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
        public String getCollege() { return college; }
        public void setCollege(String college) { this.college = college; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public List<String> getInterests() { return interests; }
        public void setInterests(List<String> interests) { this.interests = interests; }
    }

    public static class RegisterCoordinatorRequest {
        private String fullName;
        private String username;
        private String password;
        private String confirmPassword;
        private String college;

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getConfirmPassword() { return confirmPassword; }
        public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
        public String getCollege() { return college; }
        public void setCollege(String college) { this.college = college; }
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class UpdateProfileRequest {
        private String fullName;
        private String college;
        private String location;
        private List<String> interests;

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getCollege() { return college; }
        public void setCollege(String college) { this.college = college; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public List<String> getInterests() { return interests; }
        public void setInterests(List<String> interests) { this.interests = interests; }
    }
}
