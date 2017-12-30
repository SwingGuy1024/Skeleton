package com.neptunedreams.skeleton.data.sqlite;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.neptunedreams.skeleton.data.ConnectionSource;
import com.neptunedreams.skeleton.data.DatabaseInfo;
import com.neptunedreams.skeleton.data.RecordField;
import com.neptunedreams.skeleton.gen.tables.records.RecordRecord;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.*;

//import com.neptunedreams.skeleton.data.Record;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/27/17
 * <p>Time: 5:42 PM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings({"HardCodedStringLiteral", "HardcodedLineSeparator", "MagicNumber"})
public class SqLiteRecordDaoTest {

  private static @MonotonicNonNull ConnectionSource connectionSource;
  private static @MonotonicNonNull SQLiteRecordDao dao;

  @BeforeClass
  public static void setup() throws SQLException, IOException {
    System.err.printf("Before%n%n"); // NON-NLS
    //noinspection HardcodedFileSeparator
    final DatabaseInfo info = new SQLiteInfo("/.sqlite.skeletonTest");
    info.init();
    connectionSource = info.getConnectionSource();
    dao = (SQLiteRecordDao) info.<RecordRecord, Integer>getDao(RecordRecord.class, connectionSource);
  }
  
  @After
  public void tearDown() throws SQLException {
    System.err.printf("After%n%n"); // NON-NLS
    assert dao != null;
    Collection<RecordRecord> results = dao.findAll(null);
    for (RecordRecord recordRecord : results) {
      recordRecord.delete();
    }
  }

