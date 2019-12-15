package com;

import javax.swing.JOptionPane;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/29/17
 * <p>Time: 12:24 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public enum ErrorReport {
  ;

  @SuppressWarnings("argument.type.incompatible") // null parentComponent is actually allowed.
  public static void reportException(String operation, Throwable t) {
    //noinspection HardCodedStringLiteral
    String message = String.format("Error during %s:%n%s", operation, t.getMessage());
    t.printStackTrace();
    //noinspection HardCodedStringLiteral
    JOptionPane.showMessageDialog( null, message, "Error", JOptionPane.ERROR_MESSAGE);
  }
}
