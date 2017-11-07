package com.neptunedreams.skeleton.data;


import org.jetbrains.annotations.NotNull;

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
//  @Column(name="SOURCE")
  private String source="";
//  @Column(name = "USERNAME")
  private String userName="";
//  @Column(name = "PASSWORD")
  private String password="";
//  @Column(name = "NOTES", length = 2048)
  private String notes="";

  public enum FIELD { ID, SOURCE, USERNAME, PASSWORD, NOTES }

  public Record() { }

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

  @NotNull
  public String getSource() {
    return source;
  }

  public void setSource(@NotNull final String source) {
    this.source = source;
  }

  @NotNull
  public String getUserName() {
    return userName;
  }

  public void setUserName(@NotNull final String userName) {
    this.userName = userName;
  }

  @NotNull
  public String getPassword() {
    return password;
  }

  public void setPassword(@NotNull final String password) {
    this.password = password;
  }

  @NotNull
  public String getNotes() {
    return notes;
  }

  public void setNotes(@NotNull final String notes) {
    this.notes = notes;
  }

  @SuppressWarnings("HardCodedStringLiteral")
  @Override
  public String toString() {
    return String.format("{%n  id: %d,%n  source: %s,%n  username: %s,%n  password: %s,%n  notes: %s%n}", id, source, userName, password, notes);
  }
}
