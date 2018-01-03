package com.neptunedreams.skeleton.task;

import java.util.concurrent.Callable;
import org.checkerframework.checker.nullness.qual.NonNull;
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
public abstract class ParameterizedCallable<I, R> implements Callable<R> {
  private I inputData;
  public ParameterizedCallable(I initialValue) {
    inputData = initialValue;
  }

  @SuppressWarnings("WeakerAccess")
  public void setInputData(I input) {
    inputData = input;
  }

  protected I getInputData() { return inputData; }

  @Override
  public abstract R call() throws InterruptedException;
}
