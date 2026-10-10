package com.lms.service.interfaces;

import com.lms.exception.LMSException;
import com.lms.model.Course;
import com.lms.model.CourseMaterial;
import com.lms.model.Assignment;
import java.util.List;

public interface CourseManageable {
    void addMaterial(int courseId, CourseMaterial material) throws LMSException;
    void addAssignment(int courseId, Assignment assignment) throws LMSException;
    List<Course> getManagedCourses(int instructorId) throws LMSException;
}
