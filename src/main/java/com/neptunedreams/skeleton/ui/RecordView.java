package com.neptunedreams.skeleton.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import com.neptunedreams.Setter;
import com.neptunedreams.framework.ui.FieldBinding;
import com.neptunedreams.skeleton.data.RecordField;
import org.checkerframework.checker.initialization.qual.UnderInitialization;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/29/17
 * <p>Time: 10:54 AM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings("WeakerAccess")
public final class RecordView<R> extends JPanel {
  private static final int TEXT_COLUMNS = 40;
  private static final int TEXT_ROWS = 6;
  private static final int NO_RECORD = -999;
  private JPanel labelPanel = new JPanel(new GridLayout(0, 1));
  private JPanel fieldPanel = new JPanel(new GridLayout(0, 1));
  private JPanel checkBoxPanel = new JPanel(new GridLayout(0, 1));
  private ButtonGroup buttonGroup = new ButtonGroup();

  @SuppressWarnings("HardCodedStringLiteral")
  private R currentRecord; // = new Record("D", "D", "D", "D");
  
  private RecordController<R, ?> controller;
  private final FieldBinding.IntegerBinding<R> idBinding;
  private final List<? extends FieldBinding<R, ? extends Serializable, ? extends JComponent>> allBindings;

  @SuppressWarnings({"initialization.fields.uninitialized", "argument.type.incompatible", "method.invocation.invalid", "HardCodedStringLiteral"})
  private RecordView(R record,
                     RecordField initialSort,
                     Function<R, Integer> getIdFunction, Setter<R, Integer> setIdFunction,
                     Function<R, String> getSourceFunction, Setter<R, String> setSourceFunction,
                     Function<R, String> getUserNameFunction, Setter<R, String> setUserNameFunction,
                     Function<R, String> getPasswordFunction, Setter<R, String> setPasswordFunction,
                     Function<R, String> getNotesFunction, Setter<R, String> setNotesFunction
   ) {
    super(new BorderLayout());
    currentRecord = record;
    final JLabel idField = (JLabel) addField("ID", false, RecordField.ID, initialSort);
    final JTextComponent sourceField = (JTextComponent) addField("Source", true, RecordField.SOURCE, initialSort);
    final JTextComponent usernameField = (JTextComponent) addField("User Name", true, RecordField.USERNAME, initialSort);
    final JTextComponent pwField = (JTextComponent) addField("Password", true, RecordField.PASSWORD, initialSort);
    final JTextComponent notesField = addNotesField();
    assert getIdFunction != null : "Null id getter";
    assert setIdFunction != null : "Null id Setter";
    idBinding = FieldBinding.bindInteger(getIdFunction, setIdFunction, idField);
    final FieldBinding.StringEditableBinding<R> sourceBinding = FieldBinding.bindEditableString(getSourceFunction, setSourceFunction, sourceField);
    final FieldBinding.StringEditableBinding<R> userNameBinding = FieldBinding.bindEditableString(getUserNameFunction, setUserNameFunction, usernameField);
    final FieldBinding.StringEditableBinding<R> passwordBinding = FieldBinding.bindEditableString(getPasswordFunction, setPasswordFunction, pwField);
    final FieldBinding.StringEditableBinding<R> notesBinding = FieldBinding.bindEditableString(getNotesFunction, setNotesFunction, notesField);
    allBindings = Arrays.asList(idBinding, sourceBinding, userNameBinding, passwordBinding, notesBinding);
    
    // currentRecord has null values for lots of non-null fields. This should clean those fields up.
    for (FieldBinding<R, ?, ?> b : allBindings) {
      cleanValue(b, currentRecord);
    }
    idBinding.setValue(currentRecord, NO_RECORD);
    add(makeTopPanel(), BorderLayout.PAGE_START);

    installStandardCaret(sourceField);
    installStandardCaret(usernameField);
    installStandardCaret(pwField);
    installStandardCaret(notesField);
  }

  /**
   * Clean the value during initialization. This needs to be a separate method because there's no way to infer the 
   * type of T if I put this code in the original loop. Without T, there's no way for the compiler to know that the 
   * value returned by binding.getValue() is the same type as the one we need to pass to setValue().
   * @param binding The FieldBinding
   * @param record The record to clean
   * @param <T> The type of the record.
   */
  private <T> void cleanValue(@UnderInitialization RecordView<R> this, FieldBinding<R, T, ?> binding, R record) {
    binding.setValue(record, binding.getValue(record));
  }

  private JPanel makeTopPanel() {
    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.add(labelPanel, BorderLayout.LINE_START);
    topPanel.add(fieldPanel, BorderLayout.CENTER);
    topPanel.add(checkBoxPanel, BorderLayout.LINE_END);
    return topPanel;
  }

  @SuppressWarnings("HardCodedStringLiteral")
  public void setController(RecordController<R, ?> theController) {
    controller = theController;
  }

  /**
   * On the Mac, the AquaCaret will get installed. This caret has an annoying feature of selecting all the text on a
   * focus-gained event. If this isn't bad enough, it also fails to check temporary vs permanent focus gain, so it 
   * gets triggered on a focused JTextComponent whenever a menu is released! This removes the Aqua Caret and installs
   * a standard caret. It's safe to use on any platform.
   * @param component The component to repair. This is usually a JTextField or JTextArea.
   */
  public static void installStandardCaret(JTextComponent component) {
    final Caret priorCaret = component.getCaret();
    int blinkRate = priorCaret.getBlinkRate();
    if (priorCaret instanceof PropertyChangeListener) {
      // com.apple.laf.AquaCaret, the troublemaker, installs this listener which doesn't get removed when the Caret 
      // gets uninstalled.
      component.removePropertyChangeListener((PropertyChangeListener) priorCaret);
    }
    DefaultCaret caret = new DefaultCaret();
    component.setCaret(caret);
    caret.setBlinkRate(blinkRate); // Starts the new caret blinking.
  }

