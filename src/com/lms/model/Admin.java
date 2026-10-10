package com.lms.model;

import java.util.Arrays;
import java.util.List;

/**
 * Admin user model demonstrating Inheritance and Polymorphism.
 */
public class Admin extends User {
    private static final long serialVersionUID = 1L;

    private String department = "System Administration";

    public Admin() {
        super();
        this.role = UserRole.ADMIN;
    }

    public Admin(int id, String name, String email, String passwordHash) {
        super(id, name, email, passwordHash, UserRole.ADMIN);
    }

    @Override
    public String getDashboardTitle() {
        return "System Administrator Portal";
    }

    @Override
    public List<String> getPermissions() {
        return Arrays.asList(
            "MANAGE_USERS",
            "MANAGE_COURSES",
            "VIEW_ANALYTICS",
            "SYSTEM_SETTINGS",
            "MONITOR_ACTIVITY",
            "DATABASE_BACKUP"
        );
    }

    @Override
    public boolean canManageCourse(int courseId) {
        // Admin has universal course management privileges
        return true;
    }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}
