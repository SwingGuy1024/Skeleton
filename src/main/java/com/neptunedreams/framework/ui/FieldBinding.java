package com.neptunedreams.framework.ui;

import java.awt.Component;
import java.util.Objects;
import java.util.function.Function;
import javax.swing.JLabel;
import javax.swing.text.JTextComponent;
import com.neptunedreams.Setter;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Bind an editor or display field to a property of a data model.
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 12/6/17
 * <p>Time: 12:03 AM
 *
 * @author Miguel Mu\u00f1oz
 */
public abstract class FieldBinding<R, T, C extends Component> {
  private Function<R, T> getter;
  private Setter<R, T> setter;
  private C editor;
  private final boolean isEditable;

  FieldBinding(Function<R, T> aGetter, Setter<R, T> aSetter, C aField, boolean editable) {
    getter = aGetter;
    setter = aSetter;
    editor = aField;
    isEditable = editable;
  }

  /**
   * Determines if the property's value in the editor has changed from the value in the data model.
   * @param record The dataModel record
   * @return true if the the cleaned editor value is different from the dataModel's value, false otherwise
   */
  public boolean propertyHasChanged(R record) {
    return !Objects.equals(getValue(record), readFieldValue());
  }

  /**
   * Retrieves the data field value from the dataModel and loads it into the editor
   * @param dataRecord The dataModel record.
   */
  public void prepareEditor(R dataRecord) {
    prepareEditor(getStringValue(getValue(dataRecord)));
  }

  /**
   * Gets the editor, which may just be a display field like a JLabel, or may be an editor component like a JTextField.
   * @return The editor component.
   */
  C getEditor() { return editor; }

  /**
   * Calls the getter to get the value from the dataModel record, without doing any cleaning. For internal use only.
   * @param record The record with the data
   * @return The value
   */
  @SuppressWarnings("WeakerAccess")
  protected T getTheValue(R record) { return getter.apply(record); }

  /**
   * Gets the value from the editor or display field, without doing any cleaning. Subclasses should implement this for 
   * their particular editor component.
   * @return The uncleaned value from the editor component.
   */
  protected abstract T getFieldValue();
  
  private T readFieldValue() {
    return clean(getFieldValue());
  }

  /**
   * Uses the getter to retrieve the value from the dataModel record, and cleans it
   * @param record The dataModel record.
   * @return The cleaned value from the dataModel
   */
  public T getValue(R record) {
    return clean(getTheValue(record));
  }

  /**
   * Uses the setter to set the specified value into the dataModel record, after cleaning it.
   * @param record The dataModel record
   * @param value The value to set
   */
  public void setValue(R record, T value) {
    assert record != null : "Null record";
    assert value != null : "Null value";
    setter.setValue(record, clean(value));
  }

  /**
   * Prepare the editor by setting the editor or display component to the specified value. This is delcared as a String
   * because that's how both text and numerical data is displayed, but this may be revisited later.
   * Subclasses should implement this for their particular editor component.
   * @param editorValue The editor value, expressed as a String.
   */
  protected abstract void prepareEditor(String editorValue);

  /**
   * Cleans the value. The default implementation returns the value unchanged, but subclasses should override this
   * to do whatever cleaning is needed for the particular type of data. Strings, for example, should call the trim()
   * method.
   * @param value The value to clean
   * @return The cleaned value
   */
  protected T clean(T value) { return value; }

  /**
   * Returns the value as a String. This is used for loading an editor that displays the value as a String. 
   * @param value The value, which may be null, to express as a String. Null values are expressed as an empty String.
   * @return The String that expresses the value.
   */
  String getStringValue(@Nullable T value) { return Objects.toString(value, ""); }

  /**
   * Reads the value in the editor, cleans it, and loads it into the current record.
   * @param record The record to receive the editor's value
   */
  public void saveEdit(R record) {
    setter.setValue(record, readFieldValue());
  }

  /**
   * Determines if the Binding uses an editable component. 
   * @return true if editable, false otherwise
   */
  public boolean isEditable() {
    return isEditable;
  }

  /**
   * A Binding to edit a String value, which uses a subclass of JTextComponent (Usually a JTextField or JTextArea)
   * to display the editable value.
   * @param <D> The DataModel type.
   */
  public static class StringEditableBinding<D> extends FieldBinding<D, String, JTextComponent> {
    StringEditableBinding(Function<D, String> aGetter, Setter<D, String> aSetter, JTextComponent aField) {
      super(aGetter, aSetter, aField, true);
    }

    @Override
    protected String getFieldValue() {
      return getEditor().getText();
    }

    @Override
    protected void prepareEditor(final String editorValue) {
      getEditor().setText(clean(editorValue));
    }

    @Override
    protected String clean(final String value) { return value.trim(); }
  }

  /**
   * A binding to display a read-only integer value, which uses a JLabel to display the String value.
   * @param <D> The DataModel type
   */
  public static class IntegerBinding<D> extends FieldBinding<D, Integer, JLabel> {
    private Integer loadedValue = 0;
    IntegerBinding(final Function<D, Integer> aGetter, final Setter<D, Integer> aSetter, final JLabel aField) {
      super(aGetter, aSetter, aField, false);
    }

    @Override
    protected void prepareEditor(final String editorValue) {
      getEditor().setText(editorValue);
    }

    @Override
    protected Integer getFieldValue() {
//      return Integer.valueOf(getEditor().getText());
      return loadedValue;
    }

    @Override
    public void prepareEditor(final D dataRecord) {
      loadedValue = getTheValue(dataRecord);
      prepareEditor(getStringValue(loadedValue));
    }
  }
  
  public static <R> StringEditableBinding<R> bindEditableString(Function<R, String> getter, Setter<R, String> setter, JTextComponent field) {
    return new StringEditableBinding<>(getter, setter, field);
  }

  public static <R> IntegerBinding<R> bindInteger(Function<R, Integer> getter, Setter<R, Integer> setter, JLabel field) {
    return new IntegerBinding<>(getter, setter, field);
  }
}
