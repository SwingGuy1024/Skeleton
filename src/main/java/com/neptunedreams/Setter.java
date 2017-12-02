package com.neptunedreams;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/24/17
 * <p>Time: 2:14 PM
 *
 * @author Miguel Mu\u00f1oz
 */
@FunctionalInterface
public interface Setter<I, T> {
  void setValue(I instance, T value);
}
