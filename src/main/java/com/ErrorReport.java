package com;

import javax.swing.JOptionPane;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/29/17
 * <p>Time: 12:24 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public class ErrorReport {
  private ErrorReport() { }
  
  public static void reportException(String operation, Throwable t) {
    String message = String.format("Error during %s:%n%s", operation, t.getMessage());
    t.printStackTrace();
    JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
  }
}
