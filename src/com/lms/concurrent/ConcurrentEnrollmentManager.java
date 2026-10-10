package com.lms.concurrent;

import com.lms.db.CourseDAO;
import com.lms.db.EnrollmentDAO;
import com.lms.db.AuditLogDAO;
import com.lms.exception.EnrollmentException;
import com.lms.model.Course;
import com.lms.model.Enrollment;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Demonstrates Multithreading & Synchronization (Rubric Requirement).
 * Uses fine-grained ReentrantLocks mapped per course ID to synchronize concurrent
 * enrollment attempts and prevent race conditions on limited seats.
 */
public class ConcurrentEnrollmentManager {
    private static ConcurrentEnrollmentManager instance;

    private final CourseDAO courseDAO = new CourseDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    // Fine-grained lock per course to allow high concurrency across different courses
    private final ConcurrentHashMap<Integer, ReentrantLock> courseLocks = new ConcurrentHashMap<>();

    private ConcurrentEnrollmentManager() {}

    public static synchronized ConcurrentEnrollmentManager getInstance() {
        if (instance == null) {
            instance = new ConcurrentEnrollmentManager();
        }
        return instance;
    }

    private ReentrantLock getLockForCourse(int courseId) {
        return courseLocks.computeIfAbsent(courseId, id -> new ReentrantLock(true)); // fair lock
    }

    /**
     * Synchronized and thread-safe enrollment procedure.
     */
    public Enrollment enrollStudent(int studentId, String studentName, int courseId) throws EnrollmentException {
        ReentrantLock lock = getLockForCourse(courseId);
        lock.lock();
        try {
            // Check if student already enrolled
            if (enrollmentDAO.isEnrolled(studentId, courseId)) {
                throw new EnrollmentException("You are already enrolled in this course!");
            }

            // Fetch course details
            Optional<Course> courseOpt = courseDAO.findById(courseId);
            if (!courseOpt.isPresent()) {
                throw new EnrollmentException("Course #" + courseId + " does not exist.");
            }

            Course course = courseOpt.get();
            if (course.isFull()) {
                throw new EnrollmentException(String.format("Course '%s' is full (%d/%d seats filled)!",
                        course.getTitle(), course.getEnrolledCount(), course.getMaxSeats()));
            }

            // Perform synchronized database operations
            boolean seatReserved = courseDAO.incrementEnrolledCount(courseId);
            if (!seatReserved) {
                throw new EnrollmentException("Failed to reserve seat. Course reached full capacity!");
            }

            Enrollment newEnrollment = new Enrollment();
            newEnrollment.setCourseId(courseId);
            newEnrollment.setStudentId(studentId);
            newEnrollment.setStudentName(studentName);
            newEnrollment.setCourseTitle(course.getTitle());
            newEnrollment.setProgressPercent(0);
            newEnrollment.setStatus("ACTIVE");

            Enrollment saved = enrollmentDAO.save(newEnrollment);

            // Audit log
            auditLogDAO.log(studentId, studentName, "ENROLLMENT_SUCCESS",
                    "Successfully enrolled in " + course.getTitle() + " (Course #" + courseId + ")");

            return saved;
        } catch (EnrollmentException ee) {
            throw ee;
        } catch (Exception ex) {
            throw new EnrollmentException("Enrollment transaction failed: " + ex.getMessage());
        } finally {
            lock.unlock();
        }
    }

    /**
     * Thread-safe course drop operation.
     */
    public boolean dropCourse(int enrollmentId, int courseId, int studentId, String studentName) throws EnrollmentException {
        ReentrantLock lock = getLockForCourse(courseId);
        lock.lock();
        try {
            boolean deleted = enrollmentDAO.deleteById(enrollmentId);
            if (deleted) {
                courseDAO.decrementEnrolledCount(courseId);
                auditLogDAO.log(studentId, studentName, "COURSE_DROPPED",
                        "Dropped course enrollment #" + enrollmentId);
            }
            return deleted;
        } catch (Exception ex) {
            throw new EnrollmentException("Failed to drop course: " + ex.getMessage());
        } finally {
            lock.unlock();
        }
    }
}
