package com.lms.service.interfaces;

import com.lms.exception.LMSException;
import com.lms.model.Submission;

public interface Gradable {
    void gradeSubmission(int submissionId, double marks, String feedback, int gradedByInstructorId) throws LMSException;
    double calculateFinalGrade(int studentId, int courseId) throws LMSException;
}