  private void doTestDao(SQLiteRecordDao dao, ConnectionSource connectionSource) throws IOException, SQLException {
    assertNotNull(connectionSource);
//    ensureHomeExists(info.getHomeDir());
    assertNotNull(dao);
    // todo: fix CreateTableIfNeeded when the whole schema was created.
//    dao.createTableIfNeeded();
//    if (info.isCreateSchemaAllowed()) {
//      info.createSchema();
//    }
    RecordRecord record1 = new RecordRecord(0,"TestSiteAlpha", "testName", "testPw", "testNotes\nNote line 2\nNoteLine 3");
//    RecordRecord record1 = createRecord("TestSiteAlpha", "testName", "testPw", "testNotes\nNote line 2\nNoteLine 3");
    //noinspection resource
    assertFalse(connectionSource.getConnection().isClosed());

    Collection<RecordRecord> allRecords = showAllRecords(dao, 0);
    assertEquals(0, allRecords.size());

    dao.insert(record1);
    Integer r1Id = record1.getId();
    allRecords = showAllRecords(dao, 1);
    assertEquals(1, allRecords.size());
    assertEquals(allRecords.iterator().next().getId(), r1Id);

    RecordRecord record2 = new RecordRecord(0, "t2Site", "t2User", "t2Pw", "t2Note");
//    RecordRecord record2 = createRecord("t2Site", "t2User", "t2Pw", "t2Note");
    dao.insert(record2);
    Integer r2Id = record2.getId();
    assertNotEquals(r1Id, r2Id);
    allRecords = showAllRecords(dao, 2);
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
    showAllRecords(dao, 2);

    Collection<RecordRecord> foundRecords = dao.find("alpha", com.neptunedreams.skeleton.data.RecordField.SOURCE);
    System.out.printf("find(alpha) returned %d records, expecting 1%n", foundRecords.size());
    assertEquals(1, foundRecords.size()); // I expect this to fail
    record1 = foundRecords.iterator().next();

    // Test update
    final String revisedName = "revisedName";
    String originalName = record1.getUsername();
    record1.setUsername(revisedName);
    System.out.printf("Changing the Name from %s to %s%n", originalName, revisedName);
    dao.update(record1);
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

  private Collection<RecordRecord> showAllRecords(final SQLiteRecordDao dao, int expectedCount) throws SQLException {
    Collection<RecordRecord> allRecords = dao.getAll(com.neptunedreams.skeleton.data.RecordField.SOURCE);
    System.out.printf("getAll() returned %d records, expecting %d%n", allRecords.size(), expectedCount);
//    DataUtil.printRecord(allRecords, RecordRecord::getId, RecordRecord::getSource);
    for (RecordRecord rr: allRecords) {
      System.out.println(rr);
    }
    if (expectedCount >= 0) {
      assertEquals(expectedCount, allRecords.size());
    }
    return allRecords;
  }

  @SuppressWarnings("unused")
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
  
  @Test
  public void testFindAny() throws SQLException {
    System.err.println("testFindAny()");
    assert connectionSource != null;
    assert dao != null;
    //noinspection resource
    assertFalse(connectionSource.getConnection().isClosed());
    Collection<RecordRecord> allRecords = showAllRecords(dao, 0);
    assertEquals(0, allRecords.size());
    setupFindTests();
    Collection<RecordRecord> results;
    assert dao != null;

    // Find Any
    results = dao.findAny(null, "bravo", "charlie", "delta");
    assertThat(getIds(results), hasItems(1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 12, 13, 14, 15, 16, 17, 18));
    assertEquals(17, results.size());

    results = dao.findAny(RecordField.SOURCE, "bravo", "charlie", "delta");
    assertTrue(arraysMatch(getIds(results), 7, 4, 12, 2, 11, 14, 6, 13, 15, 3, 8, 1, 10, 16, 5, 18, 17));

    results = dao.findAny(null, "bravo", "charlie");
    assertThat(getIds(results), hasItems(1, 2, 3, 4, 5, 6, 8, 13, 16, 17, 18));
    assertEquals(11, results.size());

    results = dao.findAny(RecordField.SOURCE, "bravo", "charlie");
    assertTrue(arraysMatch(getIds(results), 4, 2, 6, 13, 3, 8, 1, 16, 5, 18, 17));

    // Find All

    results = dao.findAll(null, "bravo", "charlie", "delta");
    assertThat(getIds(results), hasItems(4, 17, 18));
    assertEquals(3, results.size());

    results = dao.findAll(RecordField.SOURCE, "bravo", "charlie", "delta");
    assertTrue(arraysMatch(getIds(results), 4, 18, 17));

    results = dao.findAll(null, "bravo", "charlie");
    assertThat(getIds(results), hasItems(1, 3, 4, 17, 18));
    assertEquals(5, results.size());

    results = dao.findAll(RecordField.SOURCE, "bravo", "charlie");
    assertTrue(arraysMatch(getIds(results), 4, 3, 1, 18, 17));

    // Find Any In Field

    results = dao.findAnyInField(RecordField.SOURCE, null, "bravo", "charlie", "delta");
    assertThat(getIds(results), hasItems(1, 2, 3, 4, 5, 6, 7, 10, 13, 14, 16, 17, 18));
    assertEquals(13, results.size());

    results = dao.findAnyInField(RecordField.SOURCE, RecordField.SOURCE, "bravo", "charlie", "delta");
    assertTrue(arraysMatch(getIds(results), 7, 4, 2, 14, 6, 13, 3, 1, 10, 16, 5, 18, 17));

    results = dao.findAnyInField(RecordField.USERNAME, null, "bravo", "charlie", "delta");
    assertThat(getIds(results), hasItems(1, 2, 5, 6, 7, 8, 11, 17, 18));
    assertEquals(9, results.size());

    results = dao.findAnyInField(RecordField.USERNAME, RecordField.SOURCE, "bravo", "charlie", "delta");
    assertTrue(arraysMatch(getIds(results), 7, 2, 11, 6, 8, 1, 5, 18, 17));

    results = dao.findAnyInField(RecordField.PASSWORD, null, "bravo", "charlie", "delta");
    assertThat(getIds(results), hasItems(3, 4, 6, 12));
    assertEquals(4, results.size());

    results = dao.findAnyInField(RecordField.PASSWORD, RecordField.SOURCE, "bravo", "charlie", "delta");
    assertTrue(arraysMatch(getIds(results), 4, 12, 6, 3));

    results = dao.findAnyInField(RecordField.NOTES, null, "bravo", "charlie", "delta");
    assertThat(getIds(results), hasItems(4, 15, 17, 18));
    assertEquals(4, results.size());

    results = dao.findAnyInField(RecordField.NOTES, RecordField.SOURCE, "bravo", "charlie", "delta");
    assertTrue(arraysMatch(getIds(results), 4, 15, 18, 17));


    results = dao.findAnyInField(RecordField.SOURCE, null, "bravo", "charlie");
    assertThat(getIds(results), hasItems(1, 2, 3, 4, 5, 13, 16, 17, 18));
    assertEquals(9, results.size());

    results = dao.findAnyInField(RecordField.SOURCE, RecordField.SOURCE, "bravo", "charlie");
    assertTrue(arraysMatch(getIds(results), 4, 2, 13, 3, 1, 16, 5, 18, 17));

    results = dao.findAnyInField(RecordField.USERNAME, null, "bravo", "charlie");
    assertThat(getIds(results), hasItems(1, 5, 6, 8));
    assertEquals(4, results.size());

    results = dao.findAnyInField(RecordField.USERNAME, RecordField.SOURCE, "bravo", "charlie");
    assertTrue(arraysMatch(getIds(results), 6, 8, 1, 5));

    results = dao.findAnyInField(RecordField.PASSWORD, null, "bravo", "charlie");
    assertThat(getIds(results), hasItems(3));
    assertEquals(1, results.size());

    results = dao.findAnyInField(RecordField.PASSWORD, RecordField.SOURCE, "bravo", "charlie");
    assertTrue(arraysMatch(getIds(results), 3));

    results = dao.findAnyInField(RecordField.NOTES, null, "bravo", "charlie");
    assertThat(getIds(results), hasItems(4, 17, 18));
    assertEquals(3, results.size());

    results = dao.findAnyInField(RecordField.NOTES, RecordField.SOURCE, "bravo", "charlie");
    assertTrue(arraysMatch(getIds(results), 4, 18, 17));
  }
  
  private List<Integer> getIds(Collection<RecordRecord> records) {
    List<Integer> ids = new LinkedList<>();
    for (RecordRecord r : records) {
      ids.add(r.getId());
    }
    return ids;
  }
  
  private boolean arraysMatch(List<Integer> list, int... values) {
    if (list.size() != values.length) {
      throw new IllegalStateException(String.format("Size mismatch: expected %d, found %d", values.length, list.size()));
    }
    Iterator<Integer> itr = list.iterator();
    for (int i: values) {
      final Integer next = itr.next();
      if (i != next) {
        throw new IllegalStateException(String.format("Expected %d, found %s", i, next));
      }
    }
    return true;
  }
  
  private void setupFindTests() throws SQLException {
    RecordRecord[] records = {
        new RecordRecord(1, "mBravoSource", "xCharlieName", "pw", ""),
        new RecordRecord(2, "eBravoSource", "xDeltaName", "pw", ""),
        new RecordRecord(3, "kBravoSource", "dummy", "xCharliePw", ""),
        new RecordRecord(4, "bBravoSource", "name", "xDeltaPw", "xCharlieNotes"),
        new RecordRecord(5, "pCharlieSource", "xCharlieName", "pw", ""),
        new RecordRecord(6, "hDeltaSource", "xCharlieName", "xDeltaPw", ""),
        new RecordRecord(7, "aDeltaSource", "xDeltaName", "pw", ""),
        new RecordRecord(8, "lSource", "xCharlieName", "pw", ""),
        new RecordRecord(9, "dSource", "name", "pw", ""),
        new RecordRecord(10, "nDeltaSource", "name", "pw", ""),
        new RecordRecord(11, "fSource", "xDeltaName", "pw", ""),
        new RecordRecord(12, "cSource", "xName", "xDeltaPw", ""),
        new RecordRecord(13, "iBravoSource", "xEchoName", "pw", ""),
        new RecordRecord(14, "gDeltaSource", "xEchoName", "pw", ""),
        new RecordRecord(15, "jSource", "xName", "xpw", "xDeltaZ"),
        new RecordRecord(16, "oBravoSource", "name", "pw", ""),
        new RecordRecord(17, "rBravoSource", "xDelta", "pw", "xCharlieNotes"),
        new RecordRecord(18, "qCharlieSource", "xDeltaName", "pw", "zBravoNotes"),
    };
    assert dao != null;
    for (RecordRecord r: records) {
      dao.insert(r);
    }
  }
  
  private void setUpFindAllTests() throws SQLException {
    RecordRecord[] records = {
        new RecordRecord(1, "mBravoSource CharlieX", "xCharlieName", "pw", ""),
        new RecordRecord(2, "nBravoSource DeltaX", "xDeltaName", "aBravo bEchoX cCharlieZ", ""),
        new RecordRecord(3, "kBravoSource EchoX", "dummy", "xCharliePw bBravoX aDeltaZ", ""),
        new RecordRecord(4, "bBravoSource CharlieX Delta Force", "name", "xDeltaPw", "xCharlieNotes"),
        new RecordRecord(5, "pCharlieSource", "xCharlieName yBravo", "pw", ""),
        new RecordRecord(6, "hDeltaSource", "xCharlieNameX yDeltaX", "xDeltaPw", "xDeltaNameX PBravoNameX ZCharlieX"),
        new RecordRecord(7, "aDeltaSource", "xDeltaNameX PBravoNameX ZCharlieX", "pw", ""),
        new RecordRecord(8, "lSource", "xCharlieName", "pw", ""),
        new RecordRecord(9, "dSource", "name", "CharlieXpw", ""),
        new RecordRecord(10, "eDeltaSource", "name", "CharlieX BravoPw XDeltaZ", "XBravoZ aCharlieZ"),
        new RecordRecord(11, "jSource", "xDeltaName zCharlieX bBravoZ", "pw", ""),
        new RecordRecord(12, "cSource", "xName", "xDeltaPw", ""),
        new RecordRecord(13, "qBravoSource dDeltaX aCharlieB", "xEchoName", "ABravoZ BDeltaX bCharlieX pw", ""),
        new RecordRecord(14, "gDeltaSource", "xEchoName", "pw", "xBravo bCharlie ZDeltaX"),
        new RecordRecord(15, "fSource", "xName", "xpw", "xDeltaNameX PBravoNameX ZCharlieX"),
        new RecordRecord(16, "oBravoSource", "name", "pw", ""),
        new RecordRecord(17, "rBravoSource", "xDelta", "pw", "xCharlieNotes"),
        new RecordRecord(18, "iCharlieSource aDeltaX bBravoX", "xDeltaName aBravoX bCharlieZ", "bEcho", "zBravoNotes"),
    };
    assert dao != null;
    for (RecordRecord r : records) {
      dao.insert(r);
    }
  }

  @Test
  public void findAllInFieldTest() throws SQLException {
    Collection<RecordRecord> results;
    assert dao != null;
    setUpFindAllTests();

    // Find All In Field
    results = dao.findAllInField(RecordField.SOURCE, null, "bravo", "charlie", "delta");
    assertThat(getIds(results), hasItems(4, 13, 18));
    assertEquals(3, results.size());

    results = dao.findAllInField(RecordField.SOURCE, RecordField.SOURCE, "bravo", "charlie", "delta");
    assertTrue(arraysMatch(getIds(results), 4, 18, 13));

    results = dao.findAllInField(RecordField.USERNAME, null, "bravo", "charlie", "delta");
    assertThat(getIds(results), hasItems(7, 11, 18));
    assertEquals(3, results.size());

    results = dao.findAllInField(RecordField.USERNAME, RecordField.SOURCE, "bravo", "charlie", "delta");
    assertTrue(arraysMatch(getIds(results), 7, 18, 11));

    results = dao.findAllInField(RecordField.PASSWORD, null, "bravo", "charlie", "delta");
    assertThat(getIds(results), hasItems(3, 10, 13));
    assertEquals(3, results.size());

    results = dao.findAllInField(RecordField.PASSWORD, RecordField.SOURCE, "bravo", "charlie", "delta");
    assertTrue(arraysMatch(getIds(results), 10, 3, 13));

    results = dao.findAllInField(RecordField.NOTES, null, "bravo", "charlie", "delta");
    assertThat(getIds(results), hasItems(6, 14, 15));
    assertEquals(3, results.size());

    results = dao.findAllInField(RecordField.NOTES, RecordField.SOURCE, "bravo", "charlie", "delta");
    assertTrue(arraysMatch(getIds(results), 15, 14, 6));


    results = dao.findAllInField(RecordField.SOURCE, null, "bravo", "charlie");
    assertThat(getIds(results), hasItems(1, 4, 13, 18));
    assertEquals(4, results.size());

    results = dao.findAllInField(RecordField.SOURCE, RecordField.SOURCE, "bravo", "charlie");
    assertTrue(arraysMatch(getIds(results), 4, 18, 1, 13));

    results = dao.findAllInField(RecordField.USERNAME, null, "bravo", "charlie");
    assertThat(getIds(results), hasItems(5, 7, 11, 18));
    assertEquals(4, results.size());

    results = dao.findAllInField(RecordField.USERNAME, RecordField.SOURCE, "bravo", "charlie");
    assertTrue(arraysMatch(getIds(results), 7, 18, 11, 5));

    results = dao.findAllInField(RecordField.PASSWORD, null, "bravo", "charlie");
    assertThat(getIds(results), hasItems(2, 3, 10, 13));
    assertEquals(4, results.size());

    results = dao.findAllInField(RecordField.PASSWORD, RecordField.SOURCE, "bravo", "charlie");
    assertTrue(arraysMatch(getIds(results), 10, 3, 2, 13));

    results = dao.findAllInField(RecordField.NOTES, null, "bravo", "charlie");
    assertThat(getIds(results), hasItems(6, 10, 14, 15));
    assertEquals(4, results.size());

    results = dao.findAllInField(RecordField.NOTES, RecordField.SOURCE, "bravo", "charlie");
    assertTrue(arraysMatch(getIds(results), 10, 15, 14, 6));
  }


  @Test
  @SuppressWarnings({"HardCodedStringLiteral", "unused", "HardcodedLineSeparator"})
  public void testDao() throws SQLException, IOException {
    System.err.println("testDao");
    assert dao != null;
    assert connectionSource != null;
    //noinspection HardcodedFileSeparator
    try {
      doTestDao(dao, connectionSource);
    } finally {

      // cleanup even on failure.
      Collection<RecordRecord> allRecords = showAllRecords(dao, -1);
      int count = allRecords.size();
      for (RecordRecord recordRecord : allRecords) {
        dao.delete(recordRecord);
      }
      allRecords = showAllRecords(dao, 0);
      assertEquals(0, allRecords.size());
    }
  }
}
