package com.neptunedreams.skeleton.data.sqlite;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import com.neptunedreams.skeleton.data.ConnectionSource;
import com.neptunedreams.skeleton.data.Dao;
import com.neptunedreams.skeleton.data.RecordField;
import com.neptunedreams.skeleton.data2.DefaultSchema;
import com.neptunedreams.skeleton.data2.tables.Record;
import com.neptunedreams.skeleton.data2.tables.SqliteSequence;
import com.neptunedreams.skeleton.data2.tables.records.RecordRecord;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jooq.Constraint;
import org.jooq.CreateSchemaFinalStep;
import org.jooq.CreateTableAsStep;
import org.jooq.CreateTableColumnStep;
import org.jooq.DSLContext;
import org.jooq.Name;
import org.jooq.Query;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.Schema;
import org.jooq.SelectConditionStep;
import org.jooq.SelectSeekStep1;
import org.jooq.SelectSelectStep;
import org.jooq.SelectWhereStep;
import org.jooq.TableField;
import org.jooq.exception.DetachedException;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.util.sqlite.SQLiteDSL;

import static com.neptunedreams.skeleton.data2.Tables.RECORD;
import static org.jooq.SQLDialect.SQLITE;
import static org.jooq.impl.DSL.*;

/**
 * Create statement: 
 * 
 * CREATE TABLE record (
 *    id          INTEGER        NOT NULL PRIMARY KEY AUTOINCREMENT,
 *    source      VARCHAR (256)  NOT NULL,
 *    username    VARCHAR (256)  NOT NULL,
 *    password    VARCHAR (256)  NOT NULL,
 *    notes       [LONG VARCHAR] NOT NULL
 * );
 * 
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/29/17
 * <p>Time: 1:03 AM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings({"StringConcatenation", "SqlResolve", "StringConcatenationMissingWhitespace", "HardCodedStringLiteral"})
public class SQLiteRecordDao implements Dao<RecordRecord, Integer> {

  private static final int LENGTH = 256;
  
  private static final Map<RecordField, TableField<RecordRecord, ?>> fieldMap = makeFieldMap();
  private final ConnectionSource connectionSource;
  private Connection connection;

  private static Map<RecordField, TableField<RecordRecord, ?>> makeFieldMap() {
    final Map<RecordField, TableField<RecordRecord, ?>> fieldMap = new EnumMap<>(RecordField.class);
    fieldMap.put(RecordField.ID,       Record.RECORD.ID);
    fieldMap.put(RecordField.SOURCE,   Record.RECORD.SOURCE);
    fieldMap.put(RecordField.USERNAME, Record.RECORD.USERNAME);
    fieldMap.put(RecordField.PASSWORD, Record.RECORD.PASSWORD);
    fieldMap.put(RecordField.NOTES,    Record.RECORD.NOTES);
    return fieldMap;
  }

  private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS record (" +
      "id INTEGER NOT NULL PRIMARY KEY," + // AUTOINCREMENT," +
      "source VARCHAR(256) NOT NULL," +
      "username VARCHAR(256) NOT NULL," +
      "password VARCHAR(256) NOT NULL," +
      "notes LONG VARCHAR NOT NULL" +
      ')';
//  private static final String SELECT_ALL = "SELECT * FROM record ORDER BY ";
//  private static final String FIND = "SELECT * FROM record WHERE (source LIKE ?) OR (username like ?) OR (password like ?) OR (notes like ?) ORDER BY ";
//  private static final String FIND_BY_FIELD = "SELECT * FROM record WHERE ? LIKE ? ORDER BY ";
//  private static final String SAVE = "UPDATE record SET source = ?, username = ?, password = ?, notes = ? where id = ?";
//  private static final String INSERT = "INSERT INTO record (source, username, password, notes) VALUES (?, ?, ?, ?)";
//  private static final String DELETE = "DELETE FROM record WHERE ID = ?";
  private static final char WC = '%';
//  private final DSLContext dslContext;
  private DSLContext getDslContext() throws SQLException {
    if (connection.isClosed()) {
      connection = connectionSource.getConnection();
    }
    return DSL.using(connection, SQLITE);
  }

  SQLiteRecordDao(ConnectionSource source) {
    connectionSource = source;
    connection = source.getConnection();
//    dslContext = DSL.using(connection, SQLITE);
  }
  
  @Override
  public boolean createTableIfNeeded() throws SQLException {
    /* All my efforts to generate the table using jOOQ failed, so I had to resort to direct SQL. */

    DSLContext dslContext = getDslContext();
    System.out.printf("DSLContext of %s%n", dslContext.getClass());
    dslContext.execute(CREATE_TABLE);
    DefaultSchema schema = DefaultSchema.DEFAULT_SCHEMA;
//    dslContext.createSchemaIfNotExists("skeleton").execute();
//    Name recordName = new 
    Query[] queries = dslContext.ddl(schema).queries();
    System.out.printf("Total of %d queries.%n", queries.length);
    for (Query q: queries) {
      System.out.printf("Query: %s%n", q);
//      if (!q.toString().contains("sqlite_sequence")) {
//        q.execute();
//      }
    }
    
//    dslContext.alterTable("record").add(constraint("autoincrement").).execute();

