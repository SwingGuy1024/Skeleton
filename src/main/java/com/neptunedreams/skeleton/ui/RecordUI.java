package com.neptunedreams.skeleton.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Consumer;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import com.ErrorReport;
import com.neptunedreams.skeleton.data.RecordField;
import com.neptunedreams.skeleton.task.ParameterizedCallable;
import com.neptunedreams.skeleton.task.QueuedTask;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Functions
 * Next, Previous
 * Find all
 * Find Text
 * Sort By
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/29/17
 * <p>Time: 12:50 PM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings("HardCodedStringLiteral")
public class RecordUI<R> extends JPanel implements RecordModelListener {

  private static final long DELAY = 1000L;
  private JTextField findField = new JTextField(10);
  private final RecordController<R, Integer> controller;
  private ButtonGroup buttonGroup = new ButtonGroup();
  private RecordView<R> view;
  private final @NonNull RecordModel<R> recordModel;
  private JButton prev = new JButton(Resource.getLeftArrow());
  private JButton next = new JButton(Resource.getRightArrow());
  private JButton first = new JButton(Resource.getFirst());
  private JButton last = new JButton(Resource.getLast());
  private JLabel infoLine = new JLabel("");
  private final ParameterizedCallable<String, Collection<R>> callable = createCallable();
  private final Consumer<Collection<R>> recordConsumer = createRecordConsumer();
  private @NonNull QueuedTask<String, Collection<R>> queuedTask = new QueuedTask<>(DELAY, callable, recordConsumer);

