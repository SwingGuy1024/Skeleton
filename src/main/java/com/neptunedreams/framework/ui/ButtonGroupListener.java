package com.neptunedreams.framework.ui;

import javax.swing.ButtonModel;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 12/29/17
 * <p>Time: 7:33 PM
 *
 * @author Miguel Mu\u00f1oz
 */
@FunctionalInterface
public interface ButtonGroupListener {
  /**
   * Called when the selection for the group has changed.
   * @param selectedButtonModel The button model
   */
  void selectionChanged(ButtonModel selectedButtonModel);
}
