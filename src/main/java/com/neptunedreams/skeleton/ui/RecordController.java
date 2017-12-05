package com.neptunedreams.skeleton.ui;

import java.sql.SQLException;
import java.util.Collection;
import com.ErrorReport;
import com.neptunedreams.skeleton.data.Dao;
import com.neptunedreams.skeleton.data.RecordField;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/29/17
 * <p>Time: 11:27 AM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr", "WeakerAccess", "HardCodedStringLiteral"})
public class RecordController<R, PK> implements RecordModelListener {
  // For DerbyRecordDao, E was Record.FIELD
//  private E order = Record.FIELD.SOURCE;
  private RecordField order;
  private final Dao<R, PK> dao;
  // TODO: RecordController and RecordView have references to each other. Replace this with a listener system.
  private final RecordView<R> recordView;
  private final RecordModel<R> model;

  @SuppressWarnings("argument.type.incompatible")
  public RecordController(
      Class<R> recordClass, 
      Dao<R, PK> theDao, 
      RecordView<R> view, 
      RecordField initialOrder
  ) {
    dao = theDao;
    recordView = view;
    model = new RecordModel<>(recordClass);
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

  public void loadNewRecord(R record, int prior) {
    if ((prior >= 0) && (prior < model.getSize())) {
      R currentRecord = model.getRecordAt(prior);
      if (recordView.recordHasChanged()) {
        try {
          recordView.loadNewData(currentRecord);
          dao.setPrimaryKey(currentRecord, dao.getNextId());
//          currentRecord.setId(dao.getNextId());
          dao.insert(currentRecord);
          model.incrementTotal();
        } catch (SQLException e) {
          ErrorReport.reportException("Insert", e);
        }
      } else if (dao.getPrimaryKey(currentRecord) == null) {
        // remove currentRecord. It hasn't changed so it's empty.
        model.deleteSelected(false, prior);
      }
    }
    recordView.setCurrentRecord(record); // Should this be done by a listener in the view? If so, how do we make sure
    // it happens at the end of this method?
  }

  public void addBlankRecord() {
    R emptyRecord = model.createNewEmptyRecord();
    model.append(emptyRecord);
    loadNewRecord(emptyRecord, model.getRecordIndex()+1);
  }

  public void setFoundRecords(final Collection<R> theFoundItems) {
    model.setNewList(theFoundItems);
//    model.goFirst();
    if (theFoundItems.isEmpty()) {
      addBlankRecord();
    } else {
      final R selectedRecord = model.getSelectedRecord();
      assert selectedRecord != null;
      loadNewRecord(selectedRecord, -1);
    }
  }

  public void findTextInField(String dirtyText, final RecordField field) {
    //noinspection TooBroadScope
    String text = dirtyText.trim();
    try {
      Collection<R> foundItems = findRecordsInField(text, field);
      setFoundRecords(foundItems);
      model.goFirst();
    } catch (SQLException e) {
      //noinspection StringConcatenation
      ErrorReport.reportException("Find Text in Field " + field, e);
    }
  }

  Collection<R> findRecordsInField(final String text, final RecordField field) throws SQLException {
    if (text.trim().isEmpty()) {
      return dao.getAll(getOrder());
    } else {
      return dao.findInField(text, field, getOrder());
    }
  }

  public void findTextAnywhere(String dirtyText) {
    //noinspection TooBroadScope
    String text = dirtyText.trim();
    try {
      Collection<R> foundItems = findRecordsAnywhere(text);
      setFoundRecords(foundItems);
      model.goFirst();
    } catch (SQLException e) {
      ErrorReport.reportException("Find Text anywhere", e);
    }
  }

  Collection<R> findRecordsAnywhere(final String text) throws SQLException {
    Collection<R> foundItems;
    if (text.isEmpty()) {
      foundItems = dao.getAll(getOrder());
    } else {
      foundItems = dao.find(text, getOrder());
      System.out.printf("Found %d items.%n", foundItems.size());
    }
    return foundItems;
  }

  @Override
  public void modelListChanged(final int newSize) {
    
  }

  @Override
  public void indexChanged(final int index, int prior) {
    loadNewRecord(model.getSelectedRecord(), prior);
  }

  public void delete(final R selectedRecord) throws SQLException {
    dao.delete(selectedRecord);
    model.decrementTotal();
  }
}
