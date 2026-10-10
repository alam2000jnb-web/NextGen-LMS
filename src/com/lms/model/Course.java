package com.lms.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Course implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String code;
    private String title;
    private String description;
    private String category;
    private int instructorId;
    private String instructorName;
    private int maxSeats;
    private int enrolledCount;
    private String syllabus;
    private String status; // "ACTIVE", "ARCHIVED", "DRAFT"
    private LocalDateTime createdAt;

    public Course() {
        this.maxSeats = 50;
        this.enrolledCount = 0;
        this.status = "ACTIVE";
        this.createdAt = LocalDateTime.now();
    }

    public Course(int id, String code, String title, String description, String category, int instructorId, int maxSeats, String syllabus) {
        this.id = id;
        this.code = code;
        this.title = title;
        this.description = description;
        this.category = category;
        this.instructorId = instructorId;
        this.maxSeats = maxSeats;
        this.enrolledCount = 0;
        this.syllabus = syllabus;
        this.status = "ACTIVE";
        this.createdAt = LocalDateTime.now();
    }

    public int getAvailableSeats() {
        return Math.max(0, maxSeats - enrolledCount);
    }

    public boolean isFull() {
        return enrolledCount >= maxSeats;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getInstructorId() { return instructorId; }
    public void setInstructorId(int instructorId) { this.instructorId = instructorId; }

    public String getInstructorName() { return instructorName != null ? instructorName : "Instructor #" + instructorId; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }

    public int getMaxSeats() { return maxSeats; }
    public void setMaxSeats(int maxSeats) { this.maxSeats = maxSeats; }

    public int getEnrolledCount() { return enrolledCount; }
    public void setEnrolledCount(int enrolledCount) { this.enrolledCount = enrolledCount; }

    public String getSyllabus() { return syllabus; }
    public void setSyllabus(String syllabus) { this.syllabus = syllabus; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getFormattedCreatedAt() {
        if (createdAt == null) return "N/A";
        return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s (Enrolled: %d/%d)", code, title, getInstructorName(), enrolledCount, maxSeats);
    }
}
