package com.neptunedreams.skeleton.data.derby;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import com.neptunedreams.skeleton.data.ConnectionSource;
import com.neptunedreams.skeleton.data.DatabaseInfo;
import com.neptunedreams.skeleton.data.Record;
import com.neptunedreams.skeleton.data.sqlite.SQLiteInfo;
import com.neptunedreams.skeleton.data.sqlite.SQLiteRecordDao;
import com.neptunedreams.skeleton.data2.tables.records.RecordRecord;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

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
    DatabaseInfo info = new DerbyInfo("/.derby.skeletonTest");
//    ensureHomeExists(info.getHomeDir());
    info.init();
    final ConnectionSource connectionSource = info.getConnectionSource();
    DerbyRecordDao dao = (DerbyRecordDao) info.<Record, Integer>getDao(Record.class, connectionSource);
    dao.createTableIfNeeded();
//    SQLiteRecordDao dao = (SQLiteRecordDao) info.<RecordRecord, Integer>getDao(Record.class, connectionSource);
    Record record1 = new Record("TestSite", "testName", "testPw", "testNotes\nNote line 2\nNoteLine 3");
    dao.save(record1);
    Record record2 = new Record("t2Site", "t2User", "t2Pw", "t2Note");
    dao.save(record2);

    Collection<Record> allRecords = dao.getAll(com.neptunedreams.skeleton.data.RecordField.SOURCE);
    System.out.printf("getAll() returned %d records, expecting 2%n", allRecords.size());

    //noinspection UnusedAssignment
    Collection<Record> foundRecords = dao.getAll(com.neptunedreams.skeleton.data.RecordField.SOURCE);
    foundRecords = dao.find("line", com.neptunedreams.skeleton.data.RecordField.SOURCE);
    System.out.printf("find(line) returned %d records, expecting 1%n", foundRecords.size());
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

    Record record1b = new Record("TestSite a", "testName a", "testPw a", "testNotes\nNote line 2\nNoteLine 3");
    dao.insert(record1b);
    Collection<?> tInfo = dao.getTableInfo();
    System.out.printf("Total of %d objects%n", tInfo.size());
    for (Object o : tInfo) {
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
