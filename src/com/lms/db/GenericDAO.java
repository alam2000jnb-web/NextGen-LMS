package com.lms.db;

import com.lms.exception.DatabaseOperationException;
import com.lms.service.interfaces.Repository;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Generic Abstract DAO class demonstrating Generics, Template Method pattern,
 * and robust Exception Handling.
 *
 * @param <T>  Entity Type
 * @param <ID> Primary Key Type
 */
public abstract class GenericDAO<T, ID> implements Repository<T, ID> {

    protected Connection getConnection() throws DatabaseOperationException {
        try {
            return DatabaseManager.getInstance().getConnection();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to acquire database connection: " + e.getMessage(), e);
        }
    }

    /**
     * Executes a transactional operation with automatic commit and rollback.
     */
    @FunctionalInterface
    public interface DatabaseAction<R> {
        R execute(Connection conn) throws SQLException, DatabaseOperationException;
    }

    protected <R> R executeTransaction(DatabaseAction<R> action) throws DatabaseOperationException {
        Connection conn = null;
        boolean originalAutoCommit = true;
        try {
            conn = getConnection();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            R result = action.execute(conn);

            conn.commit();
            return result;
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Rollback failed: " + rollbackEx.getMessage());
                }
            }
            if (e instanceof DatabaseOperationException) {
                throw (DatabaseOperationException) e;
            }
            throw new DatabaseOperationException("Database transaction aborted: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(originalAutoCommit);
                } catch (SQLException ignored) {}
            }
        }
    }
}
