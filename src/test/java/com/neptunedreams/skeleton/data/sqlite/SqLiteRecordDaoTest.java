package com.neptunedreams.skeleton.data.sqlite;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.neptunedreams.framework.data.ConnectionSource;
import com.neptunedreams.framework.data.DatabaseInfo;
import com.neptunedreams.skeleton.data.SiteField;
import com.neptunedreams.skeleton.gen.tables.records.SiteRecord;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

//import com.neptunedreams.skeleton.data.Record;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/27/17
 * <p>Time: 5:42 PM
 *
 * @author Miguel Muñoz
 */
@SuppressWarnings({"HardCodedStringLiteral", "HardcodedLineSeparator", "MagicNumber"})
public class SqLiteRecordDaoTest {

  private static ConnectionSource connectionSource;
  private static SQLiteRecordDao dao;

  @BeforeClass
  public static void setup() throws SQLException, IOException {
    System.err.printf("Before%n%n"); // NON-NLS
    //noinspection HardcodedFileSeparator
    final DatabaseInfo info = new SQLiteInfo("/.sqlite.skeletonTest");
    info.init();
    connectionSource = info.getConnectionSource();
    dao = (SQLiteRecordDao) info.<SiteRecord, Integer, SiteField>getDao(SiteRecord.class, connectionSource);
  }
  
  @After
  public void tearDown() throws SQLException {
    System.err.printf("After%n%n"); // NON-NLS
    assert dao != null;
    Collection<SiteRecord> results = dao.findAll(null);
    for (SiteRecord siteRecord : results) {
      siteRecord.delete();
    }
  }

  private void doTestDao(SQLiteRecordDao dao, ConnectionSource connectionSource) throws SQLException {
    assertNotNull(connectionSource);
//    ensureHomeExists(info.getHomeDir());
    assertNotNull(dao);
    // todo: fix CreateTableIfNeeded when the whole schema was created.
//    dao.createTableIfNeeded();
//    if (info.isCreateSchemaAllowed()) {
//      info.createSchema();
//    }
    SiteRecord record1 = new SiteRecord(0,"TestSiteAlpha", "testName", "testPw", "testNotes\nNote line 2\nNoteLine 3");
//    SiteRecord record1 = createRecord("TestSiteAlpha", "testName", "testPw", "testNotes\nNote line 2\nNoteLine 3");
    assertFalse(connectionSource.getConnection().isClosed());

    Collection<SiteRecord> allRecords = showAllRecords(dao, 0);
    assertEquals(0, allRecords.size());

    dao.insert(record1);
    Integer r1Id = record1.getId();
    allRecords = showAllRecords(dao, 1);
    assertEquals(1, allRecords.size());
    assertEquals(allRecords.iterator().next().getId(), r1Id);

    SiteRecord record2 = new SiteRecord(0, "t2Site", "t2User", "t2Pw", "t2Note");
//    SiteRecord record2 = createRecord("t2Site", "t2User", "t2Pw", "t2Note");
    dao.insert(record2);
    Integer r2Id = record2.getId();
    assertNotEquals(r1Id, r2Id);
    allRecords = showAllRecords(dao, 2);
    assertEquals(2, allRecords.size());
//    Set<Integer> set1 = new HashSet<>();for (SiteRecord rr: allRecords) set1.add(rr.getId());
    Set<Integer> set2 = allRecords
        .stream()
        .map(SiteRecord::getId)
        .collect(Collectors.toSet());
    assertThat(set2, hasItems(r1Id, r2Id));

    allRecords = dao.getAll(SiteField.Source);
    System.out.printf("getAll() returned %d records, expecting 2%n", allRecords.size());
    assertEquals(2, allRecords.size());
    showAllRecords(dao, 2);

    Collection<SiteRecord> foundRecords = dao.find("alpha", SiteField.Source);
    System.out.printf("find(alpha) returned %d records, expecting 1%n", foundRecords.size());
    assertEquals(1, foundRecords.size()); // I expect this to fail
    record1 = foundRecords.iterator().next();

    // Test update
    final String revisedName = "revisedName";
    String originalName = record1.getUsername();
    record1.setUsername(revisedName);
    System.out.printf("Changing the Name from %s to %s%n", originalName, revisedName);
    dao.update(record1);
    foundRecords = dao.find("alpha", SiteField.Source);
    System.out.printf("Found %d records%n", foundRecords.size());
    assertEquals(1, foundRecords.size());
    SiteRecord revisedRecord = foundRecords.iterator().next();
    assertEquals("revisedName", revisedRecord.getUsername());
    testShowRecord(revisedRecord);

    int deletedId = revisedRecord.getId();
    dao.delete(revisedRecord);
    allRecords = dao.getAll(SiteField.Source);
    System.out.printf("Total of %d records after deleting id %d%n", allRecords.size(), deletedId);
    assertEquals(1, allRecords.size());
    SiteRecord remainingRecord = allRecords.iterator().next();
    dao.delete(remainingRecord);
    allRecords = dao.getAll(SiteField.Source);
    assertEquals(0, allRecords.size());
    System.out.printf("Total of %d records after deleting 1%n", allRecords.size());

//    SiteRecord record1b = new SiteRecord(1, "TestSiteAlpha", "testName", "testPw", "testNotes\nNote line 2\nNoteLine 3");
//    dao.insert(record1b);
//    allRecords = dao.getAll(com.neptunedreams.skeleton.data.SiteField.SOURCE);
//    assertEquals(0, allRecords.size());
//    System.out.printf("Total of %d records after lastInsert 1%n", allRecords.size());

  }

