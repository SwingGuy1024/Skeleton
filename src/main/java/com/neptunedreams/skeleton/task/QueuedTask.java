package com.neptunedreams.skeleton.task;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.function.Consumer;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * This class lets the application search as the user types, but delays the launch of the search until after the user 
 * has stopped typing for a long enough time. It doesn't handle text or searching directly, so it may feasibly be
 * adapted to other tasks and data types. Here's how it works. <p/>
 * The class gets instantiated with a ParameterizedCallable task and a Consumer. For every change in the text of a 
 * text field, the calls the {@code feedData()} method, which puts the input (the text) onto the queue and the clock 
 * starts. When any subsequent change gets put on the queue, the previous text gets thrown out, and the clock gets 
 * restarted. When the user stops typing, the the clock runs out. At this point, the QueuedTask sets the into the 
 * ParameterizedCallable task, and calls its {@code call()} method. Then it sends the results to the Consumer.
 * It is the responsibility of the caller to define a ParameterizedCallable task that launches the search function, and
 * a Consumer that sends the search results to the proper user interface component.
 * <p/>This class is divided into two Threads because it calls two methods that are interruptable. If I did it in a
 * single task, it would be impossible to know which method I'm interrupting. The two methods are Thread.sleep() and
 * Queue.take() or Queue.poll().
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
  private final Thread launchThread;


  public QueuedTask(long delay, ParameterizedCallable<I, R> task, Consumer<R> theConsumer) {
    delayMilliSeconds = delay;
    callable = task;
    consumer = theConsumer;
    final Thread waitThread = new Thread(createWaitTask(), "QueuedTask.waitThread");
    waitThread.setDaemon(true);
    waitThread.start();
    launchThread = new Thread(createLaunchTask(), "QueuedTask.launch Thread");
    launchThread.setDaemon(true);
    launchThread.start();
  }
  
  /**
   * Feed the data into the wait queue. The data will be processed after waiting for {@code interval} milliseconds. 
   * Calling this a second time before the wait is up will restart the wait with the new data. This method may be 
   * called from any thread, including the EventDispatchThread. The wait happens on a private Thread.
   * @param data the data to process.
   */
  public void feedData(I data) {
    try {
      queue.put(data); // SynchronousQueue.add() should never get called. Unnecessary and causes big problems.
    } catch (InterruptedException ignored) { }
  }
  
  private Runnable createWaitTask(@UnderInitialization QueuedTask<I, R> this) {
    return this::waitLoop;
  }

  private void waitLoop() {
    //noinspection InfiniteLoopStatement
    while (true) {
      try {
        I input = queue.take();
        if (input != null) {
          launchTask(input);
          launchThread.interrupt();
        }
      } catch (InterruptedException ignored) { }
    }
  }

  private Runnable createLaunchTask(@UnderInitialization QueuedTask<I, R>this) {
    return this::launchTaskLoop; // Doesn't null check this, but it's only used after initialization, so we're okay.
  }

  private void launchTaskLoop() {
    //noinspection InfiniteLoopStatement
    while (true) {
      // This try block gets interrupted whenever feedData() is called.
      try {
        Thread.sleep(delayMilliSeconds);
        QueuedTask.this.launchCallable();
      } catch (InterruptedException ignored) { }
    }
  }

  /**
   * Skip the queuing and waiting and just launch the task immediately.
   */
  private void launchCallable() {
    final I inputData = callable.getInputData();
    if (inputData != null) {
      try {
        callable.setInputData(null);
        R result = callable.call(inputData); // throws InterruptedException
        consumer.accept(result);
//        callable.setInputData(null);
      } catch (InterruptedException ignored) { }
    }
  }

  private void launchTask(@NonNull I input) {
    callable.setInputData(input);
  }
}
