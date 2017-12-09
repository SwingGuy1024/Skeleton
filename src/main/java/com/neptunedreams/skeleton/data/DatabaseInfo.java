package com.neptunedreams.skeleton.data;

import java.io.IOException;
import java.sql.SQLException;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/10/17
 * <p>Time: 11:22 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public interface DatabaseInfo {
  String getUrl();
  <T, PK> Dao<T, PK> getDao(Class<T> entityClass, ConnectionSource source);

  default void init() throws IOException, SQLException { }
  
  String getHomeDir();
  Class<?> getRecordClass();
  
  boolean isCreateSchemaAllowed();
  void createSchema();

  ConnectionSource getConnectionSource();
  void shutdown();

}
