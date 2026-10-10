package com.lms.db;

import com.lms.exception.DatabaseOperationException;
import com.lms.model.Assignment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AssignmentDAO extends GenericDAO<Assignment, Integer> {

    @Override
    public Optional<Assignment> findById(Integer id) throws DatabaseOperationException {
        String sql = "SELECT a.*, c.title AS course_title " +
                     "FROM assignments a " +
                     "JOIN courses c ON a.course_id = c.id " +
                     "WHERE a.id = ?;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching assignment #" + id + ": " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public List<Assignment> findByCourseId(int courseId) throws DatabaseOperationException {
        List<Assignment> list = new ArrayList<>();
        String sql = "SELECT a.*, c.title AS course_title " +
                     "FROM assignments a " +
                     "JOIN courses c ON a.course_id = c.id " +
                     "WHERE a.course_id = ? " +
                     "ORDER BY a.due_date ASC;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching assignments for course #" + courseId + ": " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Assignment> findAll() throws DatabaseOperationException {
        List<Assignment> list = new ArrayList<>();
        String sql = "SELECT a.*, c.title AS course_title " +
                     "FROM assignments a " +
                     "JOIN courses c ON a.course_id = c.id " +
                     "ORDER BY a.id ASC;";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching assignments: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public Assignment save(Assignment a) throws DatabaseOperationException {
        String sql = "INSERT INTO assignments (course_id, title, description, max_marks, due_date) VALUES (?, ?, ?, ?, ?);";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, a.getCourseId());
            pstmt.setString(2, a.getTitle());
            pstmt.setString(3, a.getDescription());
            pstmt.setDouble(4, a.getMaxMarks());
            pstmt.setString(5, a.getDueDate());
            pstmt.executeUpdate();
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) a.setId(keys.getInt(1));
            }
            return a;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error creating assignment: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Assignment a) throws DatabaseOperationException {
        String sql = "UPDATE assignments SET title = ?, description = ?, max_marks = ?, due_date = ? WHERE id = ?;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, a.getTitle());
            pstmt.setString(2, a.getDescription());
            pstmt.setDouble(3, a.getMaxMarks());
            pstmt.setString(4, a.getDueDate());
            pstmt.setInt(5, a.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating assignment: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(Integer id) throws DatabaseOperationException {
        String sql = "DELETE FROM assignments WHERE id = ?;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error deleting assignment: " + e.getMessage(), e);
        }
    }

    @Override
    public long count() throws DatabaseOperationException {
        String sql = "SELECT COUNT(*) FROM assignments;";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error counting assignments: " + e.getMessage(), e);
        }
        return 0;
    }

    private Assignment mapResultSet(ResultSet rs) throws SQLException {
        Assignment a = new Assignment();
        a.setId(rs.getInt("id"));
        a.setCourseId(rs.getInt("course_id"));
        a.setCourseTitle(rs.getString("course_title"));
        a.setTitle(rs.getString("title"));
        a.setDescription(rs.getString("description"));
        a.setMaxMarks(rs.getDouble("max_marks"));
        a.setDueDate(rs.getString("due_date"));
        return a;
    }
}
