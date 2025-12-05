package com.neptunedreams.skeleton.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Properties;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import com.google.common.eventbus.Subscribe;
import com.neptunedreams.framework.ErrorReport;
import com.neptunedreams.framework.data.RecordModel;
import com.neptunedreams.framework.data.RecordModelListener;
import com.neptunedreams.framework.data.SearchOption;
import com.neptunedreams.framework.event.MasterEventBus;
import com.neptunedreams.framework.task.ParameterizedCallable;
import com.neptunedreams.framework.task.QueuedTask;
import com.neptunedreams.framework.ui.ButtonGroupListener;
import com.neptunedreams.framework.ui.ClearableTextField;
import com.neptunedreams.framework.ui.Constrainer;
import com.neptunedreams.framework.ui.EnumGroup;
import com.neptunedreams.framework.ui.HidingPanel;
import com.neptunedreams.framework.ui.RecordController;
import com.neptunedreams.framework.ui.SwipeDirection;
import com.neptunedreams.framework.ui.SwipeView;
import com.neptunedreams.framework.ui.TangoUtils;
import com.neptunedreams.skeleton.data.SiteField;
import com.neptunedreams.skeleton.gen.tables.records.SiteRecord;
import org.jetbrains.annotations.NotNull;

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
 * @author Miguel Muñoz
 */
@SuppressWarnings({"HardCodedStringLiteral", "ExtractMethodRecommender"})
public final class RecordUI<R extends @NotNull Object> extends JPanel implements RecordModelListener {

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
  private final JTextField findField = new JTextField(" ", 10);
  private final ClearableTextField clearableTextField = ClearableTextField.wrap(findField);
  private final RecordController<R, Integer, @NotNull SiteField> controller;
  private final EnumGroup<@NotNull SiteField> searchFieldGroup = new EnumGroup<>();
  private final @NotNull RecordModel<? extends R> recordModel;
  private final JButton prev = Resource.ARROW_LEFT_PNG.createButton(null);
  private final JButton next = Resource.ARROW_RIGHT_PNG.createButton(null);
  private final JButton first = Resource.ARROW_FIRST_PNG.createButton(null); // null, for now
  private final JButton last = Resource.ARROW_LAST_PNG.createButton(null); // null, for now
  private final JToggleButton edit;

  private final JLabel infoLine = new JLabel("");
  private final EnumGroup<@NotNull SearchOption> optionsGroup = new EnumGroup<>();
  private SwipeView<@NotNull RecordView<R>> swipeView=null;

  private final HidingPanel searchOptionsPanel = makeSearchOptionsPanel(optionsGroup);

  // recordConsumer is how the QueuedTask communicates with the application code.
  private final Consumer<Collection<@NotNull R>> recordConsumer = createRecordConsumer();
  private final @NotNull QueuedTask<@NotNull String, Collection<R>> queuedTask;
  private final LFSizeAdjuster sizeAdjuster = LFSizeAdjuster.instance;

  /**
   * Makes the Search-options panel, which holds a radio button for each of the three search modes. These are activated only
   * when the search term contains multiple words, since they all do the same thing with just a single word.
   * @param searchOptionsGroup The searchOptions Group, to which the radio buttons will all be added.
   * @return The search options panel
   */
  private HidingPanel makeSearchOptionsPanel(
      @SuppressWarnings("BoundedWildcard") EnumGroup<@NotNull SearchOption> searchOptionsGroup
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
    ButtonGroupListener selectionChanged = this::selectionChanged;
    searchOptionsGroup.addButtonGroupListener(selectionChanged); // Using a lambda is an error. This is a warning. 

    final HidingPanel hidingPanel = HidingPanel.create(optionsPanel);
    hidingPanel.setDisableInsteadOfHide(true);
    return hidingPanel;
  }

