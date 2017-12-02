package com.neptunedreams.skeleton.data;

import java.sql.Connection;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/29/17
 * <p>Time: 1:08 AM
 *
 * @author Miguel Mu\u00f1oz
 */
@FunctionalInterface
public interface ConnectionSource {
  Connection getConnection();
}
