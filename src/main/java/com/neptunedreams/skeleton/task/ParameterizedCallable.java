package com.neptunedreams.skeleton.task;

import java.util.concurrent.Callable;

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
  private I inputData = null;
  public ParameterizedCallable() {
  }

  public void setInputData(I input) {
    inputData = input;
  }
  protected I getInputData() { return inputData; }

  @Override
  public abstract R call() throws InterruptedException;
}
