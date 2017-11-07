package com.neptunedreams.skeleton.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import com.neptunedreams.skeleton.ConnectionSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/29/17
 * <p>Time: 1:03 AM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings({"StringConcatenation", "SqlResolve", "StringConcatenationMissingWhitespace"})
public class RecordDao implements Dao<Record> {
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
  
  public RecordDao(ConnectionSource source) {
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
  public Collection<Record> getAll(final Enum orderBy) throws SQLException {
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
  public Collection<Record> find(final String text, final Enum orderBy) throws SQLException {
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
  public Collection<Record> findInField(final String text, @NotNull final Enum fieldName, @Nullable final Enum orderBy) throws SQLException {
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
  public int getNextId() throws SQLException {
    String sql = "SELECT MAX(ID) FROM RECORD";
    PreparedStatement statement = connection.prepareStatement(sql);
    try (ResultSet resultSet = statement.executeQuery()) {
      resultSet.next();
      return resultSet.getInt(1) + 1;
    }
  }

  @Override
  public int getNewId(final Record entity) throws SQLException {
    String sql = "SELECT * FROM record WHERE source = ? and username = ? and password = ?"; //NON-NLS
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setObject(1, entity.getSource());
    statement.setObject(2, entity.getUserName());
    statement.setObject(3, entity.getPassword());
    try (ResultSet resultSet = statement.executeQuery()) {
      int highestId = Integer.MIN_VALUE;
      boolean hasNext = resultSet.next();
      while (hasNext) {
        int id = resultSet.getInt(1);
        if (id > highestId) {
          highestId = id;
        }
        hasNext = resultSet.next();
      }
      return highestId;
    }
  }
}
