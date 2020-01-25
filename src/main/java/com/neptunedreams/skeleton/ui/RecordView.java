package com.neptunedreams.skeleton.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.MatteBorder;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import com.google.common.eventbus.Subscribe;
import com.neptunedreams.framework.data.Dao;
import com.neptunedreams.framework.data.RecordSelectionModel;
import com.neptunedreams.framework.event.ChangeRecord;
import com.neptunedreams.framework.event.MasterEventBus;
import com.neptunedreams.framework.ui.FieldBinding;
import com.neptunedreams.skeleton.data.SiteField;
//import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.initialization.qual.NotOnlyInitialized;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.RequiresNonNull;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/29/17
 * <p>Time: 10:54 AM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings({"WeakerAccess", "HardCodedStringLiteral"})
public final class RecordView<R> extends JPanel implements RecordSelectionModel<R> {
  private static final int TEXT_COLUMNS = 40;
  private static final int TEXT_ROWS = 6;
  private JPanel labelPanel = new JPanel(new GridLayout(0, 1));
  private JPanel fieldPanel = new JPanel(new GridLayout(0, 1));
  private JPanel checkBoxPanel = new JPanel(new GridLayout(0, 1));
  private ButtonGroup buttonGroup = new ButtonGroup();

  @SuppressWarnings("HardCodedStringLiteral")
  private R currentRecord; // = new Record("D", "D", "D", "D");
  
  @NotOnlyInitialized
  private RecordController<R, Integer, SiteField> controller;
  private final List<? extends FieldBinding<R, ? extends Serializable, ? extends JComponent>> allBindings;
  private final JTextComponent sourceField;

  @SuppressWarnings({"initialization.fields.uninitialized", "argument.type.incompatible", "method.invocation.invalid"})
  private RecordView(R record,
                     SiteField initialSort,
                     Dao<R, Integer, SiteField> dao,
                     Supplier<@NonNull R> recordConstructor,
                     Function<R, Integer> getIdFunction, BiConsumer<R, Integer> setIdFunction,
                     Function<R, String> getSourceFunction, BiConsumer<R, String> setSourceFunction,
                     Function<R, String> getUserNameFunction, BiConsumer<R, String> setUserNameFunction,
                     Function<R, String> getPasswordFunction, BiConsumer<R, String> setPasswordFunction,
                     Function<R, String> getNotesFunction, BiConsumer<R, String> setNotesFunction
   ) {
    super(new BorderLayout());
    currentRecord = record;
    controller = makeController(initialSort, dao, recordConstructor);
    final JLabel idField = (JLabel) addField("ID", false, SiteField.ID, initialSort);
    sourceField = (JTextComponent) addField("Source", true, SiteField.Source, initialSort);
    final JTextComponent usernameField = (JTextComponent) addField("User Name", true, SiteField.Username, initialSort);
    final JTextComponent pwField = (JTextComponent) addField("Password", true, SiteField.Password, initialSort);
    final JTextComponent notesField = addNotesField();
    assert getIdFunction != null : "Null id getter";
    assert setIdFunction != null : "Null id Setter";
    final FieldBinding.IntegerBinding<R> idBinding = FieldBinding.bindInteger(getIdFunction, setIdFunction, idField);
    final FieldBinding.StringEditableBinding<R> sourceBinding = FieldBinding.bindEditableString(getSourceFunction, setSourceFunction, sourceField);
    final FieldBinding.StringEditableBinding<R> userNameBinding = FieldBinding.bindEditableString(getUserNameFunction, setUserNameFunction, usernameField);
    final FieldBinding.StringEditableBinding<R> passwordBinding = FieldBinding.bindEditableString(getPasswordFunction, setPasswordFunction, pwField);
    final FieldBinding.StringEditableBinding<R> notesBinding = FieldBinding.bindEditableString(getNotesFunction, setNotesFunction, notesField);
    allBindings = Arrays.asList(idBinding, sourceBinding, userNameBinding, passwordBinding, notesBinding);
    
    // currentRecord has null values for lots of non-null fields. This should clean those fields up.
    for (FieldBinding<R, ?, ?> b : allBindings) {
      cleanValue(b, record); // somehow, Nullness Checker doesn't recognize currentRecord as non-null, but record is ok
    }
    idBinding.setValue(record, 0); // same here. TODO: Ask StackOverflow about this

    makeTopPanel();

    installStandardCaret(sourceField);
    installStandardCaret(usernameField);
    installStandardCaret(pwField);
    installStandardCaret(notesField);
    
    // Put a line at the top, one pixel wide.

  }

