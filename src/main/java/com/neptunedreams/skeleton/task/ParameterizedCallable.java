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
  private @Nullable I inputData = null;
  public ParameterizedCallable() {
  }

  @SuppressWarnings("WeakerAccess")
  public void setInputData(@NonNull I input) {
    inputData = input;
  }
  protected @Nullable I getInputData() { return inputData; }

  @Override
  public abstract R call() throws InterruptedException;
}
