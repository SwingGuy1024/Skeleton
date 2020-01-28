package com.neptunedreams.skeleton.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ButtonModel;
import javax.swing.FocusManager;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import com.google.common.eventbus.Subscribe;
import com.neptunedreams.framework.ErrorReport;
import com.neptunedreams.framework.data.RecordModel;
import com.neptunedreams.framework.data.RecordModelListener;
import com.neptunedreams.framework.data.SearchOption;
import com.neptunedreams.framework.event.MasterEventBus;
import com.neptunedreams.framework.task.ParameterizedCallable;
import com.neptunedreams.framework.task.QueuedTask;
import com.neptunedreams.framework.ui.ButtonGroupListener;
import com.neptunedreams.framework.ui.EnumGroup;
import com.neptunedreams.framework.ui.HidingPanel;
import com.neptunedreams.framework.ui.RecordController;
import com.neptunedreams.framework.ui.SwipeDirection;
import com.neptunedreams.framework.ui.SwipeView;
import com.neptunedreams.skeleton.data.SiteField;
import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
//import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.UnknownKeyFor;

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
public final class RecordUI<R> extends JPanel implements RecordModelListener {

  // TODO:  The QueuedTask is terrific, but it doesn't belong in this class. It belongs in the Controller. That way,
  // todo   it can be accessed by other UI classes like RecordView. To do this, I also need to move the SearchOption
  // todo   to the controller, as well as the searchField. (Maybe I should just write an API so the Controller can just
  // todo   query the RecordUI for the selected state.) It will be a bit of work, but it will be a cleaner UI, which
  // todo   is easier to maintain. This will also allow me to implement instant response to changing the sort order.
  // todo   Maybe the way to do this would be to create a UIModel class that keeps track of all the UI state. Maybe 
  // todo   that way, the controller won't need to keep an instance of RecordView.
  
  // Todo:  Add keyboard listener to JLayer to handle left and right arrow keys. They should only activate when the 
  // todo   focus is not held by a JTextComponent.

  private static final long DELAY = 1000L;

  // We set the initial text to a space, so we can fire the initial search by setting the text to the empty String.
  private JTextField findField = new JTextField(" ",10);
  private final RecordController<R, Integer, SiteField> controller;
  private EnumGroup<SiteField> searchFieldGroup = new EnumGroup<>();
  private final @NonNull RecordModel<? extends R> recordModel;
  private JButton prev = new JButton(Resource.getLeftArrow());
  private JButton next = new JButton(Resource.getRightArrow());
  private JButton first = new JButton(Resource.getFirst());
  private JButton last = new JButton(Resource.getLast());

  private JLabel infoLine = new JLabel("");
  private final EnumGroup<SearchOption> optionsGroup = new EnumGroup<>();
  private @MonotonicNonNull SwipeView<RecordView<R>> swipeView=null;

  private final HidingPanel searchOptionsPanel = makeSearchOptionsPanel(optionsGroup);

  // recordConsumer is how the QueuedTask communicates with the application code.
  private final Consumer<Collection<@NonNull R>> recordConsumer = createRecordConsumer();
  private @NonNull QueuedTask<String, Collection<R>> queuedTask;

  @SuppressWarnings("methodref.inference.unimplemented")
  private HidingPanel makeSearchOptionsPanel(
      @UnderInitialization RecordUI<R> this,
      @SuppressWarnings("BoundedWildcard") EnumGroup<SearchOption> searchOptionsGroup
  ) {
    JPanel optionsPanel = new JPanel(new GridLayout(1, 0));
    JRadioButton findExact = searchOptionsGroup.add(SearchOption.findWhole);
    JRadioButton findAll = searchOptionsGroup.add(SearchOption.findAll);
    JRadioButton findAny = searchOptionsGroup.add(SearchOption.findAny);
    optionsPanel.add(findExact);
    optionsPanel.add(findAll);
    optionsPanel.add(findAny);
    searchOptionsGroup.setSelected(SearchOption.findAny);

//    optionsGroup.addButtonGroupListener(selectedButtonModel -> selectionChanged(selectedButtonModel));
    @SuppressWarnings("methodref.receiver.bound.invalid") // I don't understand why I need this.
    @UnknownKeyFor @Initialized ButtonGroupListener selectionChanged = this::selectionChanged;
    searchOptionsGroup.addButtonGroupListener(selectionChanged); // Using a lambda is an error. This is a warning. 

    final HidingPanel hidingPanel = HidingPanel.create(optionsPanel);
    hidingPanel.setDisableInsteadOfHide(true);
    return hidingPanel;
  }

  @SuppressWarnings({"method.invocation.invalid", "argument.type.incompatible"})
  // add(), setBorder(), etc not properly annotated in JDK.
  public RecordUI(
      @NonNull RecordModel<? extends R> model,
      RecordView<R> theView,
      @SuppressWarnings("BoundedWildcard") RecordController<R, Integer, SiteField> theController
  ) {
    super(new BorderLayout());
    recordModel = model;
    final JLayer<RecordView<R>> layer = wrapInLayer(theView);
    add(layer, BorderLayout.CENTER);
    add(createControlPanel(), BorderLayout.PAGE_START);
    add(createTrashPanel(), BorderLayout.PAGE_END);
    controller = theController;
    setBorder(new MatteBorder(4, 4, 4, 4, getBackground()));
    //noinspection ThisEscapedInObjectConstruction
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

    //noinspection ThisEscapedInObjectConstruction
    MasterEventBus.registerMasterEventHandler(this);
    queuedTask = new QueuedTask<>(DELAY, createCallable(), recordConsumer);
    queuedTask.launch();
  }
  
