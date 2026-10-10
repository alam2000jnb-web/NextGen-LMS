package com.lms.concurrent;

import com.lms.db.AuditLogDAO;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates Multithreading & Synchronization (Producer-Consumer pattern).
 * Asynchronously processes student submissions and dispatches background grading notifications.
 */
public class SubmissionProcessor {
    private static SubmissionProcessor instance;

    public static class SubmissionTask {
        public final int submissionId;
        public final int studentId;
        public final String studentName;
        public final String assignmentTitle;
        public final String taskType; // "SUBMISSION_VERIFICATION", "GRADE_NOTIFICATION"

        public SubmissionTask(int submissionId, int studentId, String studentName, String assignmentTitle, String taskType) {
            this.submissionId = submissionId;
            this.studentId = studentId;
            this.studentName = studentName;
            this.assignmentTitle = assignmentTitle;
            this.taskType = taskType;
        }
    }

    private final BlockingQueue<SubmissionTask> queue = new LinkedBlockingQueue<>();
    private final ExecutorService workerPool = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "LMS-SubmissionWorker");
        t.setDaemon(true);
        return t;
    });

    private final AuditLogDAO auditLogDAO = new AuditLogDAO();
    private final AtomicInteger processedCount = new AtomicInteger(0);

    private SubmissionProcessor() {
        startWorkerConsumers();
    }

    public static synchronized SubmissionProcessor getInstance() {
        if (instance == null) {
            instance = new SubmissionProcessor();
        }
        return instance;
    }

    private void startWorkerConsumers() {
        for (int i = 0; i < 2; i++) {
            workerPool.submit(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        SubmissionTask task = queue.take(); // Thread-safe BlockingQueue
                        processTask(task);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception ex) {
                        System.err.println("Worker error processing submission: " + ex.getMessage());
                    }
                }
            });
        }
    }

    private void processTask(SubmissionTask task) {
        // Simulate automated validation / indexing / email notification
        try {
            Thread.sleep(500); // 500ms background work
        } catch (InterruptedException ignored) {}

        processedCount.incrementAndGet();
        if ("SUBMISSION_VERIFICATION".equals(task.taskType)) {
            auditLogDAO.log(task.studentId, task.studentName, "SUBMISSION_PROCESSED",
                    "Automated integrity check passed for assignment: " + task.assignmentTitle);
        } else if ("GRADE_NOTIFICATION".equals(task.taskType)) {
            auditLogDAO.log(task.studentId, task.studentName, "GRADE_NOTIFIED",
                    "Notification generated for graded assignment: " + task.assignmentTitle);
        }
    }

    public void submitTask(SubmissionTask task) {
        queue.offer(task);
    }

    public int getProcessedCount() {
        return processedCount.get();
    }
}
