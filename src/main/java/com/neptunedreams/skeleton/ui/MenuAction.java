package com.neptunedreams.skeleton.ui;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/2/17
 * <p>Time: 11:45 PM
 *
 * @author Miguel Mu\u00f1oz
 */
abstract class MenuAction extends AbstractAction {
  MenuAction(@NotNull String name, @Nullable Icon icon, Character acceleratorKey) {
    super(name, icon);
    KeyStroke keyStroke = KeyStroke.getKeyStroke(acceleratorKey, java.awt.event.InputEvent.META_DOWN_MASK);
    putValue(Action.ACCELERATOR_KEY, keyStroke);
  }

  @SuppressWarnings("UseOfClone")
  @Override
  public MenuAction clone() throws CloneNotSupportedException {
    return (MenuAction) super.clone();
  }
}
