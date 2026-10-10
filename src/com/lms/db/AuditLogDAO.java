package com.lms.db;

import com.lms.exception.DatabaseOperationException;
import com.lms.model.AuditLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AuditLogDAO extends GenericDAO<AuditLog, Integer> {

    public void log(int userId, String userName, String action, String details) {
        String sql = "INSERT INTO audit_logs (user_id, user_name, action, details) VALUES (?, ?, ?, ?);";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, userName);
            pstmt.setString(3, action);
            pstmt.setString(4, details);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Failed to write audit log: " + e.getMessage());
        }
    }

    @Override
    public Optional<AuditLog> findById(Integer id) throws DatabaseOperationException {
        String sql = "SELECT * FROM audit_logs WHERE id = ?;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching log #" + id + ": " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public List<AuditLog> getRecentLogs(int limit) throws DatabaseOperationException {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs ORDER BY id DESC LIMIT ?;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching recent audit logs: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<AuditLog> findAll() throws DatabaseOperationException {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs ORDER BY id DESC;";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching audit logs: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public AuditLog save(AuditLog log) throws DatabaseOperationException {
        String sql = "INSERT INTO audit_logs (user_id, user_name, action, details, ip_address) VALUES (?, ?, ?, ?, ?);";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, log.getUserId());
            pstmt.setString(2, log.getUserName());
            pstmt.setString(3, log.getAction());
            pstmt.setString(4, log.getDetails());
            pstmt.setString(5, log.getIpAddress() != null ? log.getIpAddress() : "127.0.0.1");
            pstmt.executeUpdate();
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) log.setId(keys.getInt(1));
            }
            return log;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error inserting audit log: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(AuditLog log) {
        // Logs are append-only by design
        return false;
    }

    @Override
    public boolean deleteById(Integer id) throws DatabaseOperationException {
        String sql = "DELETE FROM audit_logs WHERE id = ?;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error deleting audit log: " + e.getMessage(), e);
        }
    }

    @Override
    public long count() throws DatabaseOperationException {
        String sql = "SELECT COUNT(*) FROM audit_logs;";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error counting audit logs: " + e.getMessage(), e);
        }
        return 0;
    }

    private AuditLog mapResultSet(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setId(rs.getInt("id"));
        log.setUserId(rs.getInt("user_id"));
        log.setUserName(rs.getString("user_name"));
        log.setAction(rs.getString("action"));
        log.setDetails(rs.getString("details"));
        log.setIpAddress(rs.getString("ip_address"));
        log.setTimestamp(LocalDateTime.now());
        return log;
    }
}
