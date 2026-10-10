package com.lms.db;

import com.lms.exception.DatabaseOperationException;
import com.lms.model.QuizQuestion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QuizDAO extends GenericDAO<QuizQuestion, Integer> {

    @Override
    public Optional<QuizQuestion> findById(Integer id) throws DatabaseOperationException {
        String sql = "SELECT * FROM quiz_questions WHERE id = ?;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching quiz question #" + id + ": " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public List<QuizQuestion> findByCourseId(int courseId) throws DatabaseOperationException {
        List<QuizQuestion> list = new ArrayList<>();
        String sql = "SELECT * FROM quiz_questions WHERE course_id = ? ORDER BY id ASC;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching quiz questions for course #" + courseId + ": " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<QuizQuestion> findAll() throws DatabaseOperationException {
        List<QuizQuestion> list = new ArrayList<>();
        String sql = "SELECT * FROM quiz_questions ORDER BY id ASC;";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching quiz questions: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public QuizQuestion save(QuizQuestion q) throws DatabaseOperationException {
        String sql = "INSERT INTO quiz_questions (course_id, question_text, option_a, option_b, option_c, option_d, correct_option, explanation) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, q.getCourseId());
            pstmt.setString(2, q.getQuestionText());
            pstmt.setString(3, q.getOptionA());
            pstmt.setString(4, q.getOptionB());
            pstmt.setString(5, q.getOptionC());
            pstmt.setString(6, q.getOptionD());
            pstmt.setString(7, q.getCorrectOption());
            pstmt.setString(8, q.getExplanation());
            pstmt.executeUpdate();
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) q.setId(keys.getInt(1));
            }
            return q;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error creating quiz question: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(QuizQuestion q) throws DatabaseOperationException {
        String sql = "UPDATE quiz_questions SET question_text = ?, option_a = ?, option_b = ?, option_c = ?, option_d = ?, correct_option = ?, explanation = ? WHERE id = ?;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, q.getQuestionText());
            pstmt.setString(2, q.getOptionA());
            pstmt.setString(3, q.getOptionB());
            pstmt.setString(4, q.getOptionC());
            pstmt.setString(5, q.getOptionD());
            pstmt.setString(6, q.getCorrectOption());
            pstmt.setString(7, q.getExplanation());
            pstmt.setInt(8, q.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating quiz question: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(Integer id) throws DatabaseOperationException {
        String sql = "DELETE FROM quiz_questions WHERE id = ?;";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error deleting quiz question: " + e.getMessage(), e);
        }
    }

    @Override
    public long count() throws DatabaseOperationException {
        String sql = "SELECT COUNT(*) FROM quiz_questions;";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error counting quiz questions: " + e.getMessage(), e);
        }
        return 0;
    }

    private QuizQuestion mapResultSet(ResultSet rs) throws SQLException {
        QuizQuestion q = new QuizQuestion();
        q.setId(rs.getInt("id"));
        q.setCourseId(rs.getInt("course_id"));
        q.setQuestionText(rs.getString("question_text"));
        q.setOptionA(rs.getString("option_a"));
        q.setOptionB(rs.getString("option_b"));
        q.setOptionC(rs.getString("option_c"));
        q.setOptionD(rs.getString("option_d"));
        q.setCorrectOption(rs.getString("correct_option"));
        q.setExplanation(rs.getString("explanation"));
        return q;
    }
}