  @SuppressWarnings("method.invocation.invalid")
  private RecordController<R, Integer, SiteField> makeController(final SiteField initialSort, final Dao<R, Integer, SiteField> dao, final Supplier<@NonNull R> recordConstructor) {
    return new RecordController<>(
        dao,
        this,
        initialSort,
        recordConstructor
    );
  }

  private void register() {
    MasterEventBus.registerMasterEventHandler(this);
  }

  /**
   * Clean the value during initialization. This needs to be a separate method because there's no way to infer the 
   * type of T if I put this code in the original loop. Without T, there's no way for the compiler to know that the 
   * value returned by binding.getValue() is the same type as the one we need to pass to setValue().
   * @param binding The FieldBinding
   * @param record The record to clean
   * @param <T> The type of the record.
   */
  private <T> void cleanValue(@UnderInitialization RecordView<R> this, FieldBinding<? super R, T, ?> binding, R record) {
    binding.setValue(record, binding.getValue(record));
  }

  @SuppressWarnings("method.invocation.invalid")
  @RequiresNonNull({"labelPanel", "fieldPanel", "checkBoxPanel"})
  private void makeTopPanel(@UnderInitialization RecordView<R> this) {
    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.add(labelPanel, BorderLayout.LINE_START);
    topPanel.add(fieldPanel, BorderLayout.CENTER);
    topPanel.add(checkBoxPanel, BorderLayout.LINE_END);
    add(topPanel, BorderLayout.PAGE_START);
//    return topPanel;
    setBorder(new MatteBorder(1, 0, 0, 0, Color.black));
  }

//  @SuppressWarnings("HardCodedStringLiteral")
//  public void setController(RecordController<R, Integer> theController) {
//    controller = theController;
//  }
//  
  public RecordController<R, Integer, SiteField> getController() { return controller; }

  /**
   * On the Mac, the AquaCaret will get installed. This caret has an annoying feature of selecting all the text on a
   * focus-gained event. If this isn't bad enough, it also fails to check temporary vs permanent focus gain, so it 
   * gets triggered on a focused JTextComponent whenever a menu is released! This method removes the Aqua Caret and 
   * installs a standard caret. It's only needed on the Mac, but it's safe to use on any platform.
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

  @RequiresNonNull({"labelPanel", "fieldPanel", "buttonGroup", "checkBoxPanel", "controller"})
  private JComponent addField(@UnderInitialization RecordView<R> this, final String labelText, final boolean editable, final SiteField orderField, SiteField initialSort) {
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
    @SuppressWarnings("method.invocation.invalid") // We are under initialization when we create this, not when calling
    ItemListener checkBoxListener = (itemEvent) -> itemStateChanged(orderField, itemEvent);
    orderBy.addItemListener(checkBoxListener);
    return field;
  }

  private void itemStateChanged(final SiteField orderField, final ItemEvent itemEvent) {
    if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
      controller.specifyOrder(orderField);
      // Here's where I want to call RecordUI.searchNow(). The code needs to be restructured before I can do that.
      MasterEventBus.postSearchNowEvent();
    }
  }

  @SuppressWarnings("method.invocation.invalid")
  private JTextComponent addNotesField(@UnderInitialization RecordView<R> this) {
    final JTextArea wrappedField = new JTextArea(TEXT_ROWS, TEXT_COLUMNS);
    wrappedField.setWrapStyleWord(true);
    wrappedField.setLineWrap(true);
    JComponent scrollPane = wrap(wrappedField);
    add(BorderLayout.CENTER, scrollPane);
    return wrappedField;
  }

  private JComponent wrap(@UnderInitialization RecordView<R> this, JComponent wrapped) {
    return new JScrollPane(wrapped, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
  }
  
  @Subscribe
  public void setCurrentRecord(ChangeRecord<? extends R> recordEvent) {
    R newRecord = recordEvent.getNewRecord();
    assert newRecord != null;
    currentRecord = newRecord;
    for (FieldBinding<R, ?, ?> binding: allBindings) {
      binding.prepareEditor(newRecord);
    }
  }
  
  @Override
  public boolean recordHasChanged() {
    for (FieldBinding<R, ?, ?> binding: allBindings) {
      if (binding.isEditable() && binding.propertyHasChanged(currentRecord)) {
        return true;
      }
    }
    return false;
  }
  
  public boolean saveOnExit() {// throws SQLException {
    // Test four cases:
    // 1. New Record with data
    // 2. New Record with no data
    // 3. Existing record with changes
    // 4. Existing record with no changes
    final boolean hasChanged = recordHasChanged();
    if (hasChanged) {
      loadUserEdits();
      return true;
    }
    return false;
  }

  @Override
  public R getCurrentRecord() {
    return currentRecord;
  }

  /**
   * Reads the data from the editor fields and loads them into the current record's model. This gets called by
   * the event bus in response to a LoadUIEvent.
   * @param event Unused. This used to be a Record, but we didn't need it.
   */
  @Subscribe
  public void loadUserEdits(MasterEventBus.LoadUIEvent event) {
    loadUserEdits();
  }

