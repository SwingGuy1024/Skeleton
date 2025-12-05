package com.neptunedreams.skeleton.data;

import org.jetbrains.annotations.NotNull;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/26/17
 * <p>Time: 12:21 AM
 *
 * @author Miguel Muñoz
 */
//@Entity
public class Record {
//  @Column(name="ID")
  private int id = 0;
  private @NotNull String source="";
  private @NotNull String userName="";
  private @NotNull String password="";
  private @NotNull String notes="";

  @SuppressWarnings("JavaDoc")
  public Record() { }

  @SuppressWarnings("JavaDoc")
  public Record(@NotNull String s, @NotNull String un, @NotNull String pw, @NotNull String nts) {
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

  public @NotNull String getSource() {
    return source;
  }

  public void setSource(final @NotNull String source) {
    this.source = source;
  }

  public @NotNull String getUserName() {
    return userName;
  }

  public void setUserName(final @NotNull String userName) {
    this.userName = userName;
  }

  public @NotNull String getPassword() {
    return password;
  }

  public void setPassword(final @NotNull String password) {
    this.password = password;
  }

  public @NotNull String getNotes() {
    return notes;
  }

  public void setNotes(final @NotNull String notes) {
    this.notes = notes;
  }

  @SuppressWarnings("HardCodedStringLiteral")
  @Override
  public @NotNull String toString() {
    //noinspection ConstantConditions
    return String.format("{%n  id: %d,%n  source: %s,%n  username: %s,%n  password: %s,%n  notes: %s%n}", id, source, userName, password, notes);
  }
}
