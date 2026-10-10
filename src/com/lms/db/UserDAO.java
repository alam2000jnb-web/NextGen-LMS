package com.lms.db;

import com.lms.exception.DatabaseOperationException;
import com.lms.model.Admin;
import com.lms.model.Instructor;
import com.lms.model.Student;
import com.lms.model.User;
import com.lms.model.UserRole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAO extends GenericDAO<User, Integer> {

    @Override
    public Optional<User> findById(Integer id) throws DatabaseOperationException {
        String sql = "SELECT * FROM users WHERE id = ?;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching user by ID " + id + ": " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<User> findByEmail(String email) throws DatabaseOperationException {
        String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?);";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error finding user by email: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<User> authenticate(String email, String password) throws DatabaseOperationException {
        String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?) AND password_hash = ? AND status = 'ACTIVE';";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email.trim());
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error authenticating user: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() throws DatabaseOperationException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id ASC;";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching all users: " + e.getMessage(), e);
        }
        return users;
    }

    public List<User> findByRole(UserRole role) throws DatabaseOperationException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY name ASC;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, role.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching users by role: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public User save(User user) throws DatabaseOperationException {
        String sql = "INSERT INTO users (name, email, password_hash, role, status, dept_or_major) VALUES (?, ?, ?, ?, ?, ?);";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPasswordHash());
            pstmt.setString(4, user.getRole().name());
            pstmt.setString(5, user.getStatus() != null ? user.getStatus() : "ACTIVE");

            String extra = "";
            if (user instanceof Instructor) extra = ((Instructor) user).getDepartment();
            else if (user instanceof Student) extra = ((Student) user).getMajor();
            pstmt.setString(6, extra);

            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
            }
            return user;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error creating user: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(User user) throws DatabaseOperationException {
        String sql = "UPDATE users SET name = ?, email = ?, password_hash = ?, role = ?, status = ?, dept_or_major = ? WHERE id = ?;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPasswordHash());
            pstmt.setString(4, user.getRole().name());
            pstmt.setString(5, user.getStatus());

            String extra = "";
            if (user instanceof Instructor) extra = ((Instructor) user).getDepartment();
            else if (user instanceof Student) extra = ((Student) user).getMajor();
            pstmt.setString(6, extra);

            pstmt.setInt(7, user.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating user #" + user.getId() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(Integer id) throws DatabaseOperationException {
        String sql = "DELETE FROM users WHERE id = ?;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error deleting user #" + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public long count() throws DatabaseOperationException {
        String sql = "SELECT COUNT(*) FROM users;";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error counting users: " + e.getMessage(), e);
        }
        return 0;
    }

    public long countByRole(UserRole role) throws DatabaseOperationException {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ?;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, role.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error counting users by role: " + e.getMessage(), e);
        }
        return 0;
    }

    /**
     * Demonstrates Polymorphic Object Creation based on database role value.
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String password = rs.getString("password_hash");
        String roleStr = rs.getString("role");
        String status = rs.getString("status");
        String extra = rs.getString("dept_or_major");

        UserRole role = UserRole.fromString(roleStr);
        User user;

        switch (role) {
            case ADMIN:
                Admin admin = new Admin(id, name, email, password);
                if (extra != null && !extra.isEmpty()) admin.setDepartment(extra);
                user = admin;
                break;
            case INSTRUCTOR:
                Instructor instructor = new Instructor(id, name, email, password);
                if (extra != null && !extra.isEmpty()) instructor.setDepartment(extra);
                user = instructor;
                break;
            case STUDENT:
            default:
                Student student = new Student(id, name, email, password);
                if (extra != null && !extra.isEmpty()) student.setMajor(extra);
                user = student;
                break;
        }

        user.setStatus(status);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
}
