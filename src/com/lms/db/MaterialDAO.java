package com.lms.db;

import com.lms.exception.DatabaseOperationException;
import com.lms.model.CourseMaterial;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MaterialDAO extends GenericDAO<CourseMaterial, Integer> {

    @Override
    public Optional<CourseMaterial> findById(Integer id) throws DatabaseOperationException {
        String sql = "SELECT * FROM materials WHERE id = ?;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching material #" + id + ": " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public List<CourseMaterial> findByCourseId(int courseId) throws DatabaseOperationException {
        List<CourseMaterial> list = new ArrayList<>();
        String sql = "SELECT * FROM materials WHERE course_id = ? ORDER BY id ASC;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching materials for course #" + courseId + ": " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<CourseMaterial> findAll() throws DatabaseOperationException {
        List<CourseMaterial> list = new ArrayList<>();
        String sql = "SELECT * FROM materials ORDER BY id ASC;";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching all materials: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public CourseMaterial save(CourseMaterial m) throws DatabaseOperationException {
        String sql = "INSERT INTO materials (course_id, title, content_type, content_data, file_url) VALUES (?, ?, ?, ?, ?);";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, m.getCourseId());
            pstmt.setString(2, m.getTitle());
            pstmt.setString(3, m.getContentType());
            pstmt.setString(4, m.getContentData());
            pstmt.setString(5, m.getFileUrl());
            pstmt.executeUpdate();
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) m.setId(keys.getInt(1));
            }
            return m;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error creating material: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(CourseMaterial m) throws DatabaseOperationException {
        String sql = "UPDATE materials SET title = ?, content_type = ?, content_data = ?, file_url = ? WHERE id = ?;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, m.getTitle());
            pstmt.setString(2, m.getContentType());
            pstmt.setString(3, m.getContentData());
            pstmt.setString(4, m.getFileUrl());
            pstmt.setInt(5, m.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating material: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(Integer id) throws DatabaseOperationException {
        String sql = "DELETE FROM materials WHERE id = ?;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error deleting material: " + e.getMessage(), e);
        }
    }

    @Override
    public long count() throws DatabaseOperationException {
        String sql = "SELECT COUNT(*) FROM materials;";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error counting materials: " + e.getMessage(), e);
        }
        return 0;
    }

    private CourseMaterial mapResultSet(ResultSet rs) throws SQLException {
        CourseMaterial m = new CourseMaterial();
        m.setId(rs.getInt("id"));
        m.setCourseId(rs.getInt("course_id"));
        m.setTitle(rs.getString("title"));
        m.setContentType(rs.getString("content_type"));
        m.setContentData(rs.getString("content_data"));
        m.setFileUrl(rs.getString("file_url"));
        return m;
    }
}
