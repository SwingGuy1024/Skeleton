package com.neptunedreams.skeleton.data.derby;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import com.neptunedreams.skeleton.data.ConnectionSource;
import com.neptunedreams.skeleton.data.Dao;
import com.neptunedreams.skeleton.data.Record;
import com.neptunedreams.skeleton.data.RecordField;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.neptunedreams.skeleton.DataUtil.*;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/29/17
 * <p>Time: 1:03 AM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings({"StringConcatenation", "SqlResolve", "StringConcatenationMissingWhitespace", "SqlNoDataSourceInspection", "resource", "HardCodedStringLiteral"})
public final class DerbyRecordDao implements Dao<Record, Integer> {
  private static final String CREATE_TABLE = "CREATE TABLE record (" +
      "id INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY," +
      "source VARCHAR(256) NOT NULL," +
      "username VARCHAR(256) NOT NULL," +
      "password VARCHAR(256) NOT NULL," +
      "notes LONG VARCHAR NOT NULL" +
      ')';
  private static final String SELECT_ALL = "SELECT * FROM record ";
  private static final String FIND = "SELECT * FROM record WHERE (source LIKE ?) OR (username like ?) OR (password like ?) OR (notes like ?) ";
  private static final String FIND_BY_FIELD = "SELECT * FROM record WHERE ? LIKE ? ";
  private static final String SAVE = "UPDATE record SET source = ?, username = ?, password = ?, notes = ? where id = ?";
  private static final String INSERT = "INSERT INTO record (source, username, password, notes) VALUES (?, ?, ?, ?)";
  private static final String DELETE = "DELETE FROM record WHERE ID = ?";
  private static final String ORDER_BY = "ORDER BY ";
  private static final char WC = '%';
  private static final String SELECT_MAX = "SELECT MAX(ID) FROM RECORD";
  private final @NonNull Connection connection;
  
  private DerbyRecordDao(ConnectionSource source) {
    connection = source.getConnection();
  }

  private DerbyRecordDao launch() {
    try {
      createTableIfNeeded();
    } catch (SQLException e) {
      throw new IllegalStateException(e);
    }
    return this;
  }
  
  public static DerbyRecordDao create(ConnectionSource source) {
    return new DerbyRecordDao(source).launch();
  }

  @Override
  public boolean createTableIfNeeded() throws SQLException {
    assert connection != null;
    try {
      connection.prepareStatement(SELECT_ALL + "id");
    } catch (SQLException e) {
      if (Objects.toString(e.getMessage()).contains("does not exist.")) {
        createTable(connection);
      }
      return false;
    }
    return true;
  }

  private void createTable(Connection c) throws SQLException {
    PreparedStatement statement = c.prepareStatement(CREATE_TABLE);
    statement.execute();
  }

  /**
   * using setObject() for the orderBy value gives this error message:
   *   "There is a ? parameter in the select list.  This is not allowed."
   * So we set the ORDER BY value (if present) in this method.
   * @param sql The query's sql, without the orderBy clause
   * @param orderBy The field to order by, which may be null
   * @return the sql String, with the orderBy clause if needed.
   */
  private String sqlWithOrder(String sql, @Nullable RecordField orderBy) {
    return (orderBy == null) ? sql : (sql + ORDER_BY + orderBy);
  }


  @Override
  public Collection<Record> getAll(final @Nullable RecordField orderBy) throws SQLException {
    // using setObject() for the orderBy value gives this error message:
    //   "There is a ? parameter in the select list.  This is not allowed."
    final String sql = sqlWithOrder(SELECT_ALL, orderBy);
    PreparedStatement statement = connection.prepareStatement(sql);
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
        aRecord.setSource(Objects.toString(resultSet.getString(2), ""));
        aRecord.setUserName(Objects.toString(resultSet.getString(3), ""));
        aRecord.setPassword(Objects.toString(resultSet.getString(4), ""));
        aRecord.setNotes(Objects.toString(resultSet.getString(5), ""));
        rowIsValid = resultSet.next();
        recordList.add(aRecord);
      }
      return recordList;
    }
  }

  @Override
  public Collection<Record> find(final String text, final @Nullable RecordField orderBy) throws SQLException {
    // using setObject() for the orderBy value gives this error message:
    //   "There is a ? parameter in the select list.  This is not allowed."
    final String sql = sqlWithOrder(FIND, orderBy);
    PreparedStatement statement = connection.prepareStatement(sql);
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
  public Collection<Record> findInField(final String text, final @NonNull RecordField fieldName, final @Nullable RecordField orderBy) throws SQLException {
    // using setObject() for the orderBy value gives this error message:
    //   "There is a ? parameter in the select list.  This is not allowed."
    final String sql = sqlWithOrder(FIND_BY_FIELD, orderBy);
//    System.out.println("Find By Field: " + sql);
    PreparedStatement statement = connection.prepareStatement(sql);
    //noinspection StringConcatenationMissingWhitespace
    String wildCardText = WC + text + WC;
    statement.setObject(1, fieldName.toString());
    statement.setObject(2, wildCardText);
    return extractRecords(statement);
  }

  @Override
  public Collection<Record> findAny(final @Nullable RecordField orderBy, final String... text) throws SQLException {
    throw new AssertionError("Not yet written");
  }

  @Override
  public Collection<Record> findAll(final @Nullable RecordField orderBy, final String... text) throws SQLException {
    throw new AssertionError("Not yet written");
  }

  @Override
  public Collection<Record> findAnyInField(final @NonNull RecordField findBy, final @Nullable RecordField orderBy, final String... text) throws SQLException {
    throw new AssertionError("Not yet written");
  }

  @Override
  public Collection<Record> findAllInField(final @NonNull RecordField findBy, final @Nullable RecordField orderBy, final String... text) throws SQLException {
    throw new AssertionError("Not yet written");
  }

  @Override
  public void update(final Record entity) throws SQLException {
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
  
  @Override
  public void insert(final Record entity) throws SQLException {
    doInsert(entity);
    connection.commit();
  }

  @Override
  public void insertOrUpdate(final Record entity) throws SQLException {
    int id = entity.getId();
    if (id == 0) {
      insert(entity);
    } else {
      update(entity);
    }
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
    PreparedStatement statement = connection.prepareStatement(SELECT_MAX);
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
   * @param <T> I don't know
   * @return The table info, as a collection of something
   * @throws SQLException duh
   */
//  @Override
  @SuppressWarnings({"HardCodedStringLiteral", "resource", "JDBCResourceOpenedButNotSafelyClosed"})
  <T> Collection<T> getTableInfo() throws SQLException {
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
    //noinspection UseOfSystemOutOrSystemErr
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
}
