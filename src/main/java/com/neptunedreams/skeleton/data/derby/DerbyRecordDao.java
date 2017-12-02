package com.neptunedreams.skeleton.data.derby;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import com.neptunedreams.skeleton.data.ConnectionSource;
import com.neptunedreams.skeleton.data.Dao;
import com.neptunedreams.skeleton.data.Record;
import com.neptunedreams.skeleton.data.RecordField;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/29/17
 * <p>Time: 1:03 AM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings({"StringConcatenation", "SqlResolve", "StringConcatenationMissingWhitespace"})
public class DerbyRecordDao implements Dao<Record, Integer> {
  private static final String CREATE_TABLE = "CREATE TABLE record (" +
      "id INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY," +
      "source VARCHAR(256) NOT NULL," +
      "username VARCHAR(256) NOT NULL," +
      "password VARCHAR(256) NOT NULL," +
      "notes LONG VARCHAR NOT NULL" +
      ')';
  private static final String SELECT_ALL = "SELECT * FROM record ORDER BY ";
  private static final String FIND = "SELECT * FROM record WHERE (source LIKE ?) OR (username like ?) OR (password like ?) OR (notes like ?) ORDER BY ";
  private static final String FIND_BY_FIELD = "SELECT * FROM record WHERE ? LIKE ? ORDER BY ";
  private static final String SAVE = "UPDATE record SET source = ?, username = ?, password = ?, notes = ? where id = ?";
  private static final String INSERT = "INSERT INTO record (source, username, password, notes) VALUES (?, ?, ?, ?)";
  private static final String DELETE = "DELETE FROM record WHERE ID = ?";
  private static final char WC = '%';
  private final Connection connection;
  
  public DerbyRecordDao(ConnectionSource source) {
    connection = source.getConnection();
  }
  
  @Override
  public boolean createTableIfNeeded() throws SQLException {
    try {
      connection.prepareStatement(SELECT_ALL + "id");
    } catch (SQLException e) {
      if (e.getMessage().contains("does not exist.")) {
        createTable();
      }
      return false;
    }
    return true;
  }

  private void createTable() throws SQLException {
    PreparedStatement statement = connection.prepareStatement(CREATE_TABLE);
    statement.execute();
  }


  @Override
  public Collection<Record> getAll(final RecordField orderBy) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(SELECT_ALL + orderBy);
//    statement.setObject(1, orderBy);
    return extractRecords(statement);
  }

  private Collection<Record> extractRecords(final PreparedStatement statement) throws SQLException {
    try (ResultSet resultSet = statement.executeQuery()) {
      List<Record> recordList = new LinkedList<>();
      boolean rowIsValid = resultSet.next();
      while (rowIsValid) {
        Record aRecord = new Record();
        aRecord.setId(resultSet.getInt(1));
        aRecord.setSource(resultSet.getString(2));
        aRecord.setUserName(resultSet.getString(3));
        aRecord.setPassword(resultSet.getString(4));
        aRecord.setNotes(resultSet.getString(5));
        rowIsValid = resultSet.next();
        recordList.add(aRecord);
      }
      return recordList;
    }
  }

  @Override
  public Collection<Record> find(final String text, final RecordField orderBy) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(FIND + orderBy);
//    System.out.println("Find: " + FIND + orderBy);
    //noinspection StringConcatenationMissingWhitespace
    String wildCardText = WC + text + WC;
