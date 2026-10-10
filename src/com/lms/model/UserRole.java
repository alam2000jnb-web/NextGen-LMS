package com.lms.model;

public enum UserRole {
    ADMIN("Administrator"),
    INSTRUCTOR("Instructor"),
    STUDENT("Student");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static UserRole fromString(String roleStr) {
        if (roleStr == null) return STUDENT;
        for (UserRole role : values()) {
            if (role.name().equalsIgnoreCase(roleStr.trim()) || role.getDisplayName().equalsIgnoreCase(roleStr.trim())) {
                return role;
            }
        }
        return STUDENT;
    }
}
