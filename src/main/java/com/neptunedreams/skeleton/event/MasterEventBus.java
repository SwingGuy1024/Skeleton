package com.neptunedreams.skeleton.event;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * This class serves as a facade for the event bus. The event bus instance is private, and all methods to post an event
 * are static methods in this class. This makes it easier to keep track of the events.
 * <p>
 * Under no circumstances should the EventBus instance be made available to other classes. By requiring all posts
 * to be done by static methods, we make it possible to use multiple EventBusses in a project, each with its own 
 * set of post methods. This design guarantee that a message can't get posted to the wrong EventBus.
 * The only mistake that can get made is registering a class with the wrong event bus. Consequently, 
 * each facade class should also have a unique name for its register method.
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 1/2/18
 * <p>Time: 12:38 AM
 *
 * @author Miguel Mu\u00f1oz
 */
public final class MasterEventBus {
  private MasterEventBus() {
    master.register(this); // Register Dead Events.
  }

  @SuppressWarnings("HardCodedStringLiteral")
  private static EventBus master = new EventBus("master");
  
  public static void registerMasterEventHandler(Object eventHandlerInstance) {
    master.register(eventHandlerInstance);
  }

  // Data-Free Events:
  private static final LoadUIEvent uiEvent = new LoadUIEvent();
  private static final SearchNowEvent searchNowEvent = new SearchNowEvent();
  private static final UserRequestedNewRecordEvent userRequestedNewRecordEvent = new UserRequestedNewRecordEvent();

  // Simple public Event Classes (Classes that have no data)

  public static final class LoadUIEvent { }
  public static final class UserRequestedNewRecordEvent { }
  public static final class SearchNowEvent { }
  
  // Public post methods

  public static void postLoadUserData() {
    master.post(uiEvent);
  }

  public static void postUserRequestedNewRecordEvent() {
    master.post(userRequestedNewRecordEvent);
  }

  public static void postSearchNowEvent() {
    master.post(searchNowEvent);
  }

  public static <R> void postChangeRecordEvent(R record) {
    master.post(new ChangeRecord<>(record));
  }

  @Subscribe
  public void showDeadEvent(DeadEvent deadEvent) {
    //noinspection UseOfSystemOutOrSystemErr,HardCodedStringLiteral
    System.err.printf("Dead Event: %s of class %s%n", deadEvent, deadEvent.getClass());
  }
}
