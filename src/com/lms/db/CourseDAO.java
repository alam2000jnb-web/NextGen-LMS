package com.lms.db;

import com.lms.exception.DatabaseOperationException;
import com.lms.model.Course;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CourseDAO extends GenericDAO<Course, Integer> {

    @Override
    public Optional<Course> findById(Integer id) throws DatabaseOperationException {
        String sql = "SELECT c.*, u.name AS instructor_name " +
                     "FROM courses c " +
                     "JOIN users u ON c.instructor_id = u.id " +
                     "WHERE c.id = ?;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCourse(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error finding course #" + id + ": " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<Course> findByCode(String code) throws DatabaseOperationException {
        String sql = "SELECT c.*, u.name AS instructor_name " +
                     "FROM courses c " +
                     "JOIN users u ON c.instructor_id = u.id " +
                     "WHERE LOWER(c.code) = LOWER(?);";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCourse(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error finding course by code: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Course> findAll() throws DatabaseOperationException {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT c.*, u.name AS instructor_name " +
                     "FROM courses c " +
                     "JOIN users u ON c.instructor_id = u.id " +
                     "ORDER BY c.id ASC;";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToCourse(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching courses: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Course> findByInstructorId(int instructorId) throws DatabaseOperationException {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT c.*, u.name AS instructor_name " +
                     "FROM courses c " +
                     "JOIN users u ON c.instructor_id = u.id " +
                     "WHERE c.instructor_id = ? " +
                     "ORDER BY c.id ASC;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, instructorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCourse(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching instructor's courses: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Course> searchCourses(String keyword, String category) throws DatabaseOperationException {
        List<Course> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT c.*, u.name AS instructor_name " +
                "FROM courses c " +
                "JOIN users u ON c.instructor_id = u.id " +
                "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (c.title LIKE ? OR c.code LIKE ? OR c.description LIKE ?) ");
            String term = "%" + keyword.trim() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
        }
        if (category != null && !category.equalsIgnoreCase("All Categories")) {
            sql.append("AND c.category = ? ");
            params.add(category);
        }
        sql.append("ORDER BY c.id ASC;");

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCourse(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error searching courses: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public Course save(Course course) throws DatabaseOperationException {
        String sql = "INSERT INTO courses (code, title, description, category, instructor_id, max_seats, enrolled_count, syllabus, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, course.getCode());
            pstmt.setString(2, course.getTitle());
            pstmt.setString(3, course.getDescription());
            pstmt.setString(4, course.getCategory());
            pstmt.setInt(5, course.getInstructorId());
            pstmt.setInt(6, course.getMaxSeats());
            pstmt.setInt(7, course.getEnrolledCount());
            pstmt.setString(8, course.getSyllabus());
            pstmt.setString(9, course.getStatus() != null ? course.getStatus() : "ACTIVE");

            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    course.setId(generatedKeys.getInt(1));
                }
            }
            return course;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error creating course: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Course course) throws DatabaseOperationException {
        String sql = "UPDATE courses SET code = ?, title = ?, description = ?, category = ?, " +
                     "instructor_id = ?, max_seats = ?, enrolled_count = ?, syllabus = ?, status = ? " +
                     "WHERE id = ?;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, course.getCode());
            pstmt.setString(2, course.getTitle());
            pstmt.setString(3, course.getDescription());
            pstmt.setString(4, course.getCategory());
            pstmt.setInt(5, course.getInstructorId());
            pstmt.setInt(6, course.getMaxSeats());
            pstmt.setInt(7, course.getEnrolledCount());
            pstmt.setString(8, course.getSyllabus());
            pstmt.setString(9, course.getStatus());
            pstmt.setInt(10, course.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating course #" + course.getId() + ": " + e.getMessage(), e);
        }
    }

    public boolean incrementEnrolledCount(int courseId) throws DatabaseOperationException {
        String sql = "UPDATE courses SET enrolled_count = enrolled_count + 1 WHERE id = ? AND enrolled_count < max_seats;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error incrementing enrollment for course #" + courseId + ": " + e.getMessage(), e);
        }
    }

    public boolean decrementEnrolledCount(int courseId) throws DatabaseOperationException {
        String sql = "UPDATE courses SET enrolled_count = MAX(0, enrolled_count - 1) WHERE id = ?;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error decrementing enrollment for course #" + courseId + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(Integer id) throws DatabaseOperationException {
        String sql = "DELETE FROM courses WHERE id = ?;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error deleting course #" + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public long count() throws DatabaseOperationException {
        String sql = "SELECT COUNT(*) FROM courses;";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error counting courses: " + e.getMessage(), e);
        }
        return 0;
    }

    private Course mapResultSetToCourse(ResultSet rs) throws SQLException {
        Course c = new Course();
        c.setId(rs.getInt("id"));
        c.setCode(rs.getString("code"));
        c.setTitle(rs.getString("title"));
        c.setDescription(rs.getString("description"));
        c.setCategory(rs.getString("category"));
        c.setInstructorId(rs.getInt("instructor_id"));
        c.setInstructorName(rs.getString("instructor_name"));
        c.setMaxSeats(rs.getInt("max_seats"));
        c.setEnrolledCount(rs.getInt("enrolled_count"));
        c.setSyllabus(rs.getString("syllabus"));
        c.setStatus(rs.getString("status"));
        return c;
    }
}
