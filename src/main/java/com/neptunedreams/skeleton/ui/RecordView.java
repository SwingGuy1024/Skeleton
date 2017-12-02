package com.neptunedreams.skeleton.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
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
import com.neptunedreams.skeleton.data.RecordField;

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
  private JPanel labelPanel = new JPanel(new GridLayout(0, 1));
  private JPanel fieldPanel = new JPanel(new GridLayout(0, 1));
  private JPanel checkBoxPanel = new JPanel(new GridLayout(0, 1));
  private ButtonGroup buttonGroup = new ButtonGroup();

  private Function<R, Integer> idGetter;
  private Setter<R, Integer> idSetter;
  private Function<R, String> sourceGetter;
  private Setter<R, String> sourceSetter;
  private Function<R, String> userNameGetter;
  private Setter<R, String> userNameSetter;
  private Function<R, String> passwordGetter;
  private Setter<R, String> passwordSetter;
  private Function<R, String> notesGetter;
  private Setter<R, String> notesSetter;

  private JLabel idField;
  private JTextComponent sourceField;
  private JTextComponent usernameField;
  private JTextComponent pwField;
  private JTextComponent notesField;

  @SuppressWarnings("HardCodedStringLiteral")
  private R currentRecord; // = new Record("D", "D", "D", "D");
  
  private RecordController<R, ?> controller;

  private RecordView(R record,
                     Function<R, Integer> getIdFunction, Setter<R, Integer> setIdFunction,
                     Function<R, String> getSourceFunction, Setter<R, String> setSourceFunction,
                     Function<R, String> getUserNameFunction, Setter<R, String> setUserNameFunction,
                     Function<R, String> getPasswordFunction, Setter<R, String> setPasswordFunction,
                     Function<R, String> getNotesFunction, Setter<R, String> setNotesFunction
   ) {
    super(new BorderLayout());
    idGetter = getIdFunction;
    idSetter = setIdFunction;
    sourceGetter = getSourceFunction;
    sourceSetter = setSourceFunction;
    userNameGetter = getUserNameFunction;
    userNameSetter = setUserNameFunction;
    passwordGetter = getPasswordFunction;
    passwordSetter = setPasswordFunction;
    notesGetter = getNotesFunction;
    notesSetter = setNotesFunction;
    idSetter.setValue(currentRecord,-999);
    add(makeTopPanel(), BorderLayout.PAGE_START);
    currentRecord = record;
  }

  private JPanel makeTopPanel() {
    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.add(labelPanel, BorderLayout.LINE_START);
    topPanel.add(fieldPanel, BorderLayout.CENTER);
    topPanel.add(checkBoxPanel, BorderLayout.LINE_END);
    return topPanel;
  }

  public void setController(RecordController<R, ?> theController) {
    controller = theController;
    
    // This is safe because setController is only called once.
    idField = (JLabel) addField("ID", false, RecordField.ID);
    sourceField = (JTextComponent) addField("Source", true, RecordField.SOURCE);
    usernameField = (JTextComponent) addField("User Name", true, RecordField.USERNAME);
    pwField = (JTextComponent) addField("Password", true, RecordField.PASSWORD);
    notesField = addNotesField();
    installStandardCaret(sourceField);
    installStandardCaret(usernameField);
    installStandardCaret(pwField);
    installStandardCaret(notesField);
  }

  /**
   * On the Mac, the AquaCaret will get installed. This caret has an annoying feature of selecting all the text on a
   * focus-gained event. If this isn't bad enough, it also fails to check temporary vs permanent focus gain, so it 
   * gets triggered on a focused JTextComponent whenever a menu is released!
   * @param component The component to repair.
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
    caret.setBlinkRate(blinkRate);
  }

  private JComponent addField(final String labelText, final boolean editable, final RecordField orderField) {
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
    if (orderField == controller.getOrder()) {
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
    JComponent scrollPane = wrap(wrappedField);
    add(BorderLayout.CENTER, scrollPane);
    return wrappedField;
  }

  private JComponent wrap(JComponent wrapped) {
    return new JScrollPane(wrapped, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
  }
  
  void setCurrentRecord(R newRecord) {
    currentRecord = newRecord;
//    if (currentRecord == null) {
//      idField.setText("");
//      sourceField.setText("");
//      usernameField.setText("");
//      pwField.setText("");
//      notesField.setText("");
//    } else {
      idField.setText(String.valueOf(idGetter.apply(currentRecord)));
      sourceField.setText(sourceGetter.apply(currentRecord));
      usernameField.setText(userNameGetter.apply(currentRecord));
      pwField.setText(passwordGetter.apply(currentRecord));
      notesField.setText(notesGetter.apply(currentRecord));
//    }
  }
  
  boolean recordHasChanged() {
//    if (currentRecord.getId() == 0) {
//      return
//          !usernameField.getText().trim().isEmpty() || 
//          !pwField.getText().trim().isEmpty() ||
//          !sourceField.getText().trim().isEmpty() ||
//          !notesField.getText().trim().isEmpty();
//    }
    //noinspection EqualsReplaceableByObjectsCall
    return !userNameGetter.apply(currentRecord).trim().equals(usernameField.getText().trim()) ||
        !passwordGetter.apply(currentRecord).trim().equals(pwField.getText().trim()) ||
        !sourceGetter.apply(currentRecord).trim().equals(sourceField.getText().trim()) ||
        !notesGetter.apply(currentRecord).trim().equals(notesField.getText().trim());
  }
  
  public void saveOnExit() throws SQLException {
    final R current = getCurrentRecord();
    if ((idGetter.apply(current) == 0) && recordHasChanged()) {
      loadNewData(current);
      controller.getDao().save(current);
    }
  }

  public R getCurrentRecord() {
//    if (recordHasChanged()) {
//      Record newRecord = new Record();
//      final int id = (currentRecord == null) ? 0 : currentRecord.getId();
//      newRecord.setId(id);
//      newRecord.setSource(sourceField.getText().trim());
//      newRecord.setUserName(usernameField.getText().trim());
//      newRecord.setPassword(pwField.getText().trim());
//      newRecord.setNotes(notesField.getText().trim());
//      return newRecord;
//    }
    return currentRecord;
  }
  
  public void loadNewData(R theRecord) {
    final int id = (currentRecord == null) ? 0 : idGetter.apply(currentRecord);
    idSetter.setValue(theRecord, id);
    sourceSetter.setValue(theRecord, sourceField.getText().trim());
    userNameSetter.setValue(theRecord, usernameField.getText().trim());
    passwordSetter.setValue(theRecord, pwField.getText().trim());
    notesSetter.setValue(theRecord, notesField.getText().trim());
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
    public static class Builder<RR> {
      private RR record;
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
      public Builder(RR record) {
        this.record = record;
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
          getId, setId, 
          getSource, setSource, 
          getUserName, setUserName, 
          getPassword, setPassword, 
          getNotes, setNotes
      );
    }
  
  }
}