//    final CreateTableAsStep<org.jooq.Record> table;
////    dslContext.
//    table = dslContext.createTableIfNotExists(RECORD);
////    table.
////    connection.commit();
////    dslContext.
//    
////    try (
////      CreateSchemaFinalStep skeleton = dslContext.createSchemaIfNotExists("skeleton")) {
////      skeleton.execute();
////      table = dslContext.createTableIfNotExists(RECORD);
////    }
//
////    try (
//        CreateTableColumnStep idColumn = table.column(RECORD.ID, SQLDataType.INTEGER.nullable(false).identity(true)); //) {
//      //noinspection resource
//      idColumn.column(RECORD.SOURCE, SQLDataType.VARCHAR(LENGTH).nullable(false))
//          .column(RECORD.USERNAME, SQLDataType.VARCHAR(LENGTH).nullable(false))
//          .column(RECORD.PASSWORD, SQLDataType.VARCHAR(LENGTH).nullable(false))
//          .column(RECORD.NOTES, SQLDataType.LONGNVARCHAR.nullable(false));
//      idColumn.execute();
////    connection.commit();
////    }
    return true;
  }

  @Override
  public Collection<RecordRecord> getAll(final RecordField orderBy) throws SQLException {

    DSLContext dslContext = getDslContext();
    try (
        SelectWhereStep<RecordRecord> recordRecords = dslContext.selectFrom(RECORD)) {
      return recordRecords.fetch();
    }
  }

  @Override
  public Collection<RecordRecord> find(final String text, final RecordField orderBy) throws SQLException {
    final String wildCardText = WC + text + WC;
    DSLContext dslContext = getDslContext();
    try (
      final SelectWhereStep<RecordRecord> recordRecords = dslContext.selectFrom(RECORD);
      SelectConditionStep<RecordRecord> where = recordRecords.where(
          RECORD.SOURCE.like(wildCardText).or(
          RECORD.USERNAME.like(wildCardText).or(
          RECORD.PASSWORD.like(wildCardText).or(
          RECORD.NOTES.like(wildCardText)))));
      final SelectSeekStep1<RecordRecord, ?> step = where.orderBy(fieldMap.get(orderBy))
    ) {
      return step.fetch();
    }
  }

  @Override
  public Collection<RecordRecord> findInField(final String text, final @NonNull RecordField field, final @Nullable RecordField orderBy) throws SQLException {
    String wildCardText = WC + text + WC;
    DSLContext dslContext = getDslContext();
    try (
        final SelectWhereStep<RecordRecord> recordRecords = dslContext.selectFrom(RECORD);
        final SelectConditionStep<RecordRecord> where = recordRecords.where(fieldMap.get(orderBy).like(wildCardText))
    ) {
      if (orderBy != null) {
        //noinspection NestedTryStatement
        try (SelectSeekStep1<RecordRecord, ?> step = where.orderBy(fieldMap.get(orderBy))) {
          return step.fetch();
        }
      } else {
        return where.fetch();
      }
    }
         
//         final String sql = FIND_BY_FIELD + orderBy;
////    System.out.println("Find By Field: " + sql);
//    PreparedStatement statement = connection.prepareStatement(sql);
//    //noinspection StringConcatenationMissingWhitespace
//    statement.setObject(1, fieldName.toString());
//    statement.setObject(2, wildCardText);
//    return extractRecords(statement);
  }

  @Override
  public void save(final RecordRecord entity) throws SQLException {
    entity.store();
  }
  
  @Override
  public void insert(final RecordRecord entity) throws SQLException {
    DSLContext dslContext = getDslContext();
//    Record1 record1 = dslContext.select(max(Record.RECORD.ID)).from(Record.RECORD).fetchOne();
//    System.out.printf("Max: %s%n", record1);
//    Integer max = ((Integer)(record1.get("max")));
//    int id = (max == null) ? 1 : (max + 1);
//
//    System.out.printf("Inserting into Record using id %d%n", id);
//    entity.setId(id);

//    dslContext.attach(entity);
    dslContext.insertInto(RECORD)
        .set(RECORD.SOURCE, entity.getSource())
        .set(RECORD.USERNAME, entity.getUsername())
        .set(RECORD.PASSWORD, entity.getPassword())
        .set(RECORD.NOTES, entity.getNotes())
        .execute();
    Result<?> result = dslContext.fetch("SELECT last_insert_rowId()");
    System.out.println(result);
    int id = (Integer) result.getValue(0, "last_insert_rowId()");
    entity.setId(id);
    
//    entity.insert();
    System.out.printf("Inserted record has id %d%n", entity.getId());
  }

  @Override
  public void delete(final RecordRecord entity) throws SQLException {
    entity.delete();
  }
  
  @Override
  public Integer getNextId() throws SQLException {

    DSLContext dslContext = getDslContext();
    try (
        final SelectSelectStep<Record1<Integer>> select = dslContext.select(max(Record.RECORD.ID))
    ) {
      Result<Record1<Integer>> result = select.fetch();
      return result.get(0).getValue(1, Integer.class); // I'm guessing that Result (a List) is zero-based, but Record1 is one-based.
    }
  }

  @Override
  public Integer getPrimaryKey(final RecordRecord entity) {
    return entity.getId();
  }

  @Override
  public void setPrimaryKey(final RecordRecord entity, final Integer primaryKey) {
    entity.setId(primaryKey);
  }

//  /**
//   * This is an attempt (ultimately successful) to fix the sql statement that fails during code generation.
//   * @param <T>
//   * @return
//   * @throws SQLException
//   */
////  @Override
//  public <T> Collection<T> getTableInfo() throws SQLException {
//    return null;
//  }
}
