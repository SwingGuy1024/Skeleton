package com.neptunedreams.skeleton.data;

import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.initialization.qual.UnderInitialization;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/12/17
 * <p>Time: 11:50 AM
 *
 * @author Miguel Mu\u00f1oz
 */
public class AbstractDaoFactory {
  private final Map<Class<?>, Dao<?, ?>> daoMap = new HashMap<>();
  protected final <T> void addDao(@UnderInitialization AbstractDaoFactory this, Class<T> tClass, Dao<T, ?> tDao) {
    daoMap.put(tClass, tDao);
  }
  
  public <T, PK> Dao<T, PK> getDao(Class<T> tClass) {
    //noinspection unchecked
    return (Dao<T, PK>) daoMap.get(tClass);
  } 
}
