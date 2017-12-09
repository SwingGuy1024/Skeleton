package com.neptunedreams.skeleton.data;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/22/17
 * <p>Time: 3:21 PM
 *
 * @param <I> The input type
 * @param <O> The output type
 * @author Miguel Mu\u00f1oz
 */
@FunctionalInterface
public interface Translator<I, O> {
  /**
   * Transform an object of one type into an object of another.
   * @param inputValue The input
   * @return The transformed output instance
   */
  O transform(I inputValue);
}
