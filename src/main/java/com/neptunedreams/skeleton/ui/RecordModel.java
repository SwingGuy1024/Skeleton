package com.neptunedreams.skeleton.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import com.neptunedreams.skeleton.data.Record;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/29/17
 * <p>Time: 3:27 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public class RecordModel {
  final private List<RecordModelListener> listenerList = new LinkedList<>();
  
//  RecordModel() {
//    Thread.dumpStack();
//  }
//
  private List<Record> foundItems;
  private int recordIndex = 0;
  private int total = 0;

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
  
  public void removeModelListener(RecordModelListener listener) {
    listenerList.remove(listener);
  }
 
  public void setNewList(Collection<Record> records) {
    foundItems = new ArrayList<>(records);
    recordIndex = 0;
    if (foundItems.isEmpty()) {
      foundItems.add(new Record());
    }
    fireModelListChanged();
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

  public void append(Record insertedRecord) {
    foundItems.add(recordIndex, insertedRecord);
//    recordIndex++; // Should we call setRecordIndex() here?
    fireModelListChanged();
  }

  public Record getSelectedRecord() {
    if (!foundItems.isEmpty()) {
      return foundItems.get(recordIndex);
    }
    Record emptyRecord = new Record();
    foundItems.add(emptyRecord);
    fireModelListChanged(); // Is it dangerous to fire the listener before returning the record?
    return emptyRecord;
  }
  
  public Record getRecordAt(int index) {
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
        foundItems.add(new Record());
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