  public static <RR extends @NotNull Object> RecordUI<RR> makeRecordUI(
      @NotNull RecordModel<? extends RR> model,
      RecordView<RR> theView,
      @SuppressWarnings("BoundedWildcard") RecordController<RR, Integer, @NotNull SiteField> theController
  ) {
    final RecordUI<RR> recordUI = new RecordUI<>(model, theController, theView.getEditModel());
    final JLayer<RecordView<RR>> layer = recordUI.wrapInLayer(theView);
    recordUI.add(layer, BorderLayout.CENTER);
    recordUI.add(recordUI.createControlPanel(), BorderLayout.PAGE_START);
    recordUI.add(recordUI.createTrashPanel(), BorderLayout.PAGE_END);
    final @NotNull Color bgColor = recordUI.getBackground();
    recordUI.setBorder(new MatteBorder(4, 4, 4, 4, bgColor));
    model.addModelListener(recordUI); // argument.type.incompatible checker error suppressed
    recordUI.findField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(final DocumentEvent e) {
        recordUI.process(e);
      }

      @Override
      public void removeUpdate(final DocumentEvent e) {
        recordUI.process(e);
      }

      @Override
      public void changedUpdate(final DocumentEvent e) {
        recordUI.process(e);
      }

    });
    MasterEventBus.registerMasterEventHandler(recordUI);
    return recordUI;
  } 
  
  private RecordUI(
      @NotNull RecordModel<? extends R> model,
      @SuppressWarnings("BoundedWildcard") RecordController<R, Integer, @NotNull SiteField> theController,
      JToggleButton.ToggleButtonModel editModel
  ) {
    super(new BorderLayout());
    edit = makeEditButton(editModel);
    recordModel = model;
    controller = theController;
    queuedTask = new QueuedTask<>(DELAY, createCallable(), recordConsumer);
    queuedTask.launch();
  }
  
  private void setupActions(SwipeView<@NotNull RecordView<R>> swipeView) {
    swipeView.assignRestrictedRepeatingKeystrokeAction("Previous", KeyEvent.VK_LEFT, 0, () -> recordModel.goPrev(), SwipeDirection.SWIPE_RIGHT);
    swipeView.assignRestrictedRepeatingKeystrokeAction("Next", KeyEvent.VK_RIGHT, 0, () -> recordModel.goNext(), SwipeDirection.SWIPE_LEFT);

    // These don't work, with either Alt or meta masks. I don't know why, but they're not that important.
    swipeView.assignKeyStrokeAction("First Record", KeyEvent.VK_LEFT, InputEvent.ALT_DOWN_MASK,
        () -> recordModel.goFirst(), SwipeDirection.SWIPE_RIGHT);
    swipeView.assignKeyStrokeAction("Last Record", KeyEvent.VK_RIGHT, InputEvent.META_DOWN_MASK,
        () -> recordModel.goLast(), SwipeDirection.SWIPE_LEFT);
  }

  private JLayer<RecordView<R>> wrapInLayer(RecordView<R> recordView) {
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
      //noinspection CallToPrintStackTrace
      e1.printStackTrace();
    }
  }

  /**
   * Creates the control panel, which has the search field, the search radio buttons, the search options, and the main navigation panel.
   *
   * @return The control panel
   */
  private JPanel createControlPanel() {
    JPanel controlPanel = new JPanel(new BorderLayout());
    controlPanel.add(createSearchRadioPanel(), BorderLayout.LINE_END);
    controlPanel.add(createNavigationPanel(), BorderLayout.CENTER);
    return controlPanel;
  }

  /**
   * Creates the navigation panel, with the search field, search options, and the navigation buttons.
   * @return The navigation panel
   */
  private JPanel createNavigationPanel() {
    JPanel buttonPanel = new JPanel(new BorderLayout());
    buttonPanel.add(getSearchField(), BorderLayout.PAGE_START);
    buttonPanel.add(searchOptionsPanel, BorderLayout.CENTER);
    buttonPanel.add(getNavButtons(), BorderLayout.PAGE_END);
    return buttonPanel;
  }

  /**
   * Creates the trash panel, which has the info line, the java version, and the trash button.
   * @return The trash panel
   */
  private JPanel createTrashPanel() {
    JPanel trashPanel = new JPanel(new BorderLayout());
    trashPanel.add(getEndButtons(), BorderLayout.LINE_END);

    assert infoLine != null;
    trashPanel.add(infoLine, BorderLayout.LINE_START);
    
    trashPanel.add(makeJavaVersion(), BorderLayout.CENTER);
    return trashPanel;
  }
  
  private JPanel getEndButtons() {
    //noinspection MagicNumber
    JPanel endPanel = new JPanel(new GridLayout(1, 0, 16, 0));

    JButton fontSizeButton = Resource.TEXT_SIZE_PNG.createButton(e -> showTextSizeDialog());
    endPanel.add(fontSizeButton);

    JButton trashRecordButton = Resource.BIN_EMPTY_PNG.createButton((e) -> delete());
    endPanel.add(trashRecordButton);
    return endPanel;
  }

  private void showTextSizeDialog() {
    final JList<Object> fontList = sizeAdjuster.createFontSizeList();
    JLabel label = new JLabel("Choose your Font Size…");
    //noinspection MagicNumber
    Border border = BorderFactory.createMatteBorder(0, 0, 16, 0, label.getBackground());
    label.setBorder(border);
    
    int result = JOptionPane.showConfirmDialog(
        getRootPane(),
        makeDualPanel(label, fontList),
        "Font Size",
        JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.PLAIN_MESSAGE
    );
    
    if (result == JOptionPane.OK_OPTION) {
      sizeAdjuster.changeFontSize(fontList.getSelectedValue().toString());
    }
  }

  private JPanel makeDualPanel(JComponent topComponent, JComponent bottomComponent) {
    JPanel dualPanel = new JPanel(new BorderLayout());
    dualPanel.add(topComponent, BorderLayout.PAGE_START);
    dualPanel.add(bottomComponent, BorderLayout.PAGE_END);
    return dualPanel;
  }

  private JPanel makeJavaVersion() {
    Properties properties = new Properties();
    final String resSource = "/pom.properties";
    final InputStream inStream = getClass().getResourceAsStream(resSource);
    if (inStream == null) {
      throw new IllegalStateException(resSource);
    }
    try {
      properties.load(inStream);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    final String javaVersion = "Java version " + System.getProperty("java.version");
    final String appVersion = String.format("App: %s", properties.getProperty("revision"));
    final String tango = properties.getProperty("tango.version");

    @SuppressWarnings("MagicCharacter")
    final String tangoVersion = String.format("Tango: %s", tango.substring(0, tango.indexOf('-')));
    final JLabel label = new JLabel(String.format("%s   •   %s   •   %s", javaVersion, appVersion, tangoVersion));
    
    label.setHorizontalAlignment(SwingConstants.CENTER);
    final Font labelFont = label.getFont();
    int textSize = labelFont.getSize();
    //noinspection MagicNumber
    label.setFont(labelFont.deriveFont(0.75f * textSize));
    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.add(label, BorderLayout.PAGE_END);
    return centerPanel;
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

  private JPanel getNavButtons() {
    JPanel buttons = new JPanel(new GridBagLayout());
    final JButton importBtn = new JButton("Imp");
    Constrainer constrainer = new Constrainer();
    buttons.add(Resource.BULLET_ADD_PNG.createButton((e1) -> addBlankRecord(e1)), constrainer);
    buttons.add(Box.createHorizontalStrut(10), constrainer);
    buttons.add(first, constrainer);
    buttons.add(prev, constrainer);
    buttons.add(next, constrainer);
    buttons.add(last, constrainer);
    buttons.add(Box.createHorizontalStrut(10), constrainer);
    buttons.add(edit, constrainer);
//    buttons.add(importBtn);

    @NotNull SwipeView<@NotNull RecordView<R>> sView = swipeView;
    sView.assignMouseDownAction(prev, recordModel::goPrev, SwipeDirection.SWIPE_RIGHT);
    sView.assignMouseDownAction(next, recordModel::goNext, SwipeDirection.SWIPE_LEFT);
    first.addActionListener((e) -> sView.swipeRight(recordModel::goFirst));
    last.addActionListener((e)  -> sView.swipeLeft(recordModel::goLast));
    edit.setSelected(true); // lets me execute the listener immediately
    edit.addItemListener((e) -> sView.getLiveComponent().setTextEditable(edit.isSelected()));
    edit.setSelected(false); // executes because the state changes
    importBtn.addActionListener((e) -> doImport());
    JPanel flowPanel = new JPanel(new FlowLayout());
    flowPanel.add(buttons);
    setupActions(sView);

    return flowPanel;
  }

  private void addBlankRecord(ActionEvent event) {
//    System.out.printf("Modifiers: 0x%08X SD: 0x%04x, SM: 0x%04x%n", event.getModifiers(), KeyEvent.SHIFT_DOWN_MASK, KeyEvent.SHIFT_MASK); // NON-NLS
    //noinspection deprecation,IfStatementWithNegatedCondition
    if ((event.getModifiers() & KeyEvent.SHIFT_MASK) != 0) {
      doImport();
    } else {
      controller.addBlankRecord();
      MasterEventBus.postUserRequestedNewRecordEvent();
      loadInfoLine();
    }
  }

  private void doImport() {
//    ImportDialog importDialog = new ImportDialog((Window) getRootPane().getParent(), controller.getDao());
    @SuppressWarnings("unchecked")
    ImportDialog importDialog = ImportDialog.build(
        (Window) getRootPane().getParent(),
        (RecordController<SiteRecord, Integer, @NotNull SiteField>) controller,
        (RecordUI<@NotNull SiteField>) this
    );
    importDialog.setVisible(true);
  }

  private JPanel getSearchField() {
    JLabel findIcon = Resource.MAGNIFIER_16_PNG.createLabel();
    TangoUtils.installStandardCaret(findField);
    JPanel searchPanel = new JPanel(new BorderLayout());
    searchPanel.add(findIcon, BorderLayout.LINE_START);
    searchPanel.add(clearableTextField, BorderLayout.CENTER);
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
  private ParameterizedCallable<String, Collection<@NotNull R>> createCallable() {
    return new ParameterizedCallable<>(null) {
      // Originally, I didn't need to do this. Then I restructured the code to eliminate lots of other warnings, but
      // this warning had to come back. I can't make any sense out of the comment above, although I'm sure it was
      // clear to me at the time. But since this doesn't get called until after construction is complete, it's safe
      // to suppress this warning. I don't understand why I had to replace the implicit parameter, but it may be because
      // I had restore all the implicit parameters after I stopped suppressing the method.invocation.invalid and 
      // argument.type.incompatible warnings on the constructor.
      @Override
      public Collection<@NotNull R> call(String inputData) {
        return retrieveNow(inputData);
      }
    };
  }
  
  private Collection<@NotNull R> retrieveNow(String text) {
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

  private Consumer<Collection<@NotNull R>> createRecordConsumer() {
    return records -> SwingUtilities.invokeLater(() -> controller.setFoundRecords(records));
  }

  @Override
  public void indexChanged(final int index, int prior) {
    loadInfoLine();
  }

  private void selectionChanged(ButtonModel selectedButtonModel) { searchNow(); }
  
  private JToggleButton makeEditButton(JToggleButton.ToggleButtonModel model) {
    JToggleButton editButton = new JToggleButton(Resource.EDIT_PNG.getIcon());
    editButton.setRequestFocusEnabled(false);
    editButton.setModel(model);
    editButton.setToolTipText(Resource.EDIT_PNG.getText());
    return editButton;
  }
}
