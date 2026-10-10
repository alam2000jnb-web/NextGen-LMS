package com.lms.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Submission implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int assignmentId;
    private String assignmentTitle;
    private int studentId;
    private String studentName;
    private String submissionText;
    private String fileAttachment;
    private Double marksObtained;
    private String feedback;
    private String status; // "PENDING", "GRADED", "RESUBMIT"
    private LocalDateTime submittedAt;
    private LocalDateTime gradedAt;

    public Submission() {
        this.status = "PENDING";
        this.submittedAt = LocalDateTime.now();
    }

    public Submission(int id, int assignmentId, int studentId, String submissionText, String fileAttachment) {
        this.id = id;
        this.assignmentId = assignmentId;
        this.studentId = studentId;
        this.submissionText = submissionText;
        this.fileAttachment = fileAttachment;
        this.status = "PENDING";
        this.submittedAt = LocalDateTime.now();
    }

    public boolean isGraded() {
        return "GRADED".equalsIgnoreCase(status);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAssignmentId() { return assignmentId; }
    public void setAssignmentId(int assignmentId) { this.assignmentId = assignmentId; }

    public String getAssignmentTitle() { return assignmentTitle != null ? assignmentTitle : "Assignment #" + assignmentId; }
    public void setAssignmentTitle(String assignmentTitle) { this.assignmentTitle = assignmentTitle; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName != null ? studentName : "Student #" + studentId; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getSubmissionText() { return submissionText; }
    public void setSubmissionText(String submissionText) { this.submissionText = submissionText; }

    public String getFileAttachment() { return fileAttachment; }
    public void setFileAttachment(String fileAttachment) { this.fileAttachment = fileAttachment; }

    public Double getMarksObtained() { return marksObtained; }
    public void setMarksObtained(Double marksObtained) { this.marksObtained = marksObtained; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getGradedAt() { return gradedAt; }
    public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }

    public String getFormattedSubmittedAt() {
        if (submittedAt == null) return "N/A";
        return submittedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public String getFormattedGradedAt() {
        if (gradedAt == null) return "Not graded yet";
        return gradedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    @Override
    public String toString() {
        return String.format("Submission #%d - %s by %s [%s]", id, getAssignmentTitle(), getStudentName(), status);
    }
}
