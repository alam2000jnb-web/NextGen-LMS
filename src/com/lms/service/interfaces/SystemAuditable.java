package com.lms.service.interfaces;

public interface SystemAuditable {
    void logAction(int userId, String userName, String action, String details);
}
