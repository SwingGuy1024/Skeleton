package com.neptunedreams.skeleton.task;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/5/17
 * <p>Time: 12:50 AM
 *
 * @author Miguel Mu\u00f1oz
 * @param <I> InputType
 * @param <R> ResultType
 */
public abstract class ParameterizedCallable<I, R> {
  private @Nullable I inputData;
  public ParameterizedCallable(@Nullable I initialValue) {
    inputData = initialValue;
  }

  @SuppressWarnings("WeakerAccess")
  public void setInputData(@Nullable I input) {
    inputData = input;
  }

  @SuppressWarnings("WeakerAccess")
  protected @Nullable I getInputData() { return inputData; }

  public abstract R call(I input);
}
