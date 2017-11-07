package com.neptunedreams.skeleton.ui;

import java.sql.SQLException;
import java.util.Collection;
import com.ErrorReport;
import com.neptunedreams.skeleton.data.Dao;
import com.neptunedreams.skeleton.data.Record;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/29/17
 * <p>Time: 11:27 AM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr", "WeakerAccess", "HardCodedStringLiteral"})
public class RecordController implements RecordModelListener {
  private Record.FIELD order = Record.FIELD.SOURCE;
  private final Dao<Record> dao;
  private final RecordView recordView;
  private final RecordModel model = new RecordModel();

  public RecordController(Dao<Record> theDao, RecordView view) {
    dao = theDao;
    recordView = view;
    model.addModelListener(this);
  }

  public RecordModel getModel() {
    return model;
  }
  
  public Dao<Record> getDao() { return dao; }

  public void specifyOrder(Record.FIELD theOrder) {
    order = theOrder;
  }

  public Record.FIELD getOrder() {
    return order;
  }

  public void loadNewRecord(Record record, int prior) {
    if ((prior >= 0) && (prior < model.getSize())) {
      Record currentRecord = model.getRecordAt(prior);
      if (recordView.recordHasChanged()) {
        try {
          recordView.loadNewData(currentRecord);
          currentRecord.setId(dao.getNextId());
          dao.insert(currentRecord);
          model.incrementTotal();
        } catch (SQLException e) {
          ErrorReport.reportException("Insert", e);
        }
      } else if (currentRecord.getId() == 0) {
        // remove currentRecord. It hasn't changed so it's empty.
        model.deleteSelected(false, prior);
      }
    }
    recordView.setCurrentRecord(record); // Should this be done by a listener in the view? If so, how do we make sure
    // it happens at the end of this method?
  }

  public void addBlankRecord() {
    Record emptyRecord = new Record();
    model.append(emptyRecord);
    loadNewRecord(emptyRecord, model.getRecordIndex()+1);
  }

  public void setFoundRecords(final Collection<Record> theFoundItems) {
    model.setNewList(theFoundItems);
//    model.goFirst();
    if (theFoundItems.isEmpty()) {
      addBlankRecord();
    } else {
      final Record selectedRecord = model.getSelectedRecord();
      assert selectedRecord != null;
      loadNewRecord(selectedRecord, -1);
    }
  }

  public void findTextInField(String dirtyText, final Record.FIELD field) {
    //noinspection TooBroadScope
    String text = dirtyText.trim();
    try {
      Collection<Record> foundItems = findRecordsInField(text, field);
      setFoundRecords(foundItems);
      model.goFirst();
    } catch (SQLException e) {
      //noinspection StringConcatenation
      ErrorReport.reportException("Find Text in Field " + field, e);
    }
  }

  Collection<Record> findRecordsInField(final String text, final Record.FIELD field) throws SQLException {
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
      Collection<Record> foundItems = findRecordsAnywhere(text);
      setFoundRecords(foundItems);
      model.goFirst();
    } catch (SQLException e) {
      ErrorReport.reportException("Find Text anywhere", e);
    }
  }

  Collection<Record> findRecordsAnywhere(final String text) throws SQLException {
    Collection<Record> foundItems;
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

  public void delete(final Record selectedRecord) throws SQLException {
    dao.delete(selectedRecord);
    model.decrementTotal();
  }
}
