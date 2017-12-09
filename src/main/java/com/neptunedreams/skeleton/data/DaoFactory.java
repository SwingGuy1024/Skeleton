package com.neptunedreams.skeleton.data;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/12/17
 * <p>Time: 11:47 AM
 *
 * @author Miguel Mu\u00f1oz
 */
public interface DaoFactory {
  <T> Dao<T, ?> getDao(Class<T> tableClass);
}
