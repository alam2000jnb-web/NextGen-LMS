package com.lms.db;

import com.lms.exception.DatabaseOperationException;
import com.lms.model.Enrollment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EnrollmentDAO extends GenericDAO<Enrollment, Integer> {

    @Override
    public Optional<Enrollment> findById(Integer id) throws DatabaseOperationException {
        String sql = "SELECT e.*, c.code AS course_code, c.title AS course_title, u.name AS student_name " +
                     "FROM enrollments e " +
                     "JOIN courses c ON e.course_id = c.id " +
                     "JOIN users u ON e.student_id = u.id " +
                     "WHERE e.id = ?;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching enrollment #" + id + ": " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public boolean isEnrolled(int studentId, int courseId) throws DatabaseOperationException {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND course_id = ?;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error checking enrollment status: " + e.getMessage(), e);
        }
        return false;
    }

    public List<Enrollment> findByStudentId(int studentId) throws DatabaseOperationException {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT e.*, c.code AS course_code, c.title AS course_title, u.name AS student_name " +
                     "FROM enrollments e " +
                     "JOIN courses c ON e.course_id = c.id " +
                     "JOIN users u ON e.student_id = u.id " +
                     "WHERE e.student_id = ? " +
                     "ORDER BY e.enrolled_at DESC;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching student enrollments: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Enrollment> findByCourseId(int courseId) throws DatabaseOperationException {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT e.*, c.code AS course_code, c.title AS course_title, u.name AS student_name " +
                     "FROM enrollments e " +
                     "JOIN courses c ON e.course_id = c.id " +
                     "JOIN users u ON e.student_id = u.id " +
                     "WHERE e.course_id = ? " +
                     "ORDER BY u.name ASC;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching course enrollments: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Enrollment> findAll() throws DatabaseOperationException {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT e.*, c.code AS course_code, c.title AS course_title, u.name AS student_name " +
                     "FROM enrollments e " +
                     "JOIN courses c ON e.course_id = c.id " +
                     "JOIN users u ON e.student_id = u.id " +
                     "ORDER BY e.enrolled_at DESC;";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching all enrollments: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public Enrollment save(Enrollment e) throws DatabaseOperationException {
        String sql = "INSERT INTO enrollments (course_id, student_id, progress_percent, status, final_grade) VALUES (?, ?, ?, ?, ?);";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, e.getCourseId());
            pstmt.setInt(2, e.getStudentId());
            pstmt.setInt(3, e.getProgressPercent());
            pstmt.setString(4, e.getStatus() != null ? e.getStatus() : "ACTIVE");
            if (e.getFinalGrade() != null) pstmt.setDouble(5, e.getFinalGrade());
            else pstmt.setNull(5, java.sql.Types.DOUBLE);

            pstmt.executeUpdate();
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) e.setId(keys.getInt(1));
            }
            return e;
        } catch (SQLException ex) {
            throw new DatabaseOperationException("Error creating enrollment: " + ex.getMessage(), ex);
        }
    }

    public boolean updateProgress(int enrollmentId, int progressPercent) throws DatabaseOperationException {
        String sql = "UPDATE enrollments SET progress_percent = ?, status = CASE WHEN ? >= 100 THEN 'COMPLETED' ELSE status END WHERE id = ?;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, progressPercent);
            pstmt.setInt(2, progressPercent);
            pstmt.setInt(3, enrollmentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating progress for enrollment #" + enrollmentId + ": " + e.getMessage(), e);
        }
    }

    public boolean updateFinalGrade(int enrollmentId, double grade) throws DatabaseOperationException {
        String sql = "UPDATE enrollments SET final_grade = ? WHERE id = ?;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, grade);
            pstmt.setInt(2, enrollmentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating final grade for enrollment #" + enrollmentId + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Enrollment e) throws DatabaseOperationException {
        String sql = "UPDATE enrollments SET progress_percent = ?, status = ?, final_grade = ? WHERE id = ?;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, e.getProgressPercent());
            pstmt.setString(2, e.getStatus());
            if (e.getFinalGrade() != null) pstmt.setDouble(3, e.getFinalGrade());
            else pstmt.setNull(3, java.sql.Types.DOUBLE);
            pstmt.setInt(4, e.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseOperationException("Error updating enrollment: " + ex.getMessage(), ex);
        }
    }

    @Override
    public boolean deleteById(Integer id) throws DatabaseOperationException {
        String sql = "DELETE FROM enrollments WHERE id = ?;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error deleting enrollment: " + e.getMessage(), e);
        }
    }

    @Override
    public long count() throws DatabaseOperationException {
        String sql = "SELECT COUNT(*) FROM enrollments;";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error counting enrollments: " + e.getMessage(), e);
        }
        return 0;
    }

    public double getAverageProgress() throws DatabaseOperationException {
        String sql = "SELECT AVG(progress_percent) FROM enrollments;";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error calculating average progress: " + e.getMessage(), e);
        }
        return 0.0;
    }

    public Map<String, Integer> getEnrollmentsPerCourseMap() throws DatabaseOperationException {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT c.title, COUNT(e.id) AS cnt " +
                     "FROM courses c " +
                     "LEFT JOIN enrollments e ON c.id = e.course_id " +
                     "GROUP BY c.id, c.title " +
                     "ORDER BY cnt DESC;";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("title"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching enrollments distribution: " + e.getMessage(), e);
        }
        return map;
    }

    private Enrollment mapResultSet(ResultSet rs) throws SQLException {
        Enrollment e = new Enrollment();
        e.setId(rs.getInt("id"));
        e.setCourseId(rs.getInt("course_id"));
        e.setCourseCode(rs.getString("course_code"));
        e.setCourseTitle(rs.getString("course_title"));
        e.setStudentId(rs.getInt("student_id"));
        e.setStudentName(rs.getString("student_name"));
        e.setProgressPercent(rs.getInt("progress_percent"));
        e.setStatus(rs.getString("status"));
        double grade = rs.getDouble("final_grade");
        if (!rs.wasNull()) e.setFinalGrade(grade);
        return e;
    }
}
