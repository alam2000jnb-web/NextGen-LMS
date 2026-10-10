package com.lms.model;

import java.util.Arrays;
import java.util.List;

/**
 * Instructor model demonstrating Inheritance and Polymorphism.
 */
public class Instructor extends User {
    private static final long serialVersionUID = 1L;

    private String department = "Computer Science & Engineering";
    private String qualification = "Ph.D. in Computer Science";
    private String officeHours = "Mon-Wed 10:00 - 12:00";

    public Instructor() {
        super();
        this.role = UserRole.INSTRUCTOR;
    }

    public Instructor(int id, String name, String email, String passwordHash) {
        super(id, name, email, passwordHash, UserRole.INSTRUCTOR);
    }

    public Instructor(int id, String name, String email, String passwordHash, String department, String qualification) {
        super(id, name, email, passwordHash, UserRole.INSTRUCTOR);
        this.department = department;
        this.qualification = qualification;
    }

    @Override
    public String getDashboardTitle() {
        return "Instructor Course & Evaluation Suite";
    }

    @Override
    public List<String> getPermissions() {
        return Arrays.asList(
            "CREATE_COURSES",
            "EDIT_MY_COURSES",
            "CREATE_ASSIGNMENTS",
            "GRADE_SUBMISSIONS",
            "VIEW_STUDENT_PERFORMANCE",
            "VIEW_COURSE_STATS"
        );
    }

    @Override
    public boolean canManageCourse(int courseId) {
        // Will be checked against instructor ID in course service
        return true;
    }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public String getOfficeHours() { return officeHours; }
    public void setOfficeHours(String officeHours) { this.officeHours = officeHours; }
}
