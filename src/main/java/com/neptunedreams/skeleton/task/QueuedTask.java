package com.neptunedreams.skeleton.task;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/5/17
 * <p>Time: 12:02 AM
 *
 * @author Miguel Mu\u00f1oz
 * @param <I> Input type
 * @param <R> Result type
 */
public class QueuedTask<I, R> {
  private final ParameterizedCallable<I, R> callable;
  private final long delayMilliSeconds;
  private final Consumer<R> consumer;
  private final BlockingQueue<I> queue = new SynchronousQueue<>();
  private final @NonNull CountDownDoor door = new CountDownDoor(1);


  public QueuedTask(long delay, ParameterizedCallable<I, R> task, Consumer<R> theConsumer) {
    delayMilliSeconds = delay;
    callable = task;
    consumer = theConsumer;
    final Thread waitThread = new Thread(createWaitTask(), "QueuedTask.waitThread");
    waitThread.setDaemon(true);
    waitThread.start();
    final Thread launchThread = new Thread(createLaunchTask(), "QueuedTask.launch Thread");
    launchThread.setDaemon(true);
    launchThread.start();
  }

  /**
   * Search for the text after waiting for {@code interval} milliseconds. Calling this a second time before the wait
   * is up will restart the wait with a new String. This method may be called from any thread, including the 
   * EventDispatchThread. The wait happens on a private Thread.
   * TODO: Remove ParameterizedCallable and use an ordinary Callable. We never look at the parameter.
   * @param text the text to process.
   */
  public void feedData(I text) {
    queue.add(text);
  }
  
  @SuppressWarnings("method.invocation.invalid")
  private Runnable createWaitTask(@UnderInitialization QueuedTask<I, R> this) {
    return () -> {
      //noinspection InfiniteLoopStatement
      while (true) {
        try {
          I input = queue.poll(delayMilliSeconds, TimeUnit.MILLISECONDS);
          if (input != null) {
            launchTask(input); // NullChecker error. It doesn't understand that this runs when fully initialized.
          }
        } catch (InterruptedException ignored) {
          Thread.interrupted(); // clears the interrupt.
          queue.clear();
        }
      }
    };
  }
  
  private void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException ignored) { }
  }

  private Runnable createLaunchTask(@UnderInitialization QueuedTask<I, R>this) {
    return () -> {
      //noinspection InfiniteLoopStatement
      while (true) {
        try {
//            System.out.println("... Awaiting... " + System.currentTimeMillis());
          assert door != null;
          door.await();      // throws InterruptedException
//            System.out.println(".. reOpening... " + System.currentTimeMillis());
          door.reset(1);
//            System.out.println("... Sleeping... " + System.currentTimeMillis());
          QueuedTask.this.sleep(delayMilliSeconds);
//            System.out.println(".. Launching... " + System.currentTimeMillis());
          QueuedTask.this.launchCallable();
        } catch (InterruptedException ignored) {
//            System.out.println("--- Interrupted " + System.currentTimeMillis());
        }

      }
    };
  }

  private void launchCallable() {
    try {
      R result = callable.call(); // throws InterruptedException
      consumer.accept(result);
    } catch (InterruptedException ignored) {
    }
  }

  private void launchTask(@NonNull I input) {
    callable.setInputData(input);
//    launchThread.interrupt();
    door.countDown();
  }
}
