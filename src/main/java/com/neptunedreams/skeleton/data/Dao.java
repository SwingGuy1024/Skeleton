package com.neptunedreams.skeleton.data;

import java.sql.SQLException;
import java.util.Collection;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/28/17
 * <p>Time: 6:52 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public interface Dao<E, PK> {
  boolean createTableIfNeeded() throws SQLException;
  
  Collection<E> getAll(@Nullable SiteField orderBy) throws SQLException;
  
  Collection<E> find(String text, @Nullable SiteField orderBy) throws SQLException;
  Collection<E> findAny(@Nullable SiteField orderBy, String... text) throws SQLException;
  Collection<E> findAll(@Nullable SiteField orderBy, String... text) throws SQLException;

  Collection<E> findInField(String text, @NonNull SiteField findBy, @Nullable SiteField orderBy) throws SQLException;
  Collection<E> findAnyInField(@NonNull SiteField findBy, @Nullable SiteField orderBy, String... text) throws SQLException;
  Collection<E> findAllInField(@NonNull SiteField findBy, @Nullable SiteField orderBy, String... text) throws SQLException;

//  E newEmptyRecord();

  /**
   * insert or update the entity.
   * @param entity The entity
//   * @return True if the entity was new and was inserted, false if it updated an existing entry.
   * @throws SQLException Yeah, you know.
   */
  void update(E entity) throws SQLException;

  void insert(E entity) throws SQLException;

  /**
   * insert or update the provided entity. If the id is 0 or null, it inserts the record. Otherwise it updates it.
   * @param entity The entity to save
   * @throws SQLException Sql exception
   */
  void insertOrUpdate(E entity) throws SQLException;

  void delete(E entity) throws SQLException;
  
  PK getNextId() throws SQLException;
  
  PK getPrimaryKey(E entity);
  
  int getTotal() throws SQLException;

  void setPrimaryKey(E entity, PK primaryKey);
  
//  <T> Collection<T> getTableInfo() throws SQLException;
}