  private void loadUserEdits() {
    for (FieldBinding<R, ?, ?> binding : allBindings) {
      if (binding.isEditable()) {
        binding.saveEdit(currentRecord);
      }
    }
  }

  @Subscribe void userRequestedNewRecord(MasterEventBus.UserRequestedNewRecordEvent event) {
    sourceField.requestFocus();
  }

    @SuppressWarnings({"initialization.fields.uninitialized", "InstanceVariableMayNotBeInitialized"})
    public static class Builder<RR> {
      private RR record;
      private SiteField initialSort;
      private Function<RR, Integer> getId;
      private BiConsumer<RR, Integer> setId;
      private Function<RR, String> getSource;
      private BiConsumer<RR, String> setSource;
      private Function<RR, String> getUserName;
      private BiConsumer<RR, String> setUserName;
      private Function<RR, String> getPassword;
      private BiConsumer<RR, String> setPassword;
      private Function<RR, String> getNotes;
      private BiConsumer<RR, String> setNotes;
      private Dao<RR, Integer, SiteField> dao;
      private Supplier<@NonNull RR> recordConstructor;
      public Builder(RR record, SiteField initialSort) {
        this.record = record;
        this.initialSort = initialSort;
      }
  
    public Builder<RR> id(Function<RR, Integer> getter, BiConsumer<RR, Integer> setter) {
      getId = getter;
      setId = setter;
      return this;
    }
  
    public Builder<RR> source(Function<RR, String> getter, BiConsumer<RR, String> setter) {
      getSource = getter;
      setSource = setter;
      return this;
    }
  
    public Builder<RR> userName(Function<RR, String> getter, BiConsumer<RR, String> setter) {
      getUserName = getter;
      setUserName = setter;
      return this;
    }
  
    public Builder<RR> password(Function<RR, String> getter, BiConsumer<RR, String> setter) {
      getPassword = getter;
      setPassword = setter;
      return this;
    }
  
    public Builder<RR> notes(Function<RR, String> getter, BiConsumer<RR, String> setter) {
      getNotes = getter;
      setNotes = setter;
      return this;
    }
    
    public Builder<RR> withDao(Dao<RR, Integer, SiteField> dao) {
        this.dao = dao;
        return this;
    }
    
    public Builder<RR> withConstructor(Supplier<@NonNull RR> constructor) {
        recordConstructor = constructor;
        return this;
    }
    
    public RecordView<RR> build() {
      final RecordView<RR> view = new RecordView<>(
          record,
          initialSort,
          dao,
          recordConstructor,
          getId, setId,
          getSource, setSource,
          getUserName, setUserName,
          getPassword, setPassword,
          getNotes, setNotes
      );
      view.register();
      return view;
    }
  }
}
