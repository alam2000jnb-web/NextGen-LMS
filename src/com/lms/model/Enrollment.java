package com.lms.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Enrollment implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int courseId;
    private String courseCode;
    private String courseTitle;
    private int studentId;
    private String studentName;
    private LocalDateTime enrolledAt;
    private int progressPercent; // 0 to 100
    private String status;        // "ACTIVE", "COMPLETED", "DROPPED"
    private Double finalGrade;    // Optional final grade (e.g. 92.5)

    public Enrollment() {
        this.progressPercent = 0;
        this.status = "ACTIVE";
        this.enrolledAt = LocalDateTime.now();
    }

    public Enrollment(int id, int courseId, int studentId) {
        this.id = id;
        this.courseId = courseId;
        this.studentId = studentId;
        this.progressPercent = 0;
        this.status = "ACTIVE";
        this.enrolledAt = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public String getCourseCode() { return courseCode != null ? courseCode : "CRS-" + courseId; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseTitle() { return courseTitle != null ? courseTitle : "Course #" + courseId; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName != null ? studentName : "Student #" + studentId; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public LocalDateTime getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(LocalDateTime enrolledAt) { this.enrolledAt = enrolledAt; }

    public int getProgressPercent() { return progressPercent; }
    public void setProgressPercent(int progressPercent) { this.progressPercent = Math.min(100, Math.max(0, progressPercent)); }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getFinalGrade() { return finalGrade; }
    public void setFinalGrade(Double finalGrade) { this.finalGrade = finalGrade; }

    public String getFormattedEnrolledAt() {
        if (enrolledAt == null) return "N/A";
        return enrolledAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    @Override
    public String toString() {
        return String.format("%s enrolled in %s (%d%%)", getStudentName(), getCourseTitle(), progressPercent);
    }
}
