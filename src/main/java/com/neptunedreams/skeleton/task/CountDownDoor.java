package com.neptunedreams.skeleton.task;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * This class is a modified version of the CountDownLatch class. It's 
 * essentially identical except that it adds a reset() method that lets me
 * reset it after it has expired. This avoids synchronization problems with
 * instantiating a new latch while another Thread may be about to hit the old
 * latch.
 * <p/>
 * It hasn't been tested thoroughly in the case where reset is called 
 * when it hasn't expired yet, because I'm not yet using it that way.
 * <p/>
 * A synchronization aid that allows one or more threads to wait until
 * a set of operations being performed in other threads completes.
 * <p>
 * <p>A {@code CountDownDoor} is initialized with a given <em>count</em>.
 * The {@link #await await} methods block until the current count reaches
 * zero due to invocations of the {@link #countDown} method, after which
 * all waiting threads are released and any subsequent invocations of
 * {@link #await await} return immediately.  This is a one-shot phenomenon
 * -- the count cannot be reset.  If you need a version that resets the
 * count, consider using a {@link CyclicBarrier}.
 * <p>
 * <p>A {@code CountDownDoor} is a versatile synchronization tool
 * and can be used for a number of purposes.  A
 * {@code CountDownDoor} initialized with a count of one serves as a
 * simple on/off latch, or gate: all threads invoking {@link #await await}
 * wait at the gate until it is opened by a thread invoking {@link
 * #countDown}.  A {@code CountDownDoor} initialized to <em>N</em>
 * can be used to make one thread wait until <em>N</em> threads have
 * completed some action, or some action has been completed N times.
 * <p>
 * <p>A useful property of a {@code CountDownDoor} is that it
 * doesn't require that threads calling {@code countDown} wait for
 * the count to reach zero before proceeding, it simply prevents any
 * thread from proceeding past an {@link #await await} until all
 * threads could pass.
 * <p>
 * <p><b>Sample usage:</b> Here is a pair of classes in which a group
 * of worker threads use two countdown latches:
 * <ul>
 * <li>The first is a start signal that prevents any worker from proceeding
 * until the driver is ready for them to proceed;
 * <li>The second is a completion signal that allows the driver to wait
 * until all workers have completed.
 * </ul>
 * <p>
 * <pre> {@code
 * class Driver { // ...
 *   void main() throws InterruptedException {
 *     CountDownDoor startSignal = new CountDownDoor(1);
 *     CountDownDoor doneSignal = new CountDownDoor(N);
 *
 *     for (int i = 0; i < N; ++i) // create and start threads
 *       new Thread(new Worker(startSignal, doneSignal)).start();
 *
 *     doSomethingElse();            // don't let run yet
 *     startSignal.countDown();      // let all threads proceed
 *     doSomethingElse();
 *     doneSignal.await();           // wait for all to finish
 *   }
 * }
 *
 * class Worker implements Runnable {
 *   private final CountDownDoor startSignal;
 *   private final CountDownDoor doneSignal;
 *   Worker(CountDownDoor startSignal, CountDownDoor doneSignal) {
 *     this.startSignal = startSignal;
 *     this.doneSignal = doneSignal;
 *   }
 *   public void run() {
 *     try {
 *       startSignal.await();
 *       doWork();
 *       doneSignal.countDown();
 *     } catch (InterruptedException ex) {} // return;
 *   }
 *
 *   void doWork() { ... }
 * }}</pre>
 * <p>
 * <p>Another typical usage would be to divide a problem into N parts,
 * describe each part with a Runnable that executes that portion and
 * counts down on the latch, and queue all the Runnables to an
 * Executor.  When all sub-parts are complete, the coordinating thread
 * will be able to pass through await. (When threads must repeatedly
 * count down in this way, instead use a {@link CyclicBarrier}.)
 * <p>
 * <pre> {@code
 * class Driver2 { // ...
 *   void main() throws InterruptedException {
 *     CountDownDoor doneSignal = new CountDownDoor(N);
 *     Executor e = ...
 *
 *     for (int i = 0; i < N; ++i) // create and start threads
 *       e.execute(new WorkerRunnable(doneSignal, i));
 *
 *     doneSignal.await();           // wait for all to finish
 *   }
 * }
 *
 * class WorkerRunnable implements Runnable {
 *   private final CountDownDoor doneSignal;
 *   private final int i;
 *   WorkerRunnable(CountDownDoor doneSignal, int i) {
 *     this.doneSignal = doneSignal;
 *     this.i = i;
 *   }
 *   public void run() {
 *     try {
 *       doWork(i);
 *       doneSignal.countDown();
 *     } catch (InterruptedException ex) {} // return;
 *   }
 *
 *   void doWork() { ... }
 * }}</pre>
 * <p>
 * <p>Memory consistency effects: Until the count reaches
 * zero, actions in a thread prior to calling
 * {@code countDown()}
 * <a href="package-summary.html#MemoryVisibility"><i>happen-before</i></a>
 * actions following a successful return from a corresponding
 * {@code await()} in another thread.
 *
 * @author Doug Lea
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class CountDownDoor {
  /**
   * Synchronization control For CountDownDoor.
   * Uses AQS state to represent count.
   */
  private static final class Sync extends AbstractQueuedSynchronizer {
    private static final long serialVersionUID = 4982264981922014374L;

    Sync(int count) {
      super();
      setState(count);
    }

    int getCount() {
      return getState();
    }

    protected int tryAcquireShared(int acquires) {
      return (getState() == 0) ? 1 : -1;
    }

    protected boolean tryReleaseShared(int releases) {
      // Decrement count; signal when transition to zero
      while (true) {
        int c = getState();
        if (c == 0) {
          return false;
        }
        int nextC = c - 1;
        if (compareAndSetState(c, nextC)) {
          return nextC == 0;
        }
      }
    }
    
    void reOpen(int count) {
      setState(count);
    }
  }

  private final Sync sync;

  /**
   * Constructs a {@code CountDownDoor} initialized with the given count.
   *
   * @param count the number of times {@link #countDown} must be invoked
   *              before threads can pass through {@link #await}
   * @throws IllegalArgumentException if {@code count} is negative
   */
  public CountDownDoor(int count) {
    if (count < 0) {
      throw new IllegalArgumentException("count < 0");
    }
    this.sync = new Sync(count);
  }

  /**
   * Causes the current thread to wait until the latch has counted down to
   * zero, unless the thread is {@linkplain Thread#interrupt interrupted}.
   * <p>
   * <p>If the current count is zero then this method returns immediately.
   * <p>
   * <p>If the current count is greater than zero then the current
   * thread becomes disabled for thread scheduling purposes and lies
   * dormant until one of two things happen:
   * <ul>
   * <li>The count reaches zero due to invocations of the
   * {@link #countDown} method; or
   * <li>Some other thread {@linkplain Thread#interrupt interrupts}
   * the current thread.
   * </ul>
   * <p>
   * <p>If the current thread:
   * <ul>
   * <li>has its interrupted status set on entry to this method; or
   * <li>is {@linkplain Thread#interrupt interrupted} while waiting,
   * </ul>
   * then {@link InterruptedException} is thrown and the current thread's
   * interrupted status is cleared.
   *
   * @throws InterruptedException if the current thread is interrupted
   *                              while waiting
   */
  public void await() throws InterruptedException {
    sync.acquireSharedInterruptibly(1);
  }

  /**
   * Causes the current thread to wait until the latch has counted down to
   * zero, unless the thread is {@linkplain Thread#interrupt interrupted},
   * or the specified waiting time elapses.
   * <p>
   * <p>If the current count is zero then this method returns immediately
   * with the value {@code true}.
   * <p>
   * <p>If the current count is greater than zero then the current
   * thread becomes disabled for thread scheduling purposes and lies
   * dormant until one of three things happen:
   * <ul>
   * <li>The count reaches zero due to invocations of the
   * {@link #countDown} method; or
   * <li>Some other thread {@linkplain Thread#interrupt interrupts}
   * the current thread; or
   * <li>The specified waiting time elapses.
   * </ul>
   * <p>
   * <p>If the count reaches zero then the method returns with the
   * value {@code true}.
   * <p>
   * <p>If the current thread:
   * <ul>
   * <li>has its interrupted status set on entry to this method; or
   * <li>is {@linkplain Thread#interrupt interrupted} while waiting,
   * </ul>
   * then {@link InterruptedException} is thrown and the current thread's
   * interrupted status is cleared.
   * <p>
   * <p>If the specified waiting time elapses then the value {@code false}
   * is returned.  If the time is less than or equal to zero, the method
   * will not wait at all.
   *
   * @param timeout the maximum time to wait
   * @param unit    the time unit of the {@code timeout} argument
   * @return {@code true} if the count reached zero and {@code false}
   * if the waiting time elapsed before the count reached zero
   * @throws InterruptedException if the current thread is interrupted
   *                              while waiting
   */
  public boolean await(long timeout, TimeUnit unit)
      throws InterruptedException {
    return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
  }

  /**
   * Decrements the count of the latch, releasing all waiting threads if
   * the count reaches zero.
   * <p>
   * <p>If the current count is greater than zero then it is decremented.
   * If the new count is zero then all waiting threads are re-enabled for
   * thread scheduling purposes.
   * <p>
   * <p>If the current count equals zero then nothing happens.
   */
  public void countDown() {
    sync.releaseShared(1);
  }

  /**
   * Returns the current count.
   * <p>
   * <p>This method is typically used for debugging and testing purposes.
   *
   * @return the current count
   */
  public long getCount() {
    return sync.getCount();
  }

  /**
   * Returns a string identifying this latch, as well as its state.
   * The state, in brackets, includes the String {@code "Count ="}
   * followed by the current count.
   *
   * @return a string identifying this latch, as well as its state
   */
  public String toString() {
    //noinspection StringConcatenation,HardCodedStringLiteral,MagicCharacter
    return super.toString() + "[Count = " + sync.getCount() + ']';
  }
  
  public void reset(int count) {
    sync.reOpen(count);
  }
}
