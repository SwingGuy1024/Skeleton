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
  private final @NonNull CountDownDoor door = new CountDownDoor(1);
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
    queue.poll(); // remove the prior data. This prevents a rare exception when feedData gets called twice too quickly.
    queue.add(data);
    launchThread.interrupt();
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
        }
      } catch (InterruptedException ignored) {
        Thread.interrupted(); // clears the interrupt.
        queue.clear();
      }
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
        // We might be able to get rid of this door and just use an AtomicBoolean to determine if the callable should
        // be launched. But for now, I'm happy with how this works.
        assert door != null;
        door.await();      // throws InterruptedException
        door.reset(1);
        Thread.sleep(delayMilliSeconds);
        QueuedTask.this.launchCallable();
      } catch (InterruptedException ignored) {
      }
    }
  }

  /**
   * Skip the queuing and waiting and just launch the task immediately.
   */
  private void launchCallable() {
    try {
      R result = callable.call(); // throws InterruptedException
      consumer.accept(result);
    } catch (InterruptedException ignored) {
    }
  }

  private void launchTask(@NonNull I input) {
    callable.setInputData(input);
    door.countDown();
  }
}
