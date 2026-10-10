package com.lms.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditLog implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private LocalDateTime timestamp;
    private int userId;
    private String userName;
    private String action;
    private String details;
    private String ipAddress;

    public AuditLog() {
        this.timestamp = LocalDateTime.now();
        this.ipAddress = "127.0.0.1";
    }

    public AuditLog(int id, int userId, String userName, String action, String details) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.action = action;
        this.details = details;
        this.ipAddress = "127.0.0.1";
        this.timestamp = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getFormattedTimestamp() {
        if (timestamp == null) return "N/A";
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public String toString() {
        return String.format("[%s] %s by %s: %s", getFormattedTimestamp(), action, userName, details);
    }
}
