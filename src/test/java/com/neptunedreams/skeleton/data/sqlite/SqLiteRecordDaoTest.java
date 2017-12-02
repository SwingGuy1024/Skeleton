package com.neptunedreams.skeleton.data.sqlite;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import com.neptunedreams.skeleton.data.ConnectionSource;
import com.neptunedreams.skeleton.data.DatabaseInfo;
//import com.neptunedreams.skeleton.data.Record;
import com.neptunedreams.skeleton.data2.tables.records.RecordRecord;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/27/17
 * <p>Time: 5:42 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public class SqLiteRecordDaoTest {
  @Test
  @SuppressWarnings({"HardCodedStringLiteral", "unused", "HardcodedLineSeparator"})
  public void testDao() throws SQLException, IOException {
    //noinspection HardcodedFileSeparator
    DatabaseInfo info = new SQLiteInfo("/.sqlite.skeletonTest");
    info.init();
    final ConnectionSource connectionSource = info.getConnectionSource();
    SQLiteRecordDao dao = (SQLiteRecordDao) info.<RecordRecord, Integer>getDao(RecordRecord.class, connectionSource);
    try {
      doTestDao(dao, connectionSource);
    } finally {
      
      // cleanup even on failure.
      Collection<RecordRecord> allRecords = showAllRecords(dao);
      int count = allRecords.size();
      for (RecordRecord recordRecord : allRecords) {
        dao.delete(recordRecord);
      }
      allRecords = showAllRecords(dao);
      assertEquals(0, allRecords.size());
    }
  }
  
  private void doTestDao(SQLiteRecordDao dao, ConnectionSource connectionSource) throws IOException, SQLException {
    assertNotNull(connectionSource);
//    ensureHomeExists(info.getHomeDir());
    assertNotNull(dao);
    // todo: fix CreateTableIfNeeded when the whole schema was created.
    dao.createTableIfNeeded();
//    if (info.isCreateSchemaAllowed()) {
//      info.createSchema();
//    }
    RecordRecord record1 = new RecordRecord(0,"TestSiteAlpha", "testName", "testPw", "testNotes\nNote line 2\nNoteLine 3");
//    RecordRecord record1 = createRecord("TestSiteAlpha", "testName", "testPw", "testNotes\nNote line 2\nNoteLine 3");
    assertFalse(connectionSource.getConnection().isClosed());

    Collection<RecordRecord> allRecords = showAllRecords(dao);
    assertEquals(0, allRecords.size());

    dao.insert(record1);
    Integer r1Id = record1.getId();
    allRecords = showAllRecords(dao);
    assertEquals(1, allRecords.size());
    assertEquals(allRecords.iterator().next().getId(), r1Id);

    RecordRecord record2 = new RecordRecord(0, "t2Site", "t2User", "t2Pw", "t2Note");
//    RecordRecord record2 = createRecord("t2Site", "t2User", "t2Pw", "t2Note");
    dao.insert(record2);
    Integer r2Id = record2.getId();
    assertNotEquals(r1Id, r2Id);
    allRecords = showAllRecords(dao);
    assertEquals(2, allRecords.size());
//    Set<Integer> set1 = new HashSet<>();for (RecordRecord rr: allRecords) set1.add(rr.getId());
    Set<Integer> set2 = allRecords
        .stream()
        .map(RecordRecord::getId)
        .collect(Collectors.toSet());
    assertThat(set2, Matchers.hasItems(r1Id, r2Id));

    allRecords = dao.getAll(com.neptunedreams.skeleton.data.RecordField.SOURCE);
    System.out.printf("getAll() returned %d records, expecting 2%n", allRecords.size());
    assertEquals(2, allRecords.size());
    showAllRecords(dao);

    Collection<RecordRecord> foundRecords = dao.find("alpha", com.neptunedreams.skeleton.data.RecordField.SOURCE);
    System.out.printf("find(alpha) returned %d records, expecting 1%n", foundRecords.size());
    assertEquals(1, foundRecords.size()); // I expect this to fail
    record1 = foundRecords.iterator().next();

    // Test update
    final String revisedName = "revisedName";
    String originalName = record1.getUsername();
    record1.setUsername(revisedName);
    System.out.printf("Changing the Name from %s to %s%n", originalName, revisedName);
    dao.save(record1);
    foundRecords = dao.find("alpha", com.neptunedreams.skeleton.data.RecordField.SOURCE);
    System.out.printf("Found %d records%n", foundRecords.size());
    assertEquals(1, foundRecords.size());
    RecordRecord revisedRecord = foundRecords.iterator().next();
    assertEquals("revisedName", revisedRecord.getUsername());
    testShowRecord(revisedRecord);

    int deletedId = revisedRecord.getId();
    dao.delete(revisedRecord);
    allRecords = dao.getAll(com.neptunedreams.skeleton.data.RecordField.SOURCE);
    System.out.printf("Total of %d records after deleting id %d%n", allRecords.size(), deletedId);
    assertEquals(1, allRecords.size());
    RecordRecord remainingRecord = allRecords.iterator().next();
    dao.delete(remainingRecord);
    allRecords = dao.getAll(com.neptunedreams.skeleton.data.RecordField.SOURCE);
    assertEquals(0, allRecords.size());
    System.out.printf("Total of %d records after deleting 1%n", allRecords.size());

//    RecordRecord record1b = new RecordRecord(1, "TestSiteAlpha", "testName", "testPw", "testNotes\nNote line 2\nNoteLine 3");
//    dao.insert(record1b);
//    allRecords = dao.getAll(com.neptunedreams.skeleton.data.RecordField.SOURCE);
//    assertEquals(0, allRecords.size());
//    System.out.printf("Total of %d records after lastInsert 1%n", allRecords.size());

  }

  private Collection<RecordRecord> showAllRecords(final SQLiteRecordDao dao) throws SQLException {
    Collection<RecordRecord> allRecords = dao.getAll(com.neptunedreams.skeleton.data.RecordField.SOURCE);
    System.out.printf("getAll() returned %d records, expecting 2%n", allRecords.size());
    for (RecordRecord rr: allRecords) {
      System.out.println(rr);
    }
    return allRecords;
  }

  private RecordRecord createRecord(String source, String userName, String password, String note) {
    RecordRecord record = new RecordRecord();
    record.setUsername(userName);
    record.setSource(source);
    record.setPassword(password);
    record.setNotes(note);
    return record;
  }

  private void testShowRecord(final RecordRecord record) {
    System.out.printf("Record: %n  id: %d%n  sr: %s%n  un: %s%n  pw: %s%n  Nt: %s%n",
        record.getId(), record.getSource(), record.getUsername(),
        record.getPassword(), record.getNotes());
  }
}
