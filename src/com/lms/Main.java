package com.lms;

import com.lms.concurrent.SubmissionProcessor;
import com.lms.concurrent.SystemActivityMonitor;
import com.lms.db.DatabaseManager;
import com.lms.ui.LoginFrame;
import com.lms.util.UITheme;
import com.lms.web.LMSWebServer;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;

/**
 * Main Entry Point for Online Learning Management System (LMS).
 * Initializes FlatLaf Theme, SQLite Database, Multithreading Daemons,
 * Embedded Live Web Server (http://localhost:8080), and Desktop GUI.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   STARTING NEXTGEN ONLINE LEARNING SYSTEM (LMS) ");
        System.out.println("=================================================");

        // 1. Initialize SQLite Database & Seeds
        System.out.println("Initializing SQLite Database and Seed Data...");
        DatabaseManager.getInstance();

        // 2. Start Multithreaded Background Services
        System.out.println("Starting background Activity Monitoring & Submission Workers...");
        SystemActivityMonitor.getInstance().startMonitoring(2);
        SubmissionProcessor.getInstance();

        // 3. Start Embedded Live Web Server on Port 8080
        try {
            System.out.println("Starting Live Web Application on port 8080...");
            LMSWebServer.startServer();
            System.out.println(">>> LIVE WEB PORTAL ACTIVE: http://localhost:8080/");
        } catch (Exception e) {
            System.err.println("Warning: Could not start web server on 8080: " + e.getMessage());
        }

        // 4. Launch Desktop GUI if display environment is present
        if (!GraphicsEnvironment.isHeadless()) {
            UITheme.setupFlatLaf();
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
                System.out.println(">>> DESKTOP GUI LAUNCHED SUCCESSFULLY.");
            });
        } else {
            System.out.println("Headless environment detected. Live Web Server serving on http://localhost:8080/");
        }
    }
}
