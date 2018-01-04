package com.neptunedreams.skeleton.data.sqlite;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import com.neptunedreams.skeleton.data.ConnectionSource;
import com.neptunedreams.skeleton.data.Dao;
import com.neptunedreams.skeleton.data.RecordField;
import com.neptunedreams.skeleton.gen.tables.Record;
import com.neptunedreams.skeleton.gen.tables.records.RecordRecord;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.ResultQuery;
import org.jooq.SelectConditionStep;
import org.jooq.SelectSelectStep;
import org.jooq.SelectWhereStep;
import org.jooq.TableField;
import org.jooq.impl.DSL;

import static com.neptunedreams.skeleton.gen.Tables.*;
import static org.jooq.SQLDialect.*;
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
public final class SQLiteRecordDao implements Dao<RecordRecord, Integer> {

  private static final Map<RecordField, @NonNull TableField<RecordRecord, ?>> fieldMap = makeFieldMap();
  private final ConnectionSource connectionSource;
  private @NonNull Connection connection;

  private static Map<RecordField, @NonNull TableField<RecordRecord, ?>> makeFieldMap() {
    final EnumMap<RecordField, @NonNull TableField<RecordRecord, ?>> fieldMap = new EnumMap<>(RecordField.class);
    fieldMap.put(RecordField.ID,       Record.RECORD.ID);
    fieldMap.put(RecordField.Source,   Record.RECORD.SOURCE);
    fieldMap.put(RecordField.Username, Record.RECORD.USERNAME);
    fieldMap.put(RecordField.Password, Record.RECORD.PASSWORD);
    fieldMap.put(RecordField.Notes,    Record.RECORD.NOTES);
    return fieldMap;
  }

  private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS record (" +
      "id INTEGER NOT NULL PRIMARY KEY," + // AUTOINCREMENT," +
      "source VARCHAR(256) NOT NULL collate noCase," +
      "username VARCHAR(256) NOT NULL collate noCase," +
      "password VARCHAR(256) NOT NULL collate noCase," +
      "notes LONG VARCHAR NOT NULL collate noCase" +
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
    assert connection != null;
    assert connectionSource != null;
    if (connection.isClosed()) {
      connection = connectionSource.getConnection();
    }
    return DSL.using(connection, SQLITE);
  }

  private SQLiteRecordDao(ConnectionSource source) {
    connectionSource = source;
    connection = source.getConnection();
//    dslContext = DSL.using(connection, SQLITE);
  }
  
  private SQLiteRecordDao launch() {
    try {
      createTableIfNeeded();
    } catch (SQLException e) {
      throw new IllegalStateException(e);
    }
    return this;
  }
  
  static SQLiteRecordDao create(ConnectionSource source) {
    return new SQLiteRecordDao(source).launch();
  }

  @Override
  public boolean createTableIfNeeded() throws SQLException {
    /* All my efforts to generate the table using jOOQ failed, so I had to resort to direct SQL. */

    //noinspection resource
    DSLContext dslContext = getDslContext();
    dslContext.execute(CREATE_TABLE);
//    DefaultSchema schema = DefaultSchema.DEFAULT_SCHEMA;
//    dslContext.createSchemaIfNotExists("skeleton").execute();
//    Name recordName = new 
//    Query[] queries = dslContext.ddl(schema).queries();
//    System.out.printf("Total of %d queries.%n", queries.length);
//    for (Query q: queries) {
//      System.out.printf("Query: %s%n", q);
////      if (!q.toString().contains("sqlite_sequence")) {
////        q.execute();
////      }
//    }
    
    return true;
  }

  @Override
  public Collection<RecordRecord> getAll(final @Nullable RecordField orderBy) throws SQLException {

    //noinspection resource
    DSLContext dslContext = getDslContext();
    try (
        SelectWhereStep<RecordRecord> recordRecords = dslContext.selectFrom(RECORD)) {
      if (orderBy == null) {
        return recordRecords.fetch();
      } else {
        //noinspection resource
        return recordRecords.orderBy(castOrderToUpper(orderBy)).fetch();
      }
    }
  }

