package com.lms.db;

import com.lms.exception.DatabaseOperationException;
import com.lms.model.SystemSetting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SystemSettingDAO extends GenericDAO<SystemSetting, String> {

    @Override
    public Optional<SystemSetting> findById(String key) throws DatabaseOperationException {
        String sql = "SELECT * FROM system_settings WHERE setting_key = ?;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, key);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching setting '" + key + "': " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public String getValue(String key, String defaultValue) {
        try {
            Optional<SystemSetting> s = findById(key);
            return s.map(SystemSetting::getValue).orElse(defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    public List<SystemSetting> findAll() throws DatabaseOperationException {
        List<SystemSetting> list = new ArrayList<>();
        String sql = "SELECT * FROM system_settings ORDER BY setting_key ASC;";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching system settings: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public SystemSetting save(SystemSetting setting) throws DatabaseOperationException {
        String sql = "INSERT OR REPLACE INTO system_settings (setting_key, setting_value, description, category) VALUES (?, ?, ?, ?);";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, setting.getKey());
            pstmt.setString(2, setting.getValue());
            pstmt.setString(3, setting.getDescription());
            pstmt.setString(4, setting.getCategory());
            pstmt.executeUpdate();
            return setting;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error saving setting: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(SystemSetting setting) throws DatabaseOperationException {
        String sql = "UPDATE system_settings SET setting_value = ?, description = ?, category = ? WHERE setting_key = ?;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, setting.getValue());
            pstmt.setString(2, setting.getDescription());
            pstmt.setString(3, setting.getCategory());
            pstmt.setString(4, setting.getKey());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating setting: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(String key) throws DatabaseOperationException {
        String sql = "DELETE FROM system_settings WHERE setting_key = ?;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, key);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error deleting setting: " + e.getMessage(), e);
        }
    }

    @Override
    public long count() throws DatabaseOperationException {
        String sql = "SELECT COUNT(*) FROM system_settings;";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error counting settings: " + e.getMessage(), e);
        }
        return 0;
    }

    private SystemSetting mapResultSet(ResultSet rs) throws SQLException {
        return new SystemSetting(
                rs.getString("setting_key"),
                rs.getString("setting_value"),
                rs.getString("description"),
                rs.getString("category")
        );
    }
}