  @SuppressWarnings("ResultOfObjectAllocationIgnored")
  private void setupActions(SwipeView<RecordView<R>> swipeView) {
    new ButtonAction("Previous", KeyEvent.VK_LEFT, 0, ()-> swipeView.swipeRight(recordModel::goPrev));
    new ButtonAction("Next", KeyEvent.VK_RIGHT, 0, ()-> swipeView.swipeLeft(recordModel::goNext));
    new ButtonAction("First Record", KeyEvent.VK_LEFT, InputEvent.META_DOWN_MASK, () -> swipeView.swipeRight(recordModel::goFirst));
    new ButtonAction("Last Record", KeyEvent.VK_RIGHT, InputEvent.META_DOWN_MASK, () -> swipeView.swipeLeft(recordModel::goLast));
  }

  @SuppressWarnings("CloneableClassWithoutClone")
  private final class ButtonAction extends AbstractAction {
    private final Runnable operation;
    private FocusManager focusManager = FocusManager.getCurrentManager();


    private ButtonAction(final String name, int key, int modifiers, final Runnable theOp) {
      super(name);
      operation = theOp;
      KeyStroke keyStroke = KeyStroke.getKeyStroke(key, modifiers);
      
      InputMap inputMap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
      ActionMap actionMap = getActionMap();
      inputMap.put(keyStroke, name);
      //noinspection ThisEscapedInObjectConstruction
      actionMap.put(name, this);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
      final Component owner = focusManager.getPermanentFocusOwner();
      // The second half of this conditional doesn't work. It may be because the text components already have
      // KeyStrokes mapped to arrow keys.
      if ((!(owner instanceof JTextComponent)) || (((JTextComponent) owner).getText().isEmpty())) {
        operation.run();
      }
    }
  }


  private JLayer<RecordView<R>> wrapInLayer(@UnderInitialization RecordUI<R> this, RecordView<R> recordView) {
    swipeView = SwipeView.wrap(recordView);
    return swipeView.getLayer();
  }
  
  public void launchInitialSearch() {
    SwingUtilities.invokeLater(() -> {
      findField.setText(""); // This fires the initial search in queuedTask.
    });
    try {
      // This is how long it takes before the find starts working
      Thread.sleep(queuedTask.getDelayMilliSeconds());
    } catch (InterruptedException ignored) { }
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
        MasterEventBus.postChangeRecordEvent(recordModel.getFoundRecord());
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
    SwipeView<RecordView<R>> sView = Objects.requireNonNull(swipeView);
    sView.assignMouseDownAction(prev, recordModel::goPrev, SwipeDirection.SWIPE_RIGHT);
    sView.assignMouseDownAction(next, recordModel::goNext, SwipeDirection.SWIPE_LEFT);
    first.addActionListener((e) -> sView.swipeRight(recordModel::goFirst));
    last.addActionListener((e)  -> sView.swipeLeft(recordModel::goLast));
//    importBtn.addActionListener((e) -> doImport());
    JPanel flowPanel = new JPanel(new FlowLayout());
    flowPanel.add(buttons);
    setupActions(sView);

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
    SiteField field = searchFieldGroup.getSelected();
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
    assert searchFieldGroup != null;
    searchFieldGroup.add(SiteField.All, radioPanel);
    searchFieldGroup.add(SiteField.Source, radioPanel);
    searchFieldGroup.add(SiteField.Username, radioPanel);
    searchFieldGroup.add(SiteField.Password, radioPanel);
    searchFieldGroup.add(SiteField.Notes, radioPanel);
    searchFieldGroup.setSelected(SiteField.All);

    ButtonGroupListener changeListener = e -> searchNow();
    searchFieldGroup.addButtonGroupListener(changeListener);

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
    error when I called getSearchOption. This is due to the nullness checker bug where it doesn't know that the
    lambda or anonymous class only gets called after initialization is complete. 
    
    I fixed this by lazily instantiating the QueuedTask that used the return value of this method. That way, I could
    remove the @UnderInitialization annotation of the implicit this parameter. This method is now called after
    construction completes, so it works fine. Very annoying that I need to do this, but it's a relatively clean
    solution.  
   */
  private ParameterizedCallable<String, Collection<@NonNull R>> createCallable() {
    return new ParameterizedCallable<String, Collection<@NonNull R>>(null) {
      @Override
      public Collection<@NonNull R> call(String inputData) {
        return retrieveNow(inputData);
      }
    };
  }
  
  private Collection<@NonNull R> retrieveNow(String text) {
    assert controller != null;
    assert searchFieldGroup != null;
    return controller.retrieveNow(searchFieldGroup.getSelected(), getSearchOption(), text);
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
    return searchOptionsPanel.isContentVisible() ? optionsGroup.getSelected() : SearchOption.findWhole;
  }

  @SuppressWarnings("dereference.of.nullable") // controller is null when we call this, but not when we call the lambda.
  private Consumer<Collection<@NonNull R>> createRecordConsumer(@UnderInitialization RecordUI<R>this) {
    return records -> SwingUtilities.invokeLater(() -> controller.setFoundRecords(records));
  }

  @Override
  public void indexChanged(final int index, int prior) {
    loadInfoLine();
  }

  private void selectionChanged(@SuppressWarnings("unused") ButtonModel selectedButtonModel) { searchNow(); }
}
