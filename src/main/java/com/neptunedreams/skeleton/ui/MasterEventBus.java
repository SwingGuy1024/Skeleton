package com.neptunedreams.skeleton.ui;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 1/2/18
 * <p>Time: 12:38 AM
 *
 * @author Miguel Mu\u00f1oz
 */
public final class MasterEventBus {
  private MasterEventBus() {
    master.register(this);
  }
  @SuppressWarnings("HardCodedStringLiteral")
  private static EventBus master = new EventBus("master");
  public static EventBus instance() { return master; }
  static final LoadUIEvent uiEvent = new LoadUIEvent();
  static final class LoadUIEvent {
    private LoadUIEvent(/*R record*/) {
    }
  }
  
  static void postLoadUserData() {
    master.post(uiEvent);
  }
  
  static final class UserRequestedNewRecordEvent { }
  
  static final UserRequestedNewRecordEvent userRequestedNewRecordEvent = new UserRequestedNewRecordEvent();
  
  static void postUserRequestedNewRecordEvent() {
    master.post(userRequestedNewRecordEvent);
  }

  static final class SearchNowEvent {
    private SearchNowEvent() { }
  }
  private static final SearchNowEvent searchNowEvent = new SearchNowEvent();
  
  static void postSearchNowEvent() {
    master.post(searchNowEvent);
  }
  
  @Subscribe
  public void showDeadEvent(DeadEvent deadEvent) {
    //noinspection UseOfSystemOutOrSystemErr,HardCodedStringLiteral
    System.err.printf("Dead Event: %s of class %s%n", deadEvent, deadEvent.getClass());
  }

  /**
   * We wrap the CurrentRecord in this event class rather than passing it directly because it's a generic type, 
   * so the EventBus registers it as an Object, and it gets called for any event type.
   * @param <R> The type of the record.
   */
  static class ChangeRecord<R> {
    private final R newRecord;
    ChangeRecord(R record) {
      newRecord = record;
    }
    
    @SuppressWarnings("WeakerAccess")
    public R getNewRecord() { return newRecord; }
  }
}
