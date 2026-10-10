package com.lms.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CourseMaterial implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int courseId;
    private String title;
    private String contentType; // "DOCUMENT", "VIDEO_NOTES", "SLIDES", "READING", "CODE_LAB"
    private String contentData; // Text / markdown / description / summary
    private String fileUrl;     // Downloadable link or resource name
    private LocalDateTime uploadedAt;

    public CourseMaterial() {
        this.uploadedAt = LocalDateTime.now();
        this.contentType = "DOCUMENT";
    }

    public CourseMaterial(int id, int courseId, String title, String contentType, String contentData, String fileUrl) {
        this.id = id;
        this.courseId = courseId;
        this.title = title;
        this.contentType = contentType;
        this.contentData = contentData;
        this.fileUrl = fileUrl;
        this.uploadedAt = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getContentData() { return contentData; }
    public void setContentData(String contentData) { this.contentData = contentData; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public String getFormattedDate() {
        if (uploadedAt == null) return "N/A";
        return uploadedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    @Override
    public String toString() {
        return String.format("[%s] %s", contentType, title);
    }
}
