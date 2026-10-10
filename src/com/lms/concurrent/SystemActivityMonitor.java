package com.lms.concurrent;

import com.lms.db.AuditLogDAO;
import com.lms.model.AuditLog;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Demonstrates Multithreading & Background Daemons (Rubric Requirement).
 * Monitors system events continuously and notifies UI listeners on Swing Event Dispatch Thread (EDT).
 */
public class SystemActivityMonitor {
    private static SystemActivityMonitor instance;

    public interface ActivityListener {
        void onNewActivities(List<AuditLog> recentLogs);
    }

    private final AuditLogDAO auditLogDAO = new AuditLogDAO();
    private final List<ActivityListener> listeners = Collections.synchronizedList(new ArrayList<>());
    private ScheduledExecutorService scheduler;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private int lastPolledLogId = 0;

    private SystemActivityMonitor() {}

    public static synchronized SystemActivityMonitor getInstance() {
        if (instance == null) {
            instance = new SystemActivityMonitor();
        }
        return instance;
    }

    public synchronized void startMonitoring(int intervalSeconds) {
        if (isRunning.get()) return;

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "LMS-ActivityMonitor-Daemon");
            t.setDaemon(true);
            return t;
        });

        isRunning.set(true);
        scheduler.scheduleWithFixedDelay(this::pollAndNotify, 1, intervalSeconds, TimeUnit.SECONDS);
    }

    public synchronized void stopMonitoring() {
        isRunning.set(false);
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    public void addListener(ActivityListener listener) {
        listeners.add(listener);
        // Immediately fetch current logs on register
        try {
            List<AuditLog> initialLogs = auditLogDAO.getRecentLogs(25);
            SwingUtilities.invokeLater(() -> listener.onNewActivities(initialLogs));
        } catch (Exception ignored) {}
    }

    public void removeListener(ActivityListener listener) {
        listeners.remove(listener);
    }

    private void pollAndNotify() {
        if (!isRunning.get()) return;
        try {
            List<AuditLog> recent = auditLogDAO.getRecentLogs(30);
            if (!recent.isEmpty()) {
                int latestId = recent.get(0).getId();
                if (latestId != lastPolledLogId) {
                    lastPolledLogId = latestId;
                    // Safely dispatch to EDT
                    SwingUtilities.invokeLater(() -> {
                        synchronized (listeners) {
                            for (ActivityListener listener : listeners) {
                                listener.onNewActivities(recent);
                            }
                        }
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("Activity monitor polling error: " + e.getMessage());
        }
    }
}
