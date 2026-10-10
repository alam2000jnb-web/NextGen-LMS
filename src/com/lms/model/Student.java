package com.lms.model;

import java.util.Arrays;
import java.util.List;

/**
 * Student model demonstrating Inheritance and Polymorphism.
 */
public class Student extends User {
    private static final long serialVersionUID = 1L;

    private String studentCode = "STU-1001";
    private String major = "Computer Science";
    private int currentSemester = 4;
    private double gpa = 3.85;

    public Student() {
        super();
        this.role = UserRole.STUDENT;
    }

    public Student(int id, String name, String email, String passwordHash) {
        super(id, name, email, passwordHash, UserRole.STUDENT);
    }

    public Student(int id, String name, String email, String passwordHash, String studentCode, String major) {
        super(id, name, email, passwordHash, UserRole.STUDENT);
        this.studentCode = studentCode;
        this.major = major;
    }

    @Override
    public String getDashboardTitle() {
        return "Student Learning Portal & Progress Center";
    }

    @Override
    public List<String> getPermissions() {
        return Arrays.asList(
            "ENROLL_COURSES",
            "VIEW_MATERIALS",
            "DOWNLOAD_MATERIALS",
            "SUBMIT_ASSIGNMENTS",
            "VIEW_PROGRESS",
            "VIEW_GRADES_FEEDBACK"
        );
    }

    @Override
    public boolean canManageCourse(int courseId) {
        // Students cannot manage courses
        return false;
    }

    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public int getCurrentSemester() { return currentSemester; }
    public void setCurrentSemester(int currentSemester) { this.currentSemester = currentSemester; }

    public double getGpa() { return gpa; }
    public void setGpa(double gpa) { this.gpa = gpa; }
}
