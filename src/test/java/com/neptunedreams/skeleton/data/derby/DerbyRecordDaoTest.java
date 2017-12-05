package com.neptunedreams.skeleton.data.derby;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import com.neptunedreams.skeleton.data.ConnectionSource;
import com.neptunedreams.skeleton.data.DatabaseInfo;
import com.neptunedreams.skeleton.data.Record;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/24/17
 * <p>Time: 3:43 PM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings({"HardCodedStringLiteral", "StringConcatenation"})
public class DerbyRecordDaoTest {
  @Test
  @SuppressWarnings({"HardCodedStringLiteral", "unused", "HardcodedLineSeparator"})
  public void testDao() throws SQLException, IOException {
    //noinspection HardcodedFileSeparator
    DatabaseInfo info = new DerbyInfo("/.derby.skeletonTest");
    info.init();
    final ConnectionSource connectionSource = info.getConnectionSource();
    DerbyRecordDao dao = (DerbyRecordDao) info.<Record, Integer>getDao(Record.class, connectionSource);
    try {
      doTestDao(dao);
    } finally {
      // cleanup even on failure.
      Collection<Record> allRecords = showAllRecords(dao);
      int count = allRecords.size();
      for (Record record : allRecords) {
        dao.delete(record);
      }
      allRecords = showAllRecords(dao);
      assertEquals(0, allRecords.size());
    }
  }
  
  private void doTestDao(DerbyRecordDao dao) throws IOException, SQLException {
//    ensureHomeExists(info.getHomeDir());
    dao.createTableIfNeeded();
//    SQLiteRecordDao dao = (SQLiteRecordDao) info.<RecordRecord, Integer>getDao(Record.class, connectionSource);
    //noinspection HardcodedLineSeparator
    Record record1 = new Record("TestSite", "testName", "testPw", "testNotes\nNote line 2\nNoteLine 3");
    dao.insert(record1);
    Record record2 = new Record("t2Site", "t2User", "t2Pw", "t2Note");
    dao.insert(record2);

    Collection<Record> allRecords = dao.getAll(com.neptunedreams.skeleton.data.RecordField.SOURCE);
    System.out.printf("getAll() returned %d records, expecting 2%n", allRecords.size());

    //noinspection UnusedAssignment
    Collection<Record> foundRecords = dao.getAll(com.neptunedreams.skeleton.data.RecordField.SOURCE);
    foundRecords = dao.find("t2site", com.neptunedreams.skeleton.data.RecordField.SOURCE);
    System.out.printf("find(t2site) returned %d records, expecting 1%n", foundRecords.size());
    record1 = foundRecords.iterator().next();

    // Test update
    final String revisedName = "revisedName";
    String originalName = record1.getUserName();
    record1.setUserName(revisedName);
    System.out.printf("Changing the Name from %s to %s%n", originalName, revisedName);
    dao.save(record1);
    foundRecords = dao.find("line", com.neptunedreams.skeleton.data.RecordField.SOURCE);
    System.out.printf("Found %d records%n", foundRecords.size());
    Record revisedRecord = foundRecords.iterator().next();
    testShowRecord(revisedRecord);

    int deletedId = revisedRecord.getId();
    dao.delete(revisedRecord);
    allRecords = dao.getAll(com.neptunedreams.skeleton.data.RecordField.SOURCE);
    System.out.printf("Total of %d records after deleting id %d%n", allRecords.size(), deletedId);
    Record remainingRecord = allRecords.iterator().next();
    dao.delete(remainingRecord);
    allRecords = dao.getAll(com.neptunedreams.skeleton.data.RecordField.SOURCE);
    System.out.printf("Total of %d records after deleting 1%n", allRecords.size());

    //noinspection HardcodedLineSeparator
    Record record1b = new Record("TestSite a", "testName a", "testPw a", "testNotes\nNote line 2\nNoteLine 3");
    dao.insert(record1b);
    Collection<@NonNull ?> tInfo = dao.getTableInfo();
    System.out.printf("Total of %d objects%n", tInfo.size());
    for (@NonNull Object o : tInfo) {
      System.out.println(o);
    }
  }

  private void testShowRecord(final Record record) {
    System.out.printf("Record: %n  id: %d%n  sr: %s%n  un: %s%n  pw: %s%n  Nt: %s%n",
        record.getId(), record.getSource(), record.getUserName(),
        record.getPassword(), record.getNotes());
  }

  @Test
  public void DaoInfoTest() throws SQLException, IOException {
    DatabaseInfo info = new DerbyInfo();
//    ensureHomeExists(info.getHomeDir());
    assertTrue("Home Dir doesn't exist!:" + info.getHomeDir(), new File(info.getHomeDir()).exists());
  }

  private Collection<Record> showAllRecords(final DerbyRecordDao dao) throws SQLException {
    Collection<Record> allRecords = dao.getAll(com.neptunedreams.skeleton.data.RecordField.SOURCE);
    System.out.printf("getAll() returned %d records, expecting 2%n", allRecords.size());
    for (Record rr : allRecords) {
      System.out.println(rr);
    }
    return allRecords;
  }

//  @SuppressWarnings("HardCodedStringLiteral")
//  private void ensureHomeExists(String databaseHome) throws IOException {
////    System.setProperty(DERBY_SYSTEM_HOME, databaseHome);
////    final String databaseHome = System.getProperty("derby.system.home");
////    final String databaseHome = props.getProperty(DERBY_SYSTEM_HOME);
////    System.out.printf("databaseHome: %s%n", databaseHome);
//    @SuppressWarnings("HardcodedFileSeparator")
//    File dataDir = new File("/", databaseHome);
//    System.out.printf("DataDir: %s%n", dataDir.getAbsolutePath());
//    if (!dataDir.exists()) {
//      //noinspection BooleanVariableAlwaysNegated
//      boolean success = dataDir.mkdir();
//      if (!success) {
//        throw new IllegalStateException("Failed to create home directory");
//      }
//    }
//
////    String connectionUrl = String.format("jdbc:derby:%s:skeleton", dataDir.getAbsolutePath());
//  }

}
