package com.neptunedreams.skeleton.data;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/21/17
 * <p>Time: 4:42 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public interface DBRecord {
  int getId();
  void setId(final int id);

  String getSource();
  void setSource(final String source);

  String getUserName();
  void setUserName(final String userName);

  String getPassword();
  void setPassword(final String password);

  String getNotes();
  void setNotes(final String notes);
}