  private Collection<SiteRecord> showAllRecords(final SQLiteRecordDao dao, int expectedCount) throws SQLException {
    Collection<SiteRecord> allRecords = dao.getAll(SiteField.Source);
    System.out.printf("getAll() returned %d records, expecting %d%n", allRecords.size(), expectedCount);
//    DataUtil.printRecord(allRecords, SiteRecord::getId, SiteRecord::getSource);
    for (SiteRecord rr: allRecords) {
      System.out.println(rr);
    }
    if (expectedCount >= 0) {
      assertEquals(expectedCount, allRecords.size());
    }
    return allRecords;
  }

  @SuppressWarnings("unused")
  private SiteRecord createRecord(String source, String userName, String password, String note) {
    SiteRecord record = new SiteRecord();
    record.setUsername(userName);
    record.setSource(source);
    record.setPassword(password);
    record.setNotes(note);
    return record;
  }

  private void testShowRecord(final SiteRecord record) {
    System.out.printf("Record: %n  id: %d%n  sr: %s%n  un: %s%n  pw: %s%n  Nt: %s%n",
        record.getId(), record.getSource(), record.getUsername(),
        record.getPassword(), record.getNotes());
  }
  
  @Test
  public void testFindAny() throws SQLException {
    System.err.println("testFindAny()");
    assert connectionSource != null;
    assert dao != null;
    assertFalse(connectionSource.getConnection().isClosed());
    Collection<SiteRecord> allRecords = showAllRecords(dao, 0);
    assertEquals(0, allRecords.size());
    setupFindTests();
    Collection<SiteRecord> results;
    assert dao != null;

    // Find Any
    results = dao.findAny(null, "bravo", "charlie", "delta");
    assertThat(getIds(results), hasItems(1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 12, 13, 14, 15, 16, 17, 18));
    assertEquals(17, results.size());

    results = dao.findAny(SiteField.Source, "bravo", "charlie", "delta");
    assertTrue(arraysMatch(getIds(results), 7, 4, 12, 2, 11, 14, 6, 13, 15, 3, 8, 1, 10, 16, 5, 18, 17));

    results = dao.findAny(null, "bravo", "charlie");
    assertThat(getIds(results), hasItems(1, 2, 3, 4, 5, 6, 8, 13, 16, 17, 18));
    assertEquals(11, results.size());

    results = dao.findAny(SiteField.Source, "bravo", "charlie");
    assertTrue(arraysMatch(getIds(results), 4, 2, 6, 13, 3, 8, 1, 16, 5, 18, 17));

    // Find All

    results = dao.findAll(null, "bravo", "charlie", "delta");
    assertThat(getIds(results), hasItems(4, 17, 18));
    assertEquals(3, results.size());

    results = dao.findAll(SiteField.Source, "bravo", "charlie", "delta");
    assertTrue(arraysMatch(getIds(results), 4, 18, 17));

    results = dao.findAll(null, "bravo", "charlie");
    assertThat(getIds(results), hasItems(1, 3, 4, 17, 18));
    assertEquals(5, results.size());

    results = dao.findAll(SiteField.Source, "bravo", "charlie");
    assertTrue(arraysMatch(getIds(results), 4, 3, 1, 18, 17));

    // Find Any In Field

    results = dao.findAnyInField(SiteField.Source, null, "bravo", "charlie", "delta");
    assertThat(getIds(results), hasItems(1, 2, 3, 4, 5, 6, 7, 10, 13, 14, 16, 17, 18));
    assertEquals(13, results.size());

    results = dao.findAnyInField(SiteField.Source, SiteField.Source, "bravo", "charlie", "delta");
    assertTrue(arraysMatch(getIds(results), 7, 4, 2, 14, 6, 13, 3, 1, 10, 16, 5, 18, 17));

    results = dao.findAnyInField(SiteField.Username, null, "bravo", "charlie", "delta");
    assertThat(getIds(results), hasItems(1, 2, 5, 6, 7, 8, 11, 17, 18));
    assertEquals(9, results.size());

    results = dao.findAnyInField(SiteField.Username, SiteField.Source, "bravo", "charlie", "delta");
    assertTrue(arraysMatch(getIds(results), 7, 2, 11, 6, 8, 1, 5, 18, 17));

    results = dao.findAnyInField(SiteField.Password, null, "bravo", "charlie", "delta");
    assertThat(getIds(results), hasItems(3, 4, 6, 12));
    assertEquals(4, results.size());

    results = dao.findAnyInField(SiteField.Password, SiteField.Source, "bravo", "charlie", "delta");
    assertTrue(arraysMatch(getIds(results), 4, 12, 6, 3));

    results = dao.findAnyInField(SiteField.Notes, null, "bravo", "charlie", "delta");
    assertThat(getIds(results), hasItems(4, 15, 17, 18));
    assertEquals(4, results.size());

    results = dao.findAnyInField(SiteField.Notes, SiteField.Source, "bravo", "charlie", "delta");
    assertTrue(arraysMatch(getIds(results), 4, 15, 18, 17));


    results = dao.findAnyInField(SiteField.Source, null, "bravo", "charlie");
    assertThat(getIds(results), hasItems(1, 2, 3, 4, 5, 13, 16, 17, 18));
    assertEquals(9, results.size());

    results = dao.findAnyInField(SiteField.Source, SiteField.Source, "bravo", "charlie");
    assertTrue(arraysMatch(getIds(results), 4, 2, 13, 3, 1, 16, 5, 18, 17));

    results = dao.findAnyInField(SiteField.Username, null, "bravo", "charlie");
    assertThat(getIds(results), hasItems(1, 5, 6, 8));
    assertEquals(4, results.size());

    results = dao.findAnyInField(SiteField.Username, SiteField.Source, "bravo", "charlie");
    assertTrue(arraysMatch(getIds(results), 6, 8, 1, 5));

    results = dao.findAnyInField(SiteField.Password, null, "bravo", "charlie");
    assertThat(getIds(results), hasItems(3));
    assertEquals(1, results.size());

    results = dao.findAnyInField(SiteField.Password, SiteField.Source, "bravo", "charlie");
    assertTrue(arraysMatch(getIds(results), 3));

    results = dao.findAnyInField(SiteField.Notes, null, "bravo", "charlie");
    assertThat(getIds(results), hasItems(4, 17, 18));
    assertEquals(3, results.size());

    results = dao.findAnyInField(SiteField.Notes, SiteField.Source, "bravo", "charlie");
    assertTrue(arraysMatch(getIds(results), 4, 18, 17));
  }
  
