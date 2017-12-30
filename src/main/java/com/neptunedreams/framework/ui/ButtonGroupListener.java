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
  void selectionChanged(ButtonModel selectedButtonModel);
}
