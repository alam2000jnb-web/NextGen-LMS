package com.lms.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Assignment implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int courseId;
    private String courseTitle;
    private String title;
    private String description;
    private double maxMarks;
    private String dueDate; // "YYYY-MM-DD"
    private LocalDateTime createdAt;

    public Assignment() {
        this.maxMarks = 100.0;
        this.createdAt = LocalDateTime.now();
    }

    public Assignment(int id, int courseId, String title, String description, double maxMarks, String dueDate) {
        this.id = id;
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.maxMarks = maxMarks;
        this.dueDate = dueDate;
        this.createdAt = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public String getCourseTitle() { return courseTitle != null ? courseTitle : "Course #" + courseId; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getMaxMarks() { return maxMarks; }
    public void setMaxMarks(double maxMarks) { this.maxMarks = maxMarks; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getFormattedCreatedAt() {
        if (createdAt == null) return "N/A";
        return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    @Override
    public String toString() {
        return String.format("%s (Max: %.0f pts, Due: %s)", title, maxMarks, dueDate);
    }
}
