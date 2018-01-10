package com.neptunedreams.skeleton.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.Collection;
import java.util.function.Consumer;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import com.ErrorReport;
import com.google.common.eventbus.Subscribe;
import com.neptunedreams.framework.ui.ButtonGroupListener;
import com.neptunedreams.framework.ui.EnumGroup;
import com.neptunedreams.framework.ui.HidingPanel;
import com.neptunedreams.skeleton.data.RecordField;
import com.neptunedreams.skeleton.task.ParameterizedCallable;
import com.neptunedreams.skeleton.task.QueuedTask;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
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

  // TODO:  The QueuedTask is terrific, but it doesn't belong in this class. It belongs in the Controller. That way,
  // todo   it can be accessed by other UI classes like RecordView. To do this, I also need to move the SearchOption
  // todo   to the controller, as well as the searchField. (Maybe I should just write an API so the Controller can just
  // todo   query the RecordUI for the selected state.) It will be a bit of work, but it will be a cleaner UI, which
  // todo   is easier to maintain. This will also allow me to implement instant response to changing the sort order.
  // todo   Maybe the way to do this would be to create a UIModel class that keeps track of all the UI state. Maybe 
  // todo   that way, the controller won't need to keep an instance of RecordView.

  private static final long DELAY = 1000L;
  private JTextField findField = new JTextField(10);
  private final RecordController<R, Integer> controller;
  private EnumGroup<RecordField> buttonGroup = new EnumGroup<>();
  private final @NonNull RecordModel<R> recordModel;
  private JButton prev = new JButton(Resource.getLeftArrow());
  private JButton next = new JButton(Resource.getRightArrow());
  private JButton first = new JButton(Resource.getFirst());
  private JButton last = new JButton(Resource.getLast());
  private JLabel infoLine = new JLabel("");
  private final EnumGroup<SearchOption> optionsGroup = new EnumGroup<>();

  private final HidingPanel searchOptionsPanel = makeSearchOptionsPanel(optionsGroup);

  // recordConsumer is how the QueuedTask communicates with the application code.
  private final Consumer<Collection<R>> recordConsumer = createRecordConsumer();
  private @NonNull QueuedTask<String, Collection<R>> queuedTask;

  private HidingPanel makeSearchOptionsPanel(@UnderInitialization RecordUI<R> this, EnumGroup<SearchOption> optionsGroup) {
    JPanel optionsPanel = new JPanel(new GridLayout(1, 0));
    JRadioButton findExact = optionsGroup.add(SearchOption.findExact);
    JRadioButton findAll = optionsGroup.add(SearchOption.findAll);
    JRadioButton findAny = optionsGroup.add(SearchOption.findAny);
    optionsPanel.add(findExact);
    optionsPanel.add(findAll);
    optionsPanel.add(findAny);
    optionsGroup.setSelected(SearchOption.findAny);
    
    optionsGroup.addButtonGroupListener(this::selectionChanged);

    return HidingPanel.create(optionsPanel);
  }

  @SuppressWarnings({"method.invocation.invalid","argument.type.incompatible"}) // add(), setBorder(), etc not properly annotated in JDK.
  public RecordUI(@NonNull RecordModel<R> model, RecordView<R> theView, RecordController<R, Integer> theController) {
    super(new BorderLayout());
    recordModel = model;
    add(theView, BorderLayout.CENTER);
    add(createControlPanel(), BorderLayout.PAGE_START);
    add(createTrashPanel(), BorderLayout.PAGE_END);
    controller = theController;
    setBorder(new MatteBorder(4, 4, 4, 4, getBackground()));
    recordModel.addModelListener(this); // argument.type.incompatible checker error suppressed
    
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
      
    });
    
    MasterEventBus.instance().register(this);
    queuedTask = new QueuedTask<>(DELAY, createCallable(), recordConsumer);
  }

  private void process(DocumentEvent e) {
    final Document document = e.getDocument();
    try {
      final String text = document.getText(0, document.getLength());
      queuedTask.feedData(text);
      // I'm assuming here that text can't contain \n, \r, \f, or \t, or even nbsp. If this turns out to be false,
      // I should probably filter them out in the process method.

      final boolean vis = text.trim().contains(" ");
      searchOptionsPanel.setContentVisible(vis);
    } catch (BadLocationException e1) {
      e1.printStackTrace();
    }
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
    buttonPanel.add(searchOptionsPanel, BorderLayout.CENTER);
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
//    recordModel.addModelListener(this);
    return trashPanel;
  }

  private void delete() {
    if (JOptionPane.showConfirmDialog(this,
        "Are you sure?",
        "Delete Record", 
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION
        ) {
      R selectedRecord = recordModel.getFoundRecord();
      try {
        controller.delete(selectedRecord); // Removes from database
        recordModel.deleteSelected(true, recordModel.getRecordIndex());
        MasterEventBus.instance().post(new MasterEventBus.ChangeRecord<>(recordModel.getFoundRecord()));
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
    
    add.addActionListener((e)->addBlankRecord());
    prev.addActionListener((e)->recordModel.goPrev());
    next.addActionListener((e)->recordModel.goNext());
    first.addActionListener((e) -> recordModel.goFirst());
    last.addActionListener((e) -> recordModel.goLast());
//    importBtn.addActionListener((e) -> doImport());
    JPanel flowPanel = new JPanel(new FlowLayout());
    flowPanel.add(buttons);
    return flowPanel;
  }

  private void addBlankRecord() {
    controller.addBlankRecord();
    MasterEventBus.postUserRequestedNewRecordEvent();
    loadInfoLine();
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
    RecordField field = buttonGroup.getSelected();
    final SearchOption searchOption = getSearchOption();
    if (field.isField()) {
      controller.findTextInField(findField.getText(), field, searchOption);
    } else {
      controller.findTextAnywhere(findField.getText(), searchOption);
    }
  }

  // I don't know why I don't need @UnknownInitialization or @UnderInitialization here.
  private JPanel createSearchRadioPanel() {
    JPanel radioPanel = new JPanel(new GridLayout(0, 1));
    assert buttonGroup != null;
    buttonGroup.add(RecordField.All, radioPanel);
    buttonGroup.add(RecordField.Source, radioPanel);
    buttonGroup.add(RecordField.Username, radioPanel);
    buttonGroup.add(RecordField.Password, radioPanel);
    buttonGroup.add(RecordField.Notes, radioPanel);
    buttonGroup.setSelected(RecordField.All);

    ButtonGroupListener changeListener = e -> searchNow();
    buttonGroup.addButtonGroupListener(changeListener);

    return radioPanel;
  }

  /**
   * Loads the info line. The info line looks like this:
   * 3/12 of 165
   * This means we're looking at record 3 of the 12 found records, from a total of 165 records in the database.
   */
  private void loadInfoLine() {
//    Thread.dumpStack();
    final int index;
    final int foundSize;
    index = recordModel.getRecordIndex() + 1;
    foundSize = recordModel.getSize();
    try {
      int total = controller.getDao().getTotal();
      if (total < foundSize) {
        // This happens when the user hits the + button.
        total = foundSize;
      }
      //noinspection HardcodedFileSeparator
      String info = String.format("%d/%d of %d", index, foundSize, total);
      infoLine.setText(info);
//      System.err.printf("Info: %S%n", info); // NON-NLS
    } catch (SQLException e) {
      ErrorReport.reportException("loadInfoLine()", e);
    }
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
  
  /*
    NullnessChecker notes:
    This used to be called during construction. It specified an implicit parameter. But this created a compiler
    error when I called getSearchOption. This is due to the nullness checker bug where it doesn't know that a
    lambda or anonymous class only gets called after initialization is complete. 
    
    I fixed this by lazily instantiating the QueuedTask that used the return value of this method. That way, I could
    remove the @UnderInitialization annotation of the implicit this parameter. This method is now called after
    construction completes, so it works fine. Very annoying that I need to do this, but it's a relatively clean
    solution.  
   */
  private ParameterizedCallable<String, Collection<R>> createCallable() {
    return new ParameterizedCallable<String, Collection<R>>("") {
      @Override
      public Collection<R> call() throws InterruptedException {
        final String inputData = this.getInputData();
        return retrieveNow(inputData);
      }
    };
  }
  
  private Collection<R> retrieveNow(String text) {
    assert controller != null;
    assert buttonGroup != null;
    return controller.retrieveNow(buttonGroup.getSelected(), getSearchOption(), text);
  }
  
  @Subscribe
  public void doSearchNow(MasterEventBus.SearchNowEvent searchNowEvent) {
    searchNow();
  }

  // This is public because I expect other classes to use it in the future. 
  @SuppressWarnings("WeakerAccess")
  public void searchNow() {
    assert SwingUtilities.isEventDispatchThread();
    assert findField != null;
    recordConsumer.accept(retrieveNow(findField.getText()));
  }

  private SearchOption getSearchOption() {
    return searchOptionsPanel.isContentVisible() ? optionsGroup.getSelected() : SearchOption.findExact;
  }

  @SuppressWarnings("dereference.of.nullable") // controller is null when we call this, but not when we call the lambda.
  private Consumer<Collection<R>> createRecordConsumer(@UnderInitialization RecordUI<R>this) {
    return records -> SwingUtilities.invokeLater(() -> controller.setFoundRecords(records));
  }

  @Override
  public void indexChanged(final int index, int prior) {
    loadInfoLine();
  }

  private void selectionChanged(@SuppressWarnings("unused") ButtonModel selectedButtonModel) { searchNow(); }
}
