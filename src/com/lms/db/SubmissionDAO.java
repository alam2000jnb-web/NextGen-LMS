package com.lms.db;

import com.lms.exception.DatabaseOperationException;
import com.lms.model.Submission;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SubmissionDAO extends GenericDAO<Submission, Integer> {

    @Override
    public Optional<Submission> findById(Integer id) throws DatabaseOperationException {
        String sql = "SELECT s.*, a.title AS assignment_title, u.name AS student_name " +
                     "FROM submissions s " +
                     "JOIN assignments a ON s.assignment_id = a.id " +
                     "JOIN users u ON s.student_id = u.id " +
                     "WHERE s.id = ?;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching submission #" + id + ": " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public List<Submission> findByAssignmentId(int assignmentId) throws DatabaseOperationException {
        List<Submission> list = new ArrayList<>();
        String sql = "SELECT s.*, a.title AS assignment_title, u.name AS student_name " +
                     "FROM submissions s " +
                     "JOIN assignments a ON s.assignment_id = a.id " +
                     "JOIN users u ON s.student_id = u.id " +
                     "WHERE s.assignment_id = ? " +
                     "ORDER BY s.submitted_at DESC;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, assignmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching submissions for assignment #" + assignmentId + ": " + e.getMessage(), e);
        }
        return list;
    }

    public List<Submission> findByStudentId(int studentId) throws DatabaseOperationException {
        List<Submission> list = new ArrayList<>();
        String sql = "SELECT s.*, a.title AS assignment_title, u.name AS student_name " +
                     "FROM submissions s " +
                     "JOIN assignments a ON s.assignment_id = a.id " +
                     "JOIN users u ON s.student_id = u.id " +
                     "WHERE s.student_id = ? " +
                     "ORDER BY s.submitted_at DESC;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching student submissions: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Submission> findByCourseId(int courseId) throws DatabaseOperationException {
        List<Submission> list = new ArrayList<>();
        String sql = "SELECT s.*, a.title AS assignment_title, u.name AS student_name " +
                     "FROM submissions s " +
                     "JOIN assignments a ON s.assignment_id = a.id " +
                     "JOIN users u ON s.student_id = u.id " +
                     "WHERE a.course_id = ? " +
                     "ORDER BY s.submitted_at DESC;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching course submissions: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Submission> findAll() throws DatabaseOperationException {
        List<Submission> list = new ArrayList<>();
        String sql = "SELECT s.*, a.title AS assignment_title, u.name AS student_name " +
                     "FROM submissions s " +
                     "JOIN assignments a ON s.assignment_id = a.id " +
                     "JOIN users u ON s.student_id = u.id " +
                     "ORDER BY s.submitted_at DESC;";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching all submissions: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public Submission save(Submission s) throws DatabaseOperationException {
        String sql = "INSERT INTO submissions (assignment_id, student_id, submission_text, file_attachment, marks_obtained, feedback, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?);";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, s.getAssignmentId());
            pstmt.setInt(2, s.getStudentId());
            pstmt.setString(3, s.getSubmissionText());
            pstmt.setString(4, s.getFileAttachment());
            if (s.getMarksObtained() != null) pstmt.setDouble(5, s.getMarksObtained());
            else pstmt.setNull(5, java.sql.Types.DOUBLE);
            pstmt.setString(6, s.getFeedback());
            pstmt.setString(7, s.getStatus() != null ? s.getStatus() : "PENDING");

            pstmt.executeUpdate();
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) s.setId(keys.getInt(1));
            }
            return s;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error saving submission: " + e.getMessage(), e);
        }
    }

    public boolean gradeSubmission(int submissionId, double marks, String feedback) throws DatabaseOperationException {
        String sql = "UPDATE submissions SET marks_obtained = ?, feedback = ?, status = 'GRADED', graded_at = ? WHERE id = ?;";
        String nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, marks);
            pstmt.setString(2, feedback);
            pstmt.setString(3, nowStr);
            pstmt.setInt(4, submissionId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error grading submission #" + submissionId + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Submission s) throws DatabaseOperationException {
        String sql = "UPDATE submissions SET submission_text = ?, file_attachment = ?, marks_obtained = ?, feedback = ?, status = ? WHERE id = ?;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, s.getSubmissionText());
            pstmt.setString(2, s.getFileAttachment());
            if (s.getMarksObtained() != null) pstmt.setDouble(3, s.getMarksObtained());
            else pstmt.setNull(3, java.sql.Types.DOUBLE);
            pstmt.setString(4, s.getFeedback());
            pstmt.setString(5, s.getStatus());
            pstmt.setInt(6, s.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating submission: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(Integer id) throws DatabaseOperationException {
        String sql = "DELETE FROM submissions WHERE id = ?;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error deleting submission: " + e.getMessage(), e);
        }
    }

    @Override
    public long count() throws DatabaseOperationException {
        String sql = "SELECT COUNT(*) FROM submissions;";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error counting submissions: " + e.getMessage(), e);
        }
        return 0;
    }

    public long countPending() throws DatabaseOperationException {
        String sql = "SELECT COUNT(*) FROM submissions WHERE status = 'PENDING';";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error counting pending submissions: " + e.getMessage(), e);
        }
        return 0;
    }

    private Submission mapResultSet(ResultSet rs) throws SQLException {
        Submission s = new Submission();
        s.setId(rs.getInt("id"));
        s.setAssignmentId(rs.getInt("assignment_id"));
        s.setAssignmentTitle(rs.getString("assignment_title"));
        s.setStudentId(rs.getInt("student_id"));
        s.setStudentName(rs.getString("student_name"));
        s.setSubmissionText(rs.getString("submission_text"));
        s.setFileAttachment(rs.getString("file_attachment"));
        double marks = rs.getDouble("marks_obtained");
        if (!rs.wasNull()) s.setMarksObtained(marks);
        s.setFeedback(rs.getString("feedback"));
        s.setStatus(rs.getString("status"));
        return s;
    }
}
