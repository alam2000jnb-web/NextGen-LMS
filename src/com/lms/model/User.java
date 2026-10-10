package com.lms.model;

import com.lms.service.interfaces.Authenticatable;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Abstract base class demonstrating OOP Inheritance and Encapsulation.
 */
public abstract class User implements Authenticatable, Serializable {
    private static final long serialVersionUID = 1L;

    protected int id;
    protected String name;
    protected String email;
    protected String passwordHash;
    protected UserRole role;
    protected String status; // "ACTIVE", "INACTIVE", "SUSPENDED"
    protected LocalDateTime createdAt;

    public User() {
        this.status = "ACTIVE";
        this.createdAt = LocalDateTime.now();
    }

    public User(int id, String name, String email, String passwordHash, UserRole role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.status = "ACTIVE";
        this.createdAt = LocalDateTime.now();
    }

    // Abstract methods demonstrating Polymorphism
    public abstract String getDashboardTitle();
    public abstract List<String> getPermissions();
    public abstract boolean canManageCourse(int courseId);

    // Interface implementation
    @Override
    public boolean authenticate(String enteredPassword) {
        if (this.passwordHash == null || enteredPassword == null) return false;
        return this.passwordHash.equals(enteredPassword);
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public String getRoleName() {
        return this.role != null ? this.role.getDisplayName() : "Unknown";
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getFormattedCreatedAt() {
        if (createdAt == null) return "N/A";
        return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    @Override
    public String toString() {
        return String.format("%s (%s) [%s]", name, email, role.getDisplayName());
    }
}