  @Override
  @SuppressWarnings("cast.unsafe")
  public Collection<RecordRecord> find(final String text, final @Nullable RecordField orderBy) throws SQLException {
    final String wildCardText = wrapWithWildCards(text);
    //noinspection resource
    DSLContext dslContext = getDslContext();
    //noinspection resource
    try (
        final SelectWhereStep<RecordRecord> recordRecords = dslContext.selectFrom(RECORD);
        SelectConditionStep<RecordRecord> where = recordRecords.where(
          RECORD.SOURCE.like(wildCardText).or(
          RECORD.USERNAME.like(wildCardText).or(
          RECORD.PASSWORD.like(wildCardText).or(
          RECORD.NOTES.like(wildCardText)))));
        final ResultQuery<RecordRecord> query = (orderBy == null) ? 
          where : 
          where.orderBy(castOrderToUpper(orderBy)) // unsafe cast
    ) {
      return query.fetch();
    }
  }

  /**
   * The proper way to do a cast-insensitive sort is by saying "ORDER BY xxx COLLATE NOCASE", but there's no method
   * call to do this in jOOQ. So instead, we cast the order to upper case, which is probably slower, but it works.
   * However, I may be able to do this by adding COLLATE NOCASE to the initial table create statement, which will allow
   * me to remove the upper() call from this method after rebuilding the table.
   * @param orderBy The orderBy field
   * @return A Field{@literal <String>} to pass to the orderBy() method to support case insensitive ordering.
   */
  @SuppressWarnings("unchecked")
  private @NonNull Field<?> castOrderToUpper(final @Nullable RecordField orderBy) {
    // Only integer field
    if (orderBy == RecordField.ID) {
      return fieldMap.get(RecordField.ID);
    }
    // unchecked cast to Field<String> is safe because all the remaining orderBy values are for text fields
    return upper((Field<String>) Objects.requireNonNull(fieldMap.get(orderBy)));
  }

  private @NonNull String wrapWithWildCards(final String text) {
    return WC + text + WC;
  }

  @Override
  public Collection<RecordRecord> findAny(final @Nullable RecordField orderBy, final String... text) throws SQLException {
    //noinspection resource
    DSLContext dslContext = getDslContext();
    Condition condition = RECORD.SOURCE.lt(""); // Should always be false
    //noinspection resource
    try (final SelectWhereStep<RecordRecord> recordRecords = dslContext.selectFrom(RECORD))
    {
      for (String txt : text) {
        String wildCardText = wrapWithWildCards(txt);
          condition = condition.or(
            RECORD.SOURCE.like(wildCardText)).or(
            RECORD.USERNAME.like(wildCardText)).or(
            RECORD.PASSWORD.like(wildCardText)).or(
            RECORD.NOTES.like(wildCardText));
      }
      return getFromQuery(orderBy, recordRecords, condition);
    }
  }

  @Override
  public Collection<RecordRecord> findAll(final @Nullable RecordField orderBy, final String... text) throws SQLException {
    //noinspection resource
    DSLContext dslContext = getDslContext();
    Condition condition = RECORD.SOURCE.ge(""); // Should always be true
    try (final SelectWhereStep<RecordRecord> recordRecords = dslContext.selectFrom(RECORD)) {
      for (String txt : text) {
        String wildCardText = wrapWithWildCards(txt);
        condition = condition.and(
            RECORD.SOURCE.like(wildCardText).or(
            RECORD.USERNAME.like(wildCardText)).or(
            RECORD.PASSWORD.like(wildCardText)).or(
            RECORD.NOTES.like(wildCardText)));
      }
      return getFromQuery(orderBy, recordRecords, condition);
    }
  }

  @Override
  @SuppressWarnings("cast.unsafe")
  public Collection<RecordRecord> findInField(
      final String text, 
      final @NonNull RecordField findBy, 
      final @Nullable RecordField orderBy
  ) throws SQLException {
    String wildCardText = wrapWithWildCards(text);
    //noinspection resource
    DSLContext dslContext = getDslContext();

    final @NonNull TableField<RecordRecord, ?> findByField = Objects.requireNonNull(fieldMap.get(findBy));
    try (
        final SelectWhereStep<RecordRecord> recordRecords = dslContext.selectFrom(RECORD);
        final SelectConditionStep<RecordRecord> where = recordRecords.where((findByField.like(wildCardText)))
    ) {
      final ResultQuery<RecordRecord> query;
      query = (orderBy == null) ?
        where :
        where.orderBy(castOrderToUpper(orderBy));
      return query.fetch();
    }
  }

