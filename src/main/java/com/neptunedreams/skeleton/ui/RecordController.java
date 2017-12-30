package com.neptunedreams.skeleton.ui;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.function.Function;
import com.ErrorReport;
import com.neptunedreams.skeleton.data.Dao;
import com.neptunedreams.skeleton.data.RecordField;
import com.neptunedreams.skeleton.gen.tables.records.RecordRecord;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/29/17
 * <p>Time: 11:27 AM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr", "WeakerAccess", "HardCodedStringLiteral"})
public class RecordController<R, PK> implements RecordModelListener {
  private static final Integer ZERO = 0;
  // For DerbyRecordDao, E was Record.FIELD
//  private E order = Record.FIELD.SOURCE;
  private RecordField order;
  private final Dao<R, PK> dao;
  // TODO:  RecordController and RecordView have references to each other. Replace this with a listener system
  // todo   This shouldn't be too hard. There are very few calls made to the RecordView.
  private final RecordView<R> recordView;
  private final RecordModel<R> model;
  private boolean initializeComplete = false;

  @SuppressWarnings("argument.type.incompatible")
  public RecordController(
      Dao<R, PK> theDao, 
      RecordView<R> view, 
      RecordField initialOrder,
      Function<Void, R> recordConstructor
  ) {
    dao = theDao;
    recordView = view;
    model = new RecordModel<>(recordConstructor);
    model.addModelListener(this); // Type checker needs "this" to be initialized, so suppress the warning.
    order = initialOrder;
  }

  public RecordModel<R> getModel() {
    return model;
  }
  
  public Dao<R, PK> getDao() { return dao; }

  public void specifyOrder(RecordField theOrder) {
    order = theOrder;
  }

  public RecordField getOrder() {
    return order;
  }

  private void loadNewRecord(R record) {
//    Thread.dumpStack();
    R currentRecord = recordView.getCurrentRecord(); // Move this back to where the comment is
//    System.err.printf("Loading record with id %s while current id is %s%n (size=%d)",
//        dao.getPrimaryKey(record),
//        dao.getPrimaryKey(currentRecord),
//        model.getSize()
//    ); // NON-NLS
    
    // Don't save the existing record on the initial search.
    if (initializeComplete) {
//      R currentRecord = model.getRecordAt(prior);
      assert currentRecord != null;
      final PK primaryKey = dao.getPrimaryKey(currentRecord);
      assert Objects.equals(primaryKey, ((RecordRecord) currentRecord).getId()); // Debug only. Don't check in.
      if (recordView.recordHasChanged()) {
//        System.out.printf("  Record (id=%s) has changed. Saving data with insertOrUpdate()%n", primaryKey);
        try {
          recordView.loadUIData(currentRecord);
          dao.insertOrUpdate(currentRecord);
          model.incrementTotal();
        } catch (SQLException e) {
          ErrorReport.reportException("Insert", e);
        }
      }
    }
    recordView.setCurrentRecord(record);
    initializeComplete = true;
  }

  public void addBlankRecord() {
    // If the last record is already blank, just go to it
    final int lastIndex = model.getSize() - 1;
    R lastRecord = model.getRecordAt(lastIndex);
    final PK lastRecordKey = dao.getPrimaryKey(lastRecord);
    
    // If we are already showing an unchanged blank record...
    if ((model.getRecordIndex() == lastIndex) && ((lastRecordKey == null) || (lastRecordKey == ZERO)) && !recordView.recordHasChanged()) {
      // ... we don't bother to create a new one.
//      System.out.printf("Not creating blank record at index %d%n", lastIndex);
      loadNewRecord(lastRecord);
    } else {
//      System.out.println("Adding blank record");
      R emptyRecord = model.createNewEmptyRecord();
      model.append(emptyRecord);
      loadNewRecord(emptyRecord);
    }
  }

  public void setFoundRecords(final Collection<R> theFoundItems) {
    model.setNewList(theFoundItems);
    if (theFoundItems.isEmpty()) {
      addBlankRecord();
    } else {
      final R selectedRecord = model.getFoundRecord();
      assert selectedRecord != null;
      loadNewRecord(selectedRecord);
    }
  }

  public void findTextInField(String dirtyText, final RecordField field, SearchOption searchOption) {
    //noinspection TooBroadScope
    String text = dirtyText.trim();
    try {
      Collection<R> foundItems = findRecordsInField(text, field, searchOption);
      setFoundRecords(foundItems);
      model.goFirst();
    } catch (SQLException e) {
      ErrorReport.reportException(String.format("Find Text in Field %s with %s", field, searchOption), e);
    }
  }

  Collection<R> findRecordsInField(final String text, final RecordField field, SearchOption searchOption) throws SQLException {
    if (text.trim().isEmpty()) {
      return dao.getAll(getOrder());
    } else {
      switch (searchOption) {
        case findExact:
          return dao.findInField(text, field, getOrder());
        case findAll:
          return dao.findAllInField(field, getOrder(), parseText(text));
        case findAny:
          return dao.findAnyInField(field, getOrder(), parseText(text));
        default:
          throw new AssertionError(String.format("Unhandled case: %s", searchOption));
      }
    }
  }

  String[] parseText(@NonNull String text) {
    //noinspection EqualsReplaceableByObjectsCall
    assert text.trim().equals(text); // text should already be trimmed
    StringTokenizer tokenizer = new StringTokenizer(text, " ");
    String[] tokens = new String[tokenizer.countTokens()];
    int i = 0;
    while (tokenizer.hasMoreTokens()) {
      tokens[i++] = tokenizer.nextToken();
    }
    return tokens;
  }

  /**
   * Find text in any field of the database.
   * @param dirtyText The text to find, without cleaning or wildcards
   */
  public void findTextAnywhere(String dirtyText, SearchOption searchOption) {
    //noinspection TooBroadScope
    String text = dirtyText.trim();
    try {
      Collection<R> foundItems = findRecordsAnywhere(text, searchOption);
      setFoundRecords(foundItems);
      model.goFirst();
    } catch (SQLException e) {
      ErrorReport.reportException("Find Text anywhere", e);
    }
  }

  Collection<R> findRecordsAnywhere(final String text, SearchOption searchOption) throws SQLException {
    if (text.isEmpty()) {
      return dao.getAll(getOrder());
    } else {
      switch (searchOption) {
        case findExact:
          return dao.find(text, getOrder());
        case findAll:
          return dao.findAll(getOrder(), parseText(text));
        case findAny:
          return dao.findAny(getOrder(), parseText(text));
        default:
          throw new AssertionError(String.format("Unhandled case: %s", searchOption));  
      }
    }
  }

  @Override
  public void modelListChanged(final int newSize) {
    
  }

  @Override
  public void indexChanged(final int index, int prior) {
    loadNewRecord(model.getFoundRecord());
  }

  public void delete(final R selectedRecord) throws SQLException {
    dao.delete(selectedRecord);
    model.decrementTotal();
  }
}
