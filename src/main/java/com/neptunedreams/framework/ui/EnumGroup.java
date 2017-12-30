package com.neptunedreams.framework.ui;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;

/**
 * A specialized kind of ButtonGroup that maps radio buttons to enum values. The getSelected() method
 * will return the enum value for the selected JRadioButton.
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 12/28/17
 * <p>Time: 6:53 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public class EnumGroup<E extends Enum<E> & DisplayEnum> {
  private final ButtonGroup group = new ButtonGroup();
  private final Map<String, E> textMap = new HashMap<>();
  private final Map<E, ButtonModel> buttonMap = new HashMap<>();
  private final List<ButtonGroupListener> listenerList = new LinkedList<>();

  @SuppressWarnings("methodref.inference.unimplemented")
  public void add(JRadioButton button, E enumValue) {
    group.add(button);
    final String display = enumValue.getDisplay();
    textMap.put(display, enumValue);
    final ButtonModel model = button.getModel();
    model.setActionCommand(display);
    buttonMap.put(enumValue, model);
    model.addChangeListener(this::fireButtonChange); // Warning here that null checking is unimplemented for method references
  }
  
  private void fireButtonChange(ChangeEvent evt) {
    ButtonModel selectedModel = (ButtonModel) evt.getSource();
    for (ButtonGroupListener listener : listenerList) {
      listener.selectionChanged(selectedModel);
    }
  }
  
  public JRadioButton add(E enumValue) {
    JRadioButton button = new JRadioButton(enumValue.getDisplay());
    add(button, enumValue);
    return button;
  }
  
  public E getSelected() {
    final ButtonModel selection = group.getSelection();
    //noinspection UseOfSystemOutOrSystemErr
    System.err.printf("Selection command: %s%n", selection.getActionCommand()); // NON-NLS
    final String actionCommand = selection.getActionCommand();
    //noinspection HardCodedStringLiteral,UseOfSystemOutOrSystemErr
    System.err.printf("ActionCommand: %s from %s into map %s%n", actionCommand, selection, textMap);
    return Objects.requireNonNull(textMap.get(actionCommand));
  }
  
  public void setSelected(E selectedValue) {
    group.setSelected(Objects.requireNonNull(buttonMap.get(selectedValue)), true);
  }
  
  public void addButtonGroupListener(ButtonGroupListener listener) {
    listenerList.add(listener);
  }
  
  @SuppressWarnings("unused")
  public void removeButtonGroupListener(ButtonGroupListener listener) {
    listenerList.remove(listener);
  }
}
