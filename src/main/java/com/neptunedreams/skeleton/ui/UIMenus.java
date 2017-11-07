package com.neptunedreams.skeleton.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import javax.swing.FocusManager;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.JTextComponent;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/2/17
 * <p>Time: 11:43 PM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings("HardCodedStringLiteral")
public enum UIMenus implements CaretListener {
  Menu();
//  private Set<JTextComponent> registeredComponents = new HashSet<>();
//  private JTextComponent selectedComponent;
  private final FocusManager focusManager = FocusManager.getCurrentManager();
  @SuppressWarnings("NonFinalFieldInEnum")
  private JTextComponent caretOwner = null;
  @SuppressWarnings({"HardCodedStringLiteral", "MagicCharacter"})
  private final MenuAction cutAction = new ClipboardAction("Cut", 'X', JTextComponent::cut);
  @SuppressWarnings({"HardCodedStringLiteral", "MagicCharacter"})
  private final MenuAction copyAction = new ClipboardAction("Copy", 'C', JTextComponent::copy);
  @SuppressWarnings({"HardCodedStringLiteral", "MagicCharacter"})
  private final MenuAction pasteAction = new ClipboardAction("Paste", 'V', JTextComponent::paste);

  UIMenus() {
    PropertyChangeListener focusListener = evt -> {
//      Component permFocusOwner = focusManager.getPermanentFocusOwner();
      Component permFocusOwner = (Component) evt.getNewValue();
      //noinspection ObjectEquality
      if (permFocusOwner != caretOwner) {
        if (caretOwner != null) {
          removeCaretListener(caretOwner);
          System.out.printf("deFocus c with %s to %s%n", caretOwner.getText(), (permFocusOwner == null) ? "None" : permFocusOwner.getClass().toString());
        }
        if (permFocusOwner instanceof JTextComponent) {
          caretOwner = (JTextComponent) permFocusOwner;
          addCaretListener(caretOwner);
          System.out.printf("Focus c with %s%n", caretOwner.getText());
        }
      }
    };
    focusManager.addPropertyChangeListener("permanentFocusOwner", focusListener);
  }
  
  private final class ClipboardAction extends MenuAction {
    private TextMenuOperation operation;
    private ClipboardAction(final String name, 
//                           @Nullable final Icon icon, 
                           final char acceleratorKey, 
                           TextMenuOperation operation) {
      super(name, null, acceleratorKey);
      this.operation = operation;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
      Component focusOwner = focusManager.getPermanentFocusOwner();
      if (focusOwner instanceof JTextComponent) {
        JTextComponent textComponent = (JTextComponent) focusOwner; // It better be!
        operation.perform(textComponent);
      }
    }

    @SuppressWarnings("UseOfClone")
    @Override
    public ClipboardAction clone() throws CloneNotSupportedException {
      return (ClipboardAction) super.clone();
    }
  }
  
  @FunctionalInterface
  private interface TextMenuOperation {
    void perform(JTextComponent component);
  }

  private void removeCaretListener(JTextComponent owner) {
    owner.removeCaretListener(this);
  }
  
  private void addCaretListener(JTextComponent owner) {
    owner.addCaretListener(this);
  }
  
  public void installMenu(JFrame frame) {
    JMenu editMenu = new JMenu("Edit");
    JMenuItem cutItem = new JMenuItem(cutAction);
    editMenu.add(cutItem);
    editMenu.add(copyAction);
    editMenu.add(pasteAction);

    JMenuBar menuBar = new JMenuBar();
    menuBar.add(editMenu);
    frame.setJMenuBar(menuBar);
  }

  @Override
  public void caretUpdate(final CaretEvent e) {
    boolean selectionPresent = e.getDot() != e.getMark();
    System.out.printf("Selection %b from %d =? %d%n", selectionPresent, e.getDot(), e.getMark());
    cutAction.setEnabled(selectionPresent);
    copyAction.setEnabled(selectionPresent);
  }
}