  private JComponent addField(final String labelText, final boolean editable, final RecordField orderField, RecordField initialSort) {
    //noinspection StringConcatenation,MagicCharacter
    JLabel label = new JLabel(labelText + ':');
    labelPanel.add(label);
    JComponent field;
    if (editable) {
      field = new JTextField(TEXT_COLUMNS);
    } else {
      field = new JLabel();
    }
    fieldPanel.add(field);
    JRadioButton orderBy = new JRadioButton("");
    buttonGroup.add(orderBy);
    if (orderField == initialSort) {
      orderBy.setSelected(true);
    }
    checkBoxPanel.add(orderBy);
    ItemListener checkBoxListener = (itemEvent) -> {
      if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
        controller.specifyOrder(orderField);
      }
    };
    orderBy.addItemListener(checkBoxListener);
    return field;
  }
  
  private JTextComponent addNotesField() {
    final JTextArea wrappedField = new JTextArea(TEXT_ROWS, TEXT_COLUMNS);
    wrappedField.setWrapStyleWord(true);
    wrappedField.setLineWrap(true);
    JComponent scrollPane = wrap(wrappedField);
    add(BorderLayout.CENTER, scrollPane);
    return wrappedField;
  }

  private JComponent wrap(JComponent wrapped) {
    return new JScrollPane(wrapped, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
  }
  
  void setCurrentRecord(R newRecord) {
    currentRecord = newRecord;
    for (FieldBinding<R, ?, ?> binding: allBindings) {
      binding.prepareEditor(newRecord);
    }
  }
  
  boolean recordHasChanged() {
    for (FieldBinding<R, ?, ?> binding: allBindings) {
      if (binding.isEditable() && binding.propertyHasChanged(currentRecord)) {
        return true;
      }
    }
    return false;
  }
  
  public boolean saveOnExit() throws SQLException {
    final R current = getCurrentRecord();
    // Test four cases:
    // 1. New Record with data
    // 2. New Record with no data
    // 3. Existing record with changes
    // 4. Existing record with no changes
//    final Integer currentId = idBinding.getValue(current);
    final boolean hasChanged = recordHasChanged();
//    System.out.printf("if %d == 0 && recordHasChanged = %b ...%n", currentId, hasChanged);
    if (hasChanged) {
      loadUIData(current);
//      System.out.printf("Saving%n");
      return true;
    }
//    System.out.println("Not Saving");
    return false;
  }

  public R getCurrentRecord() {
    return currentRecord;
  }

  /**
   * Reads the data from the editor fields and loads it into the current record's 
   * @param theRecord Unused. I'm still not entirely convinced that I'll never need it, so I'm keeping it around for now.
   */
  public void loadUIData(@SuppressWarnings("unused") R theRecord) {
    final int id = (currentRecord == null) ? 0 : idBinding.getValue(currentRecord);
    for (FieldBinding<R, ?, ?> binding : allBindings) {
      if (binding.isEditable()) {
        binding.saveEdit(currentRecord);
      }
    }
  }

//  public void setButtonState(boolean nextEnabled, boolean prevEnabled) {
//    
//  }
  
//  private class LoggingCaret extends DefaultCaret {
//    LoggingCaret() {
//      super();
//    }
//
//    @Override
//    public void setDot(final int dot) {
//      super.setDot(dot);
//      System.out.printf("Dot set to %d to %d (requested %d)%n", getDot(), getMark(), dot);
//    }
//
//    @Override
//    public void moveDot(final int dot) {
//      System.out.printf("Dot mov to %d to %d (requested %d)%n", getDot(), getMark(), dot);
//    }
//  }
    @SuppressWarnings("initialization.fields.uninitialized")
    public static class Builder<RR> {
      private RR record;
      private RecordField initialSort;
      private Function<RR, Integer> getId;
      private Setter<RR, Integer> setId;
      private Function<RR, String> getSource;
      private Setter<RR, String> setSource;
      private Function<RR, String> getUserName;
      private Setter<RR, String> setUserName;
      private Function<RR, String> getPassword;
      private Setter<RR, String> setPassword;
      private Function<RR, String> getNotes;
      private Setter<RR, String> setNotes;
      public Builder(RR record, RecordField initialSort) {
        this.record = record;
        this.initialSort = initialSort;
      }
  
    public Builder<RR> id(Function<RR, Integer> getter, Setter<RR, Integer> setter) {
      getId = getter;
      setId = setter;
      return this;
    }
  
    public Builder<RR> source(Function<RR, String> getter, Setter<RR, String> setter) {
      getSource = getter;
      setSource = setter;
      return this;
    }
  
    public Builder<RR> userName(Function<RR, String> getter, Setter<RR, String> setter) {
      getUserName = getter;
      setUserName = setter;
      return this;
    }
  
    public Builder<RR> password(Function<RR, String> getter, Setter<RR, String> setter) {
      getPassword = getter;
      setPassword = setter;
      return this;
    }
  
    public Builder<RR> notes(Function<RR, String> getter, Setter<RR, String> setter) {
      getNotes = getter;
      setNotes = setter;
      return this;
    }
    
    public RecordView<RR> build() {
      return new RecordView<>(
          record, 
          initialSort,
          getId, setId, 
          getSource, setSource, 
          getUserName, setUserName, 
          getPassword, setPassword, 
          getNotes, setNotes
      );
    }
  }
}