//    System.out.println("WC text: " + wildCardText);
    statement.setObject(1, wildCardText);
    statement.setObject(2, wildCardText);
    statement.setObject(3, wildCardText);
    statement.setObject(4, wildCardText);
    return extractRecords(statement);
  }

  @Override
  public Collection<Record> findInField(final String text, @NonNull final RecordField fieldName, @Nullable final RecordField orderBy) throws SQLException {
    final String sql = FIND_BY_FIELD + orderBy;
//    System.out.println("Find By Field: " + sql);
    PreparedStatement statement = connection.prepareStatement(sql);
    //noinspection StringConcatenationMissingWhitespace
    String wildCardText = WC + text + WC;
    statement.setObject(1, fieldName.toString());
    statement.setObject(2, wildCardText);
    return extractRecords(statement);
  }

  @Override
  public void save(final Record entity) throws SQLException {
//    boolean isInsert = false;
//    if (entity.getId() == 0) {
//      doInsert(entity);
//      isInsert = true;
//    } else {
      doSave(entity);
//    }
    connection.commit();
//    return false;
  }
  
  public void insert(final Record entity) throws SQLException {
    doInsert(entity);
    connection.commit();
  }

  private void doSave(final Record entity) throws SQLException {
//    System.err.printf("Updating entity %s%n", entity); // NON-NLS
    try (PreparedStatement statement = connection.prepareStatement(SAVE)) {
      
      statement.setObject(1, entity.getSource());
      statement.setObject(2, entity.getUserName());
      statement.setObject(3, entity.getPassword());
      statement.setObject(4, entity.getNotes());
      statement.setObject(5, entity.getId());
      statement.execute();
    }
  }

  private void doInsert(final Record entity) throws SQLException {
//    System.err.printf("Inserting entity: %s%n", entity);
    try (PreparedStatement statement = connection.prepareStatement(INSERT)) {
      statement.setObject(1, entity.getSource());
      statement.setObject(2, entity.getUserName());
      statement.setObject(3, entity.getPassword());
      statement.setObject(4, entity.getNotes());
      statement.execute();
    }
  }

  @Override
  public void delete(final Record entity) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(DELETE)) {
      statement.setObject(1, entity.getId());
      statement.execute();
    }
  }
  
  @Override
  public Integer getNextId() throws SQLException {
    String sql = "SELECT MAX(ID) FROM RECORD";
    PreparedStatement statement = connection.prepareStatement(sql);
    try (ResultSet resultSet = statement.executeQuery()) {
      resultSet.next();
      return resultSet.getInt(1) + 1;
    }
  }

  @Override
  public Integer getPrimaryKey(final Record entity) {
    return entity.getId();
  }

  @Override
  public void setPrimaryKey(final Record entity, final Integer primaryKey) {
    entity.setId(primaryKey);
  }

//  @Override
//  public int getNewId(final Record entity) throws SQLException {
//    String sql = "SELECT * FROM record WHERE source = ? and username = ? and password = ?"; //NON-NLS
//    PreparedStatement statement = connection.prepareStatement(sql);
//    statement.setObject(1, entity.getSource());
//    statement.setObject(2, entity.getUserName());
//    statement.setObject(3, entity.getPassword());
//    try (ResultSet resultSet = statement.executeQuery()) {
//      int highestId = Integer.MIN_VALUE;
//      boolean hasNext = resultSet.next();
//      while (hasNext) {
//        int id = resultSet.getInt(1);
//        if (id > highestId) {
//          highestId = id;
//        }
//        hasNext = resultSet.next();
//      }
//      return highestId;
//    }
//  }
//
  /**
   * This is an attempt (ultimately successful) to fix the sql statement that fails during code generation.
   * @param <T>
   * @return
   * @throws SQLException
   */