  @SuppressWarnings({"method.invocation.invalid","argument.type.incompatible"}) // add(), setBorder(), etc not properly annotated in JDK.
  public RecordUI(@NonNull RecordModel<R> model, RecordView<R> theView, RecordController<R, Integer> theController) {
    super(new BorderLayout());
    recordModel = model;
    view = theView;
    add(theView, BorderLayout.CENTER);
    add(createControlPanel(), BorderLayout.PAGE_START);
    add(createTrashPanel(), BorderLayout.PAGE_END);
    controller = theController;
    setBorder(new MatteBorder(4, 4, 4, 4, getBackground()));
    recordModel.addModelListener(this); // argument.type.incompatible checker error suppressed
    
//    findField.addPropertyChangeListener("text", 
//        (evt) -> System.out.printf("Change %s from %s to %s%n", evt.getPropertyName(), evt.getOldValue(), evt.getNewValue()));
    findField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(final DocumentEvent e) {
        process(e);
      }

      @Override
      public void removeUpdate(final DocumentEvent e) {
        process(e);
      }

      @Override
      public void changedUpdate(final DocumentEvent e) {
        process(e);
      }
      
      private void process(DocumentEvent e) {
        final Document document = e.getDocument();
        try {
          final String text = document.getText(0, document.getLength());
          assert queuedTask != null;
          queuedTask.feedData(text);
        } catch (BadLocationException e1) {
          e1.printStackTrace();
        }
      }
    });
  }

  private JPanel createControlPanel() {
    JPanel controlPanel = new JPanel(new BorderLayout());
    controlPanel.add(createSearchRadioPanel(), BorderLayout.LINE_END);
    controlPanel.add(createButtonPanel(), BorderLayout.CENTER);
    return controlPanel;
  }

  private JPanel createButtonPanel() {
    JPanel buttonPanel = new JPanel(new BorderLayout());
    buttonPanel.add(getSearchField(), BorderLayout.PAGE_START);
    buttonPanel.add(getButtons(), BorderLayout.PAGE_END);
//    buttonPanel.add(createTrashPanel(), BorderLayout.PAGE_END);
    return buttonPanel;
  }

  private JPanel createTrashPanel() {
    JPanel trashPanel = new JPanel(new BorderLayout());
    JButton trashRecord = new JButton(Resource.getBin());
    trashPanel.add(trashRecord, BorderLayout.LINE_END);
    trashRecord.addActionListener((e)->delete());

    assert infoLine != null;
    trashPanel.add(infoLine, BorderLayout.LINE_START);
    assert recordModel != null;
    recordModel.addModelListener(this);
    return trashPanel;
  }

  private void delete() {
    if (JOptionPane.showConfirmDialog(this,
        "Are you sure?",
        "Delete Record", 
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION
        ) {
      R selectedRecord = recordModel.getSelectedRecord();
      try {
        controller.delete(selectedRecord); // Removes from database
        recordModel.deleteSelected(true, recordModel.getRecordIndex());
        view.setCurrentRecord(recordModel.getSelectedRecord());
      } catch (SQLException e) {
        ErrorReport.reportException("delete current record", e);
      }
    }
  }

  private JPanel getButtons() {
    JPanel buttons = new JPanel(new GridLayout(1, 0));
    JButton add = new JButton(Resource.getAdd());
//    final JButton importBtn = new JButton("Imp");
    buttons.add(add);
    buttons.add(first);
    buttons.add(prev);
    buttons.add(next);
    buttons.add(last);
//    buttons.add(importBtn);
    
    assert controller != null;
    add.addActionListener((e)->controller.addBlankRecord());
    prev.addActionListener((e)->recordModel.goPrev());
    next.addActionListener((e)->recordModel.goNext());
    first.addActionListener((e) -> recordModel.goFirst());
    last.addActionListener((e) -> recordModel.goLast());
//    importBtn.addActionListener((e) -> doImport());
    JPanel flowPanel = new JPanel(new FlowLayout());
    flowPanel.add(buttons);
    return flowPanel;
  }
  
//  private void doImport() {
//    ImportDialog importDialog = new ImportDialog((Window) getRootPane().getParent(), controller.getDao());
//    importDialog.setVisible(true);
//  }

  @SuppressWarnings("method.invocation.invalid")
  private JPanel getSearchField() {
    JLabel findIcon = Resource.getMagnifierLabel();
    RecordView.installStandardCaret(findField);
    JPanel searchPanel = new JPanel(new BorderLayout());
    searchPanel.add(findIcon, BorderLayout.LINE_START);
    searchPanel.add(findField, BorderLayout.CENTER);
    findField.addActionListener((e) -> findText());
    return searchPanel;
  }
  
  private void findText() {
    ButtonModel selectedModel = buttonGroup.getSelection();
    if (selectedModel instanceof EnumToggleModel) {
      RecordField field = ((EnumToggleModel)selectedModel).getField();
      
      controller.findTextInField(findField.getText(), field);
    } else {
      controller.findTextAnywhere(findField.getText());
    }
  }
  
  @SuppressWarnings("HardCodedStringLiteral")
  private JPanel createSearchRadioPanel(@UnknownInitialization RecordUI<R>this) {
    JRadioButton all = new JRadioButton("All");
    JRadioButton source = new JRadioButton("Source");
    JRadioButton userName = new JRadioButton("User Name");
    JRadioButton pw = new JRadioButton("Password");
    JRadioButton notes = new JRadioButton("Notes");
    
    setButtonModel(source, RecordField.SOURCE); // method.invocation.invalid on setButtonModel, 
    setButtonModel(userName, RecordField.USERNAME);
    setButtonModel(pw, RecordField.PASSWORD);
    setButtonModel(notes, RecordField.NOTES);

    assert buttonGroup != null;
    buttonGroup.add(all);
    buttonGroup.add(source);
    buttonGroup.add(userName);
    buttonGroup.add(pw);
    buttonGroup.add(notes);
    all.setSelected(true);

    JPanel radioPanel = new JPanel(new GridLayout(0, 1));
    radioPanel.add(all);
    radioPanel.add(source);
    radioPanel.add(userName);
    radioPanel.add(pw);
    radioPanel.add(notes);
    return radioPanel;
  }
  
  private void setButtonModel(@UnknownInitialization RecordUI<R> this, JRadioButton button, RecordField field) {
    button.setModel(new EnumToggleModel(field));
  }
  
  private void loadInfoLine() {
    final R selectedRecord = recordModel.getSelectedRecord();
    int entryItem = (controller.getDao().getPrimaryKey(selectedRecord) == null) ? 1 : 0;
    //noinspection HardcodedFileSeparator
    String info = String.format("%d/%d of %d", 
        recordModel.getRecordIndex()+1, recordModel.getSize(), recordModel.getTotal() + entryItem);
    infoLine.setText(info);
  }

  @Override
  public void modelListChanged(final int newSize) {
    boolean pnEnabled = newSize > 1;
    prev.setEnabled(pnEnabled);
    next.setEnabled(pnEnabled);
    first.setEnabled(pnEnabled);
    last.setEnabled(pnEnabled);
    loadInfoLine();
  }
  
  private ParameterizedCallable<String, Collection<R>> createCallable(@UnknownInitialization RecordUI<R> this) {
    return new ParameterizedCallable<String, Collection<R>>() {
      @Override
      public Collection<R> call() throws InterruptedException {
        assert buttonGroup != null;
        ButtonModel selectedModel = buttonGroup.getSelection();
        try {
          assert controller != null;
          assert findField != null;
          if (selectedModel instanceof EnumToggleModel) {
            RecordField field = ((EnumToggleModel) selectedModel).getField();
            return controller.findRecordsInField(findField.getText(), field);
          } else {
            return controller.findRecordsAnywhere(findField.getText());
          }
        } catch(SQLException e){
          e.printStackTrace();
          return new LinkedList<>();
        }
      }
    };
  }

  private Consumer<Collection<R>> createRecordConsumer(@UnknownInitialization RecordUI<R>this) {
    assert controller != null;
    return records -> SwingUtilities.invokeLater(() -> controller.setFoundRecords(records));
  }

  @Override
  public void indexChanged(final int index, int prior) {
    loadInfoLine();
  }

  private static class EnumToggleModel extends JToggleButton.ToggleButtonModel {
    private final RecordField field;
    
    EnumToggleModel(RecordField theField) {
      super();
      field = theField;
    }

    @SuppressWarnings("WeakerAccess")
    public RecordField getField() {
      return field;
    }
  }
}
