package com.neptunedreams.skeleton.data;

import java.sql.SQLException;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/28/17
 * <p>Time: 6:52 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public interface Dao<E> {
  boolean createTableIfNeeded() throws SQLException;
  
  Collection<E> getAll(@Nullable Enum orderBy) throws SQLException;
  
  Collection<E> find(String text, @Nullable Enum orderBy) throws SQLException;
  
  Collection<E> findInField(String text, @NotNull Enum fieldName, @Nullable Enum orderBy) throws SQLException;

  /**
   * insert or update the entity.
   * @param entity The entity
   * @return True if the entity was new and was inserted, false if it updated an existing entry.
   * @throws SQLException Yeah, you know.
   */
  void save(E entity) throws SQLException;

  void insert(E entity) throws SQLException;

  void delete(E entity) throws SQLException;
  
  int getNewId(E entity) throws SQLException;
  
  int getNextId() throws SQLException;
}