//  @Override
  public <T> Collection<T> getTableInfo() throws SQLException {
    String sql1 = "select * from \"SYS\".\"SYSSCHEMAS\"";
    PreparedStatement statement = connection.prepareStatement(sql1);
    ResultSet resultSet = statement.executeQuery();
    printResultSet(resultSet);
    String sql2 = "select * from \"SYS\".\"SYSTABLES\"";
    statement = connection.prepareStatement(sql2);
    resultSet = statement.executeQuery();
    printResultSet(resultSet);
    
        String sql = "select " +
        "\"SYS\".\"SYSSCHEMAS\".\"SCHEMANAME\", " +
        "\"SYS\".\"SYSTABLES\".\"TABLENAME\", " +
        "\"SYS\".\"SYSTABLES\".\"TABLEID\" " +
        "from " +
        "\"SYS\".\"SYSTABLES\" " +
        "join " +
        "\"SYS\".\"SYSSCHEMAS\" " +
        "on " +
        "\"SYS\".\"SYSTABLES\".\"SCHEMAID\" = \"SYS\".\"SYSSCHEMAS\".\"SCHEMAID\" " +
        "where " +
        "cast(\"SYS\".\"SYSSCHEMAS\".\"SCHEMANAME\" as varchar(32672)) " +
        "in (cast(? as varchar(32672)), cast(? as varchar(32672)), cast(? as varchar(32672)), cast(? as varchar(32672)), cast(? as varchar(32672)), cast(? as varchar(32672)), cast(? as varchar(32672)), cast(? as varchar(32672)), cast(? as varchar(32672)), cast(? as varchar(32672)), cast(? as varchar(32672))) " +
        "order by " +
        "\"SYS\".\"SYSSCHEMAS\".\"SCHEMANAME\", \"SYS\".\"SYSTABLES\".\"TABLENAME\"";
    System.out.println(sql);
    statement = connection.prepareStatement(sql);
    String[] params = { 
        "SYSIBM",
        "SYS",
        "SYSCAT",
        "SYSFUN",
        "SYSPROC",
        "SYSSTAT",
        "NULLID",
        "SQLJ",
        "SYSCS_DIAG",
        "SYSCS_UTIL",
        "APP"
    };
    for (int ii=0; ii<params.length; ++ii) {
      statement.setObject(ii+1, params[ii]);
    }
    resultSet = statement.executeQuery();
    printResultSet(resultSet);
    return new LinkedList<>();
  }

  private void printResultSet(final ResultSet resultSet) throws SQLException {
    ResultSetMetaData metaData = resultSet.getMetaData();
    int columnCount = metaData.getColumnCount();
    List<List<String>> results = new LinkedList<>();
    
    // maxWidth_1 is 1-based
    int[] maxWidth_1 = new int[columnCount+1];
    Arrays.fill(maxWidth_1, 0);
    
    // Gather all the data and calculate the maximum column widths.
    boolean hasNext = resultSet.next();
    while (hasNext) {
      List<String> row = new ArrayList<>();
      for (int col=1; col<= columnCount; ++col) {
        String value = String.valueOf(resultSet.getString(col)); // I use String.valueOf() to turn null into "null"
        // value is definitely not null here.
        row.add(value);
        int width = value.length();
        if (maxWidth_1[col] < width) {
          maxWidth_1[col] = width;
        }
      }
      results.add(row);
      hasNext = resultSet.next();
    }
    
    // Incorporate the header widths into the column widths.
    for (int ii=1; ii<=columnCount; ++ii) {
      String cName = metaData.getColumnName(ii);
      int width = cName.length();
      if (maxWidth_1[ii] < width) {
        maxWidth_1[ii] = width;
      }
    }
    
    // Find the widest column
    int max = max(maxWidth_1);
    char[] maxArray = new char[max + 1]; // + 1 for space between columns
    //noinspection MagicCharacter
    Arrays.fill(maxArray, ' '); // Fill the array with spaces.
    String spaces = new String(maxArray); // This is the maximum number of spaces for any column in any row.

    // Pack data into an empty line from the column headers.
    StringBuilder line = new StringBuilder();
    for (int ii=1; ii<=columnCount; ++ii) {
      pack(line, metaData.getColumnName(ii), maxWidth_1[ii], spaces);
    }
    System.out.println(line);
    
    // Print out the underscores
    line = new StringBuilder();
    for (int ii=1; ii<=columnCount; ++ii) {
      final int width = maxWidth_1[ii];
      char[] dashes = new char[width+1];
      Arrays.fill(dashes, '-');
      dashes[width] = ' ';
      pack(line, new String(dashes), width, spaces);
    }
    System.out.println(line);
    
    // For each record, pack data into an empty line.
    for (List<String> resultData : results) {
      line = new StringBuilder();
      for (int col = 0; col < columnCount; ++col) {
        pack(line, resultData.get(col), maxWidth_1[col+1], spaces);
      }
      System.out.println(line);
    }
    System.out.println("---");
  }
  
  private static int max(int[] array) {
    int max = Integer.MIN_VALUE;
    for (final int i : array) {
      if (i > max) {
        max = i;
      }
    }
    return max;
  }

  private void pack(StringBuilder line, final String value, final int width, String spaces) {
    line.append(value);
//    line.append(spaces.substring(value.length(), width+1));
    line.append(spaces.substring(0, (width - value.length()) + 1));
  }
}
