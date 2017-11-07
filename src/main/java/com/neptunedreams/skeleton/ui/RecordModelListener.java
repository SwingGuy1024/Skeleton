package com.neptunedreams.skeleton.ui;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/29/17
 * <p>Time: 3:14 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public interface RecordModelListener {
  void modelListChanged(int newSize);
  void indexChanged(int index, int prior);
}