  private List<Integer> getIds(Collection<? extends SiteRecord> records) {
    List<Integer> ids = new LinkedList<>();
    for (SiteRecord r : records) {
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
        throw new IllegalStateException(String.format("Order mismatch: Expected %d found %s: %nExpected %s%n   Found %s",
            i, next, Arrays.toString(values), list));
      }
    }
    return true;
  }
  
  private void setupFindTests() throws SQLException {
    SiteRecord[] records = {
        new SiteRecord(1, "mBravoSource", "xCharlieName", "pw", ""),
        new SiteRecord(2, "EBravoSource", "xDeltaName", "pw", ""),
        new SiteRecord(3, "kBravoSource", "dummy", "xCharliePw", ""),
        new SiteRecord(4, "BBravoSource", "name", "xDeltaPw", "xCharlieNotes"),
        new SiteRecord(5, "pCharlieSource", "xCharlieName", "pw", ""),
        new SiteRecord(6, "HDeltaSource", "xCharlieName", "xDeltaPw", ""),
        new SiteRecord(7, "aDeltaSource", "xDeltaName", "pw", ""),
        new SiteRecord(8, "lSource", "xCharlieName", "pw", ""),
        new SiteRecord(9, "dSource", "name", "pw", ""),
        new SiteRecord(10, "NDeltaSource", "name", "pw", ""),
        new SiteRecord(11, "fSource", "xDeltaName", "pw", ""),
        new SiteRecord(12, "cSource", "xName", "xDeltaPw", ""),
        new SiteRecord(13, "iBravoSource", "xEchoName", "pw", ""),
        new SiteRecord(14, "GDeltaSource", "xEchoName", "pw", ""),
        new SiteRecord(15, "jSource", "xName", "xpw", "xDeltaZ"),
        new SiteRecord(16, "OBravoSource", "name", "pw", ""),
        new SiteRecord(17, "RBravoSource", "xDelta", "pw", "xCharlieNotes"),
        new SiteRecord(18, "qCharlieSource", "xDeltaName", "pw", "zBravoNotes"),
    };
    assert dao != null;
    for (SiteRecord r: records) {
      dao.insert(r);
    }
  }
  
  private void setUpFindAllTests() throws SQLException {
    SiteRecord[] records = {
        new SiteRecord(1, "mBravoSource CharlieX", "xCharlieName", "pw", ""),
        new SiteRecord(2, "NBravoSource DeltaX", "xDeltaName", "aBravo bEchoX cCharlieZ", ""),
        new SiteRecord(3, "KBravoSource EchoX", "dummy", "xCharliePw bBravoX aDeltaZ", ""),
        new SiteRecord(4, "bBravoSource CharlieX Delta Force", "name", "xDeltaPw", "xCharlieNotes"),
        new SiteRecord(5, "pCharlieSource", "xCharlieName yBravo", "pw", ""),
        new SiteRecord(6, "HDeltaSource", "xCharlieNameX yDeltaX", "xDeltaPw", "xDeltaNameX PBravoNameX ZCharlieX"),
        new SiteRecord(7, "aDeltaSource", "xDeltaNameX PBravoNameX ZCharlieX", "pw", ""),
        new SiteRecord(8, "LSource", "xCharlieName", "pw", ""),
        new SiteRecord(9, "dSource", "name", "CharlieXpw", ""),
        new SiteRecord(10, "eDeltaSource", "name", "CharlieX BravoPw XDeltaZ", "XBravoZ aCharlieZ"),
        new SiteRecord(11, "JSource", "xDeltaName zCharlieX bBravoZ", "pw", ""),
        new SiteRecord(12, "CSource", "xName", "xDeltaPw", ""),
        new SiteRecord(13, "qBravoSource dDeltaX aCharlieB", "xEchoName", "ABravoZ BDeltaX bCharlieX pw", ""),
        new SiteRecord(14, "GDeltaSource", "xEchoName", "pw", "xBravo bCharlie ZDeltaX"),
        new SiteRecord(15, "fSource", "xName", "xpw", "xDeltaNameX PBravoNameX ZCharlieX"),
        new SiteRecord(16, "OBravoSource", "name", "pw", ""),
        new SiteRecord(17, "rBravoSource", "xDelta", "pw", "xCharlieNotes"),
        new SiteRecord(18, "ICharlieSource aDeltaX bBravoX", "xDeltaName aBravoX bCharlieZ", "bEcho", "zBravoNotes"),
    };
    assert dao != null;
    for (SiteRecord r : records) {
      dao.insert(r);
    }
  }

  @Test
  public void findAllInFieldTest() throws SQLException {
    Collection<SiteRecord> results;
    assert dao != null;
    setUpFindAllTests();

    // Find All In Field
    results = dao.findAllInField(SiteField.Source, null, "bravo", "charlie", "delta");
    assertThat(getIds(results), hasItems(4, 13, 18));
    assertEquals(3, results.size());

    results = dao.findAllInField(SiteField.Source, SiteField.Source, "bravo", "charlie", "delta");
    assertTrue(arraysMatch(getIds(results), 4, 18, 13));

    results = dao.findAllInField(SiteField.Username, null, "bravo", "charlie", "delta");
    assertThat(getIds(results), hasItems(7, 11, 18));
    assertEquals(3, results.size());

    results = dao.findAllInField(SiteField.Username, SiteField.Source, "bravo", "charlie", "delta");
    assertTrue(arraysMatch(getIds(results), 7, 18, 11));

    results = dao.findAllInField(SiteField.Password, null, "bravo", "charlie", "delta");
    assertThat(getIds(results), hasItems(3, 10, 13));
    assertEquals(3, results.size());

    results = dao.findAllInField(SiteField.Password, SiteField.Source, "bravo", "charlie", "delta");
    assertTrue(arraysMatch(getIds(results), 10, 3, 13));

    results = dao.findAllInField(SiteField.Notes, null, "bravo", "charlie", "delta");
    assertThat(getIds(results), hasItems(6, 14, 15));
    assertEquals(3, results.size());

    results = dao.findAllInField(SiteField.Notes, SiteField.Source, "bravo", "charlie", "delta");
    assertTrue(arraysMatch(getIds(results), 15, 14, 6));


    results = dao.findAllInField(SiteField.Source, null, "bravo", "charlie");
    assertThat(getIds(results), hasItems(1, 4, 13, 18));
    assertEquals(4, results.size());

    results = dao.findAllInField(SiteField.Source, SiteField.Source, "bravo", "charlie");
    assertTrue(arraysMatch(getIds(results), 4, 18, 1, 13));

    results = dao.findAllInField(SiteField.Username, null, "bravo", "charlie");
    assertThat(getIds(results), hasItems(5, 7, 11, 18));
    assertEquals(4, results.size());

    results = dao.findAllInField(SiteField.Username, SiteField.Source, "bravo", "charlie");
    assertTrue(arraysMatch(getIds(results), 7, 18, 11, 5));

    results = dao.findAllInField(SiteField.Password, null, "bravo", "charlie");
    assertThat(getIds(results), hasItems(2, 3, 10, 13));
    assertEquals(4, results.size());

    results = dao.findAllInField(SiteField.Password, SiteField.Source, "bravo", "charlie");
    assertTrue(arraysMatch(getIds(results), 10, 3, 2, 13));

    results = dao.findAllInField(SiteField.Notes, null, "bravo", "charlie");
    assertThat(getIds(results), hasItems(6, 10, 14, 15));
    assertEquals(4, results.size());

    results = dao.findAllInField(SiteField.Notes, SiteField.Source, "bravo", "charlie");
    assertTrue(arraysMatch(getIds(results), 10, 15, 14, 6));
  }


  @Test
  @SuppressWarnings({"HardCodedStringLiteral", "unused"})
  public void testDao() throws SQLException {
    System.err.println("testDao");
    assert dao != null;
    assert connectionSource != null;
    //noinspection HardcodedFileSeparator
    try {
      doTestDao(dao, connectionSource);
    } finally {

      // cleanup even on failure.
      Collection<SiteRecord> allRecords = showAllRecords(dao, -1);
      int count = allRecords.size();
      for (SiteRecord siteRecord : allRecords) {
        dao.delete(siteRecord);
      }
      allRecords = showAllRecords(dao, 0);
      assertEquals(0, allRecords.size());
    }
  }
}
