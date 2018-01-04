package com.neptunedreams.framework.ui;

import java.awt.event.ItemEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;

// TODO:  Remove the DisplayEnum requirement by checking if the constant implements it, and using toString() otherwise.

/**
 * A specialized kind of ButtonGroup that maps radio buttons to enum or constant values. The getSelected() method
 * will return the enum value for the selected JRadioButton.
 * <p/>
 * This class will also help reduce the boilerplate code in setting up your radio buttons.
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 12/28/17
 * <p>Time: 6:53 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public final class EnumGroup<E extends DisplayEnum> {
  // Note: The bounds of the type of this class used to be this: <E extends Enum<E> & DisplayEnum>
  private final ButtonGroup group = new ButtonGroup();
  private final Map<String, E> textMap = new HashMap<>();
  private final Map<E, ButtonModel> buttonMap = new HashMap<>();
  private final List<ButtonGroupListener> listenerList = new LinkedList<>();
  private final AtomicReference<ButtonModel> selectedModelRef = new AtomicReference<>(); 

  /**
   * Add a JRadioButton to the button group, linking it to the specified constant value
   * @param button The JRadioButton
   * @param enumValue The value to link with the button
   */
  @SuppressWarnings("methodref.inference.unimplemented") // Null checking is unimplemented for method references (this::fireButtonChange)
  public void add(JRadioButton button, E enumValue) {
    group.add(button);
    final String display = enumValue.getDisplay();
    textMap.put(display, enumValue);
    final ButtonModel model = button.getModel();
    model.setActionCommand(display);
    buttonMap.put(enumValue, model);
    model.addItemListener(this::fireButtonChange);
  }
  
  /**
   * Add a JRadioButton to the button group, linking it to the specified constant value, and adding it to the panel
   * @param button The JRadioButton
   * @param enumValue The constant value
   * @param panel The panel to add the JRadioButton to.
   * @see #add(JRadioButton, E) 
   */
  public void add(JRadioButton button, E enumValue, JComponent panel) {
    add(button, enumValue);
    panel.add(button);
  }

  private void fireButtonChange(ItemEvent evt) {
    if (evt.getStateChange() == ItemEvent.SELECTED) {
      ButtonModel selectedModel = (ButtonModel) evt.getSource();
      selectedModelRef.set(selectedModel);
      for (ButtonGroupListener listener : listenerList) {
        listener.selectionChanged(selectedModel);
      }
    }
  }
  
  /**
   * Uses the constant value to create a JRadioButton; links it to the constant value; adds that button to the group, 
   * taking its name from the enum's display value. This is the same as calling add(JRadioButton, E), except it 
   * creates the JRadioButton.
   * @param enumValue The constant value
   * @return the JRadioButton created by this method.
   * @see #add(JRadioButton, E) 
   */
  public JRadioButton add(E enumValue) {
    JRadioButton button = new JRadioButton(enumValue.getDisplay());
    add(button, enumValue);
    return button;
  }

  /**
   * Uses the constant value to create a JRadioButton; adds that button to the group, taking its name from the
   * enum's display value; Then adds the button to the provided panel. This is the same as calling 
   * add(JRadioButton, E, JComponent), except it creates the JRadioButton.
   *
   * @param enumValue The constant value
   * @return the JRadioButton created by this method.
   * @see #add(JRadioButton, E, JComponent) 
   */
  public JRadioButton add(E enumValue, JComponent panel) {
    JRadioButton button = add(enumValue);
    panel.add(button);
    return button;
  }

  /**
   * Thread safe method to get the selection.
   * @return The value assigned to the selected button
   */
  public E getSelected() {
    ButtonModel selection = selectedModelRef.get();
    //noinspection UseOfSystemOutOrSystemErr
    final String actionCommand = selection.getActionCommand();
    //noinspection HardCodedStringLiteral,UseOfSystemOutOrSystemErr
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
