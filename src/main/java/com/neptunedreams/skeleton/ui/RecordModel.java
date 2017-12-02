package com.neptunedreams.skeleton.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
//import com.neptunedreams.skeleton.data.Record;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/29/17
 * <p>Time: 3:27 PM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings("WeakerAccess")
public class RecordModel<R> {
  private final List<RecordModelListener> listenerList = new LinkedList<>();
  
//  RecordModel() {
//    Thread.dumpStack();
//  }
//
  private List<R> foundItems;
  private int recordIndex = 0;
  private int total = 0;
  private Class<R> recordClass;
  
  RecordModel(Class<R> theRecordClass) {
    recordClass = theRecordClass;
  }

  public int getRecordIndex() {
    return recordIndex;
  }
  
  public int getSize() { return foundItems.size(); }

  public int getTotal() {
    return total;
  }

  public void setTotalFromSize() {
    this.total = foundItems.size();
  }
  
  public void incrementTotal() { total++; }
  
  public void decrementTotal() { total--; }

  public void addModelListener(RecordModelListener listener) {
    listenerList.add(listener);
  }
  
  @SuppressWarnings("unused")
  public void removeModelListener(RecordModelListener listener) {
    listenerList.remove(listener);
  }
 
  public void setNewList(Collection<R> records) {
    foundItems = new ArrayList<>(records);
    recordIndex = 0;
    if (foundItems.isEmpty()) {
    final R record;
      record = createNewEmptyRecord();
      foundItems.add(record);
    }
    fireModelListChanged();
  }

  public R createNewEmptyRecord() {
    final R record;
    try {
      record = recordClass.getConstructor().newInstance();
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new IllegalArgumentException("Record type needs no-argument constructor", e);
    }
    return record;
  }

  public void goNext() {
    assert !foundItems.isEmpty();
    int size = foundItems.size();
    int nextRecord = recordIndex + 1;
    if (nextRecord >= size) {
      nextRecord = 0;
    }
    setRecordIndex(nextRecord, recordIndex);
  }

  public void goPrev() {
    assert !foundItems.isEmpty();
    int nextRecord = recordIndex - 1;
    if (nextRecord < 0) {
      nextRecord = foundItems.size() - 1;
    }
    setRecordIndex(nextRecord, recordIndex);
  }
  
  public void goFirst() {
    assert !foundItems.isEmpty();
    setRecordIndex(0, recordIndex);
  }

  public void goLast() {
    assert !foundItems.isEmpty();
    setRecordIndex(foundItems.size()-1, recordIndex);
  }

  private void setRecordIndex(final int i, int prior) {
    recordIndex = i;
    fireIndexChanged(i, prior);
  }

  private void fireIndexChanged(final int i, int prior) {
    for (RecordModelListener modelListener: listenerList) {
      modelListener.indexChanged(i, prior);
    }
  }

  public void append(R insertedRecord) {
    foundItems.add(recordIndex, insertedRecord);
//    recordIndex++; // Should we call setRecordIndex() here?
    fireModelListChanged();
  }

  public R getSelectedRecord() {
    if (!foundItems.isEmpty()) {
      return foundItems.get(recordIndex);
    }
    R emptyRecord = createNewEmptyRecord();
    foundItems.add(emptyRecord);
    fireModelListChanged(); // Is it dangerous to fire the listener before returning the record?
    return emptyRecord;
  }
  
  public R getRecordAt(int index) {
    return foundItems.get(index);
  }

  /**
   * delete the selected item, conditionally.
   * @param notify Fire appropriate listeners after deleting
   * @param index The index of the record to delete. This method does nothing if index is < 0,
   */
  public void deleteSelected(boolean notify, int index) {
    if (index >= 0) {
      foundItems.remove(index);
      if (foundItems.isEmpty()) {
        foundItems.add(createNewEmptyRecord());
      }
      if (recordIndex >= foundItems.size()) {
        recordIndex--; // Should we call setRecordIndex() here?
        assert recordIndex >= 0;
  //      if (recordIndex < 0) {
  //        recordIndex = 0;
  //      }
        if (notify) {
          fireIndexChanged(recordIndex, index);
        }
      }
      if (notify) {
        fireModelListChanged();
      }
    }
  }
  
  private void fireModelListChanged() {
    int size = foundItems.size();
    for (RecordModelListener listener : listenerList) {
      listener.modelListChanged(size);
    }
  }
}
