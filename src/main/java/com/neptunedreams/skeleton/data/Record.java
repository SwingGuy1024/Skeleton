package com.neptunedreams.skeleton.data;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/26/17
 * <p>Time: 12:21 AM
 *
 * @author Miguel Mu\u00f1oz
 */
//@Entity
public class Record {
//  @Column(name="ID")
  private int id = 0;
  private @NonNull String source="";
  private @NonNull String userName="";
  private @NonNull String password="";
  private @NonNull String notes="";

  @SuppressWarnings("JavaDoc")
  public Record() { }

  @SuppressWarnings("JavaDoc")
  public Record(@NonNull String s, @NonNull String un, @NonNull String pw, @NonNull String nts) {
    source = s;
    userName = un;
    password = pw;
    notes = nts;
  }

  public int getId() {
    return id;
  }

  public void setId(final int id) {
    this.id = id;
  }

  public @NonNull String getSource() {
    return source;
  }

  public void setSource(final @NonNull String source) {
    this.source = source;
  }

  public @NonNull String getUserName() {
    return userName;
  }

  public void setUserName(final @NonNull String userName) {
    this.userName = userName;
  }

  public @NonNull String getPassword() {
    return password;
  }

  public void setPassword(final @NonNull String password) {
    this.password = password;
  }

  public @NonNull String getNotes() {
    return notes;
  }

  public void setNotes(final @NonNull String notes) {
    this.notes = notes;
  }

  @SuppressWarnings("HardCodedStringLiteral")
  @Override
  public @NonNull String toString() {
    //noinspection ConstantConditions
    return String.format("{%n  id: %d,%n  source: %s,%n  username: %s,%n  password: %s,%n  notes: %s%n}", id, source, userName, password, notes);
  }
}