  @Override
  public Collection<RecordRecord> findAnyInField(final @NonNull RecordField findBy, final @Nullable RecordField orderBy, final String... text) throws SQLException {
    //noinspection resource
    DSLContext dslContext = getDslContext();

    final @NonNull TableField<RecordRecord, ?> findByField = Objects.requireNonNull(fieldMap.get(findBy));
    Condition condition = RECORD.SOURCE.lt(""); // Should always be false
    try (SelectWhereStep<RecordRecord> recordRecords = dslContext.selectFrom(RECORD)) {
      for (String txt : text) {
        String wildCardText = wrapWithWildCards(txt);
        condition = condition.or(findByField.like(wildCardText));
      }
      return getFromQuery(orderBy, recordRecords, condition);
    }
  }

  @Override
  public Collection<RecordRecord> findAllInField(final @NonNull RecordField findBy, final @Nullable RecordField orderBy, final String... text) throws SQLException {
    //noinspection resource
    DSLContext dslContext = getDslContext();

    final @NonNull TableField<RecordRecord, ?> findByField = Objects.requireNonNull(fieldMap.get(findBy));
    Condition condition = RECORD.SOURCE.ge(""); // Should always be true
    try (SelectWhereStep<RecordRecord> recordRecords = dslContext.selectFrom(RECORD)) {
      for (String txt : text) {
        String wildCardText = wrapWithWildCards(txt);
        condition = condition.and(findByField.like(wildCardText));
      }
      return getFromQuery(orderBy, recordRecords, condition);
    }
  }

  private Collection<RecordRecord> getFromQuery(
      final @Nullable RecordField orderBy,
      final SelectWhereStep<RecordRecord> recordRecords,
      final Condition condition
  ) {
    final ResultQuery<RecordRecord> query;
    try (SelectConditionStep<RecordRecord> where = recordRecords.where(condition)) {
      query = (orderBy == null) ?
          where :
          where.orderBy(castOrderToUpper(orderBy));
    }
    return query.fetch();
  }

  @Override
  public void update(final RecordRecord entity) throws SQLException {
    entity.store();
  }

  @Override
  public void insertOrUpdate(final RecordRecord entity) throws SQLException {
    final Integer id = entity.getId();
    if ((id == null) || (id == 0)) {
//      System.out.printf("Saving entity (id=%s) with insert%n", id);
      insert(entity);
    } else {
//      System.out.printf("Saving entity (id=%s) with UPDATE%n", id);
      update(entity);
    }
  }

  @Override
  @SuppressWarnings("argument.type.incompatible")
  public void insert(final RecordRecord entity) throws SQLException {
    //noinspection resource
    DSLContext dslContext = getDslContext();
//    Record1 record1 = dslContext.select(max(Record.RECORD.ID)).from(Record.RECORD).fetchOne();
//    System.out.printf("Max: %s%n", record1);
//    Integer max = ((Integer)(record1.get("max")));
//    int id = (max == null) ? 1 : (max + 1);
    //noinspection ConstantConditions
    entity.setId(null); // argument.type.incompatible null assumed not allowed in generated code.
    Integer id = entity.getId(); 
//    System.out.printf("Inserting into Record using id %d%n", id);
    entity.setId(id);
    dslContext.attach(entity);
    entity.insert();
//    System.out.printf("Inserted record has id %d%n", entity.getId());
//    dslContext.insertInto(RECORD)
//        .set(RECORD.SOURCE, entity.getSource())
//        .set(RECORD.USERNAME, entity.getUsername())
//        .set(RECORD.PASSWORD, entity.getPassword())
//        .set(RECORD.NOTES, entity.getNotes())
//        .execute();
//    Result<?> result = dslContext.fetch("SELECT last_insert_rowId()");
//    System.out.println(result);
//    int id = (Integer) result.getValue(0, "last_insert_rowId()");
//    entity.setId(id);
//    
  }

  @Override
  public void delete(final RecordRecord entity) throws SQLException {
    entity.delete();
  }
  
  @Override
  public Integer getNextId() throws SQLException {

    //noinspection resource
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
