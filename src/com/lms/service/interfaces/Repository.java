package com.lms.service.interfaces;

import com.lms.exception.DatabaseOperationException;
import java.util.List;
import java.util.Optional;

/**
 * Generic repository interface demonstrating Generics & Collections.
 *
 * @param <T>  The entity type
 * @param <ID> The primary key identifier type
 */
public interface Repository<T, ID> {
    Optional<T> findById(ID id) throws DatabaseOperationException;
    List<T> findAll() throws DatabaseOperationException;
    T save(T entity) throws DatabaseOperationException;
    boolean update(T entity) throws DatabaseOperationException;
    boolean deleteById(ID id) throws DatabaseOperationException;
    long count() throws DatabaseOperationException;
}
