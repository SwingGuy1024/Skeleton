package com.neptunedreams.skeleton.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
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
import com.neptunedreams.skeleton.data.Record;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/29/17
 * <p>Time: 10:54 AM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings("WeakerAccess")
public class RecordView extends JPanel {
  private static final int TEXT_COLUMNS = 40;
  private static final int TEXT_ROWS = 6;
  private JPanel labelPanel = new JPanel(new GridLayout(0, 1));
  private JPanel fieldPanel = new JPanel(new GridLayout(0, 1));
  private JPanel checkBoxPanel = new JPanel(new GridLayout(0, 1));
  private ButtonGroup buttonGroup = new ButtonGroup();
  
  private JLabel idField;
  private JTextComponent sourceField;
  private JTextComponent usernameField;
  private JTextComponent pwField;
  private JTextComponent notesField;
  
  @SuppressWarnings("HardCodedStringLiteral")
  private Record currentRecord = new Record("D", "D", "D", "D");
  
  private RecordController controller;

  public RecordView() {
    super(new BorderLayout());
    currentRecord.setId(-999);
    add(makeTopPanel(), BorderLayout.PAGE_START);
  }

  private JPanel makeTopPanel() {
    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.add(labelPanel, BorderLayout.LINE_START);
    topPanel.add(fieldPanel, BorderLayout.CENTER);
    topPanel.add(checkBoxPanel, BorderLayout.LINE_END);
    return topPanel;
  }

  public void setController(RecordController theController) {
    controller = theController;
    
    // This is safe because setController is only called once.
    idField = (JLabel) addField("ID", false, Record.FIELD.ID);
    sourceField = (JTextComponent) addField("Source", true, Record.FIELD.SOURCE);
    usernameField = (JTextComponent) addField("User Name", true, Record.FIELD.USERNAME);
    pwField = (JTextComponent) addField("Password", true, Record.FIELD.PASSWORD);
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

  private JComponent addField(final String labelText, final boolean editable, final Record.FIELD orderField) {
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
  
  void setCurrentRecord(Record newRecord) {
    currentRecord = newRecord;
//    if (currentRecord == null) {
//      idField.setText("");
//      sourceField.setText("");
//      usernameField.setText("");
//      pwField.setText("");
//      notesField.setText("");
//    } else {
      idField.setText(String.valueOf(currentRecord.getId()));
      sourceField.setText(currentRecord.getSource());
      usernameField.setText(currentRecord.getUserName());
      pwField.setText(currentRecord.getPassword());
      notesField.setText(currentRecord.getNotes());
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
    return !currentRecord.getUserName().trim().equals(usernameField.getText().trim()) ||
        !currentRecord.getPassword().trim().equals(pwField.getText().trim()) ||
        !currentRecord.getSource().trim().equals(sourceField.getText().trim()) ||
        !currentRecord.getNotes().trim().equals(notesField.getText().trim());
  }
  
  public void saveOnExit() throws SQLException {
    final Record current = getCurrentRecord();
    if ((current.getId() == 0) && recordHasChanged()) {
      loadNewData(current);
      controller.getDao().save(current);
    }
  }

  public Record getCurrentRecord() {
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
  
  public void loadNewData(Record theRecord) {
    final int id = (currentRecord == null) ? 0 : currentRecord.getId();
    theRecord.setId(id);
    theRecord.setSource(sourceField.getText().trim());
    theRecord.setUserName(usernameField.getText().trim());
    theRecord.setPassword(pwField.getText().trim());
    theRecord.setNotes(notesField.getText().trim());
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
}
