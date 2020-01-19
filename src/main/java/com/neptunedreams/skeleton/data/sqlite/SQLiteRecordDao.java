package com.neptunedreams.skeleton.data.sqlite;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import com.neptunedreams.framework.data.ConnectionSource;
import com.neptunedreams.framework.data.Dao;
import com.neptunedreams.skeleton.data.SiteField;
import com.neptunedreams.skeleton.gen.Tables;
import com.neptunedreams.skeleton.gen.tables.Site;
import com.neptunedreams.skeleton.gen.tables.records.SiteRecord;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.ResultQuery;
import org.jooq.SelectConditionStep;
import org.jooq.SelectSeekStep1;
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
 * CREATE TABLE IF NOT EXISTS site (
 *    id          INTEGER        NOT NULL PRIMARY KEY,
 *    source      VARCHAR (256)  NOT NULL collate noCase,
 *    username    VARCHAR (256)  NOT NULL collate noCase,
 *    password    VARCHAR (256)  NOT NULL collate noCase,
 *    notes       LONG VARCHAR   NOT NULL collate noCase
 * );
 * 
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/29/17
 * <p>Time: 1:03 AM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings({"StringConcatenation", "SqlResolve", "StringConcatenationMissingWhitespace", "HardCodedStringLiteral"})
public final class SQLiteRecordDao implements Dao<SiteRecord, Integer, SiteField> {

  private static final Map<SiteField, @NonNull TableField<SiteRecord, ?>> fieldMap = makeFieldMap();
  private final ConnectionSource connectionSource;
  private @NonNull Connection connection;

  private static Map<SiteField, @NonNull TableField<SiteRecord, ?>> makeFieldMap() {
    final EnumMap<SiteField, @NonNull TableField<SiteRecord, ?>> fieldMap = new EnumMap<>(SiteField.class);
    fieldMap.put(SiteField.ID,       Site.SITE.ID);
    fieldMap.put(SiteField.Source,   Site.SITE.SOURCE);
    fieldMap.put(SiteField.Username, Site.SITE.USERNAME);
    fieldMap.put(SiteField.Password, Site.SITE.PASSWORD);
    fieldMap.put(SiteField.Notes,    Site.SITE.NOTES);
    return fieldMap;
  }

  // If you change the CREATE statement, you need to change it two other places. First, you should change the comment
  // at the beginning of this file. But more important, you should delete the master database 
  // at src/main/resources/sql/generateFromSkeleton.db anc re-create it using the revised CREATE statement.
  // Also, this statement specifies the primary key as a property, instead of as a constraint at the end of the 
  // statement. This is necessary so a null id will cause the database to generate a new valid id. If it's specified
  // in a CONSTRAINT clause, a null id will throw an exception instead. In fact, even if I specify the collate noCase
  // constraints as named constraints, a null ID will still throw an exception.
  private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS site (" +
      "id INTEGER NOT NULL PRIMARY KEY," + 
      "source VARCHAR(256) NOT NULL collate noCase," +
      "username VARCHAR(256) NOT NULL collate noCase," +
      "password VARCHAR(256) NOT NULL collate noCase," +
      "notes LONG VARCHAR NOT NULL collate noCase" +
      ')';
  private static final char WC = '%';
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
  }
  
  private SQLiteRecordDao launch() {
    try {
      createTableIfNeeded();
    } catch (SQLException e) {
      throw new IllegalStateException(e);
    }
    return this;
  }

  @SuppressWarnings("JavaDoc")
  static SQLiteRecordDao create(ConnectionSource source) {
    return new SQLiteRecordDao(source).launch();
  }

  @Override
  public boolean createTableIfNeeded() throws SQLException {
    /* All my efforts to generate the table using jOOQ failed, so I had to resort to direct SQL. */

    DSLContext dslContext = getDslContext();

    // Creates the table using the sql statement
    dslContext.execute(CREATE_TABLE);
    
    // Neither of the following two code blocks works, so I needed to use my CREATE_TABLE String to create the table I needed.
//    dslContext.ddl(Tables.SITE).executeBatch();

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
    
    // Here's what fails when I try to use jOOQ to create my table:
    // 1. The created table does not have collate noCase for each text field. This means my sorting will be
    //    case-sensitive, which I hate.
    // 2. The create statement looks has the primary key specified as a constraint, rather than a property, like this:
    //       create table if not exists site (id integer NOT NULL, ... CONSTRAINT pk_site primary key(id));
    //    instead of this:
    //       create table if not exists site (id integer NOT NULL primary key, ... );
    //    I don't see why this should make a difference, but it does. The effect is that when I specify null for 
    //    the ID, the first case will throw an SQLiteException with this message: 
    //    A NOT NULL constraint failed (NOT NULL constraint failed: site.id). The second case will work because the 
    //    database generates a valid id. I don't know why they behave differently.
    
    return true;
  }

  @Override
  public Collection<SiteRecord> getAll(final @Nullable SiteField orderBy) throws SQLException {

    DSLContext dslContext = getDslContext();
    try (SelectWhereStep<SiteRecord> siteRecords = dslContext.selectFrom(SITE)) {
      if (orderBy == null) {
        return siteRecords.fetch();
      } else {
        return getOrderedSiteRecords(orderBy, siteRecords);
      }
    }
  }

  private Collection<SiteRecord> getOrderedSiteRecords(final @NonNull SiteField orderBy, final SelectWhereStep<SiteRecord> siteRecords) {
    try (final SelectSeekStep1<SiteRecord, ?> foundRecords = siteRecords.orderBy(getField(orderBy))) {
      return foundRecords.fetch();
    }
  }

  @Override
  public Collection<SiteRecord> find(final String text, final @Nullable SiteField orderBy) throws SQLException {
    final String wildCardText = wrapWithWildCards(text);

    DSLContext dslContext = getDslContext();

    try (
        final SelectWhereStep<SiteRecord> siteRecords = dslContext.selectFrom(SITE);
        SelectConditionStep<SiteRecord> where = siteRecords.where(
          SITE.SOURCE.like(wildCardText).or(
          SITE.USERNAME.like(wildCardText).or(
          SITE.PASSWORD.like(wildCardText).or(
          SITE.NOTES.like(wildCardText)))));
        final ResultQuery<SiteRecord> query = (orderBy == null) ? 
          where : 
          where.orderBy(getField(orderBy))
    ) {
      return query.fetch();
    }
  }

  /**
   * This used to cast to upper() before returning the field, to implement case-insensitive sorting. Now
   * this is done in the table definitions, this just extracts the right TableField from the fieldMap.
   * @param orderBy The orderBy field
   * @return A Field{@literal <String>} to pass to the orderBy() method to support case insensitive ordering.
   */
//  @SuppressWarnings("unchecked")
  private @NonNull TableField<SiteRecord, ?> getField(final @Nullable SiteField orderBy) {
    return Objects.requireNonNull(fieldMap.get(orderBy));
  }

  private @NonNull String wrapWithWildCards(final String text) {
    return WC + text + WC;
  }

  @Override
  public Collection<SiteRecord> findAny(final @Nullable SiteField orderBy, final String... text) throws SQLException {

    DSLContext dslContext = getDslContext();
    Condition condition = SITE.SOURCE.lt(""); // Should always be false

    try (final SelectWhereStep<SiteRecord> siteRecords = dslContext.selectFrom(SITE))
    {
      for (String txt : text) {
        String wildCardText = wrapWithWildCards(txt);
          condition = condition.or(
            SITE.SOURCE.like(wildCardText)).or(
            SITE.USERNAME.like(wildCardText)).or(
            SITE.PASSWORD.like(wildCardText)).or(
            SITE.NOTES.like(wildCardText));
      }
      return getFromQuery(orderBy, siteRecords, condition);
    }
  }

  @Override
  public Collection<SiteRecord> findAll(final @Nullable SiteField orderBy, final String... text) throws SQLException {

    DSLContext dslContext = getDslContext();
    Condition condition = SITE.SOURCE.ge(""); // Should always be true
    try (final SelectWhereStep<SiteRecord> siteRecords = dslContext.selectFrom(SITE)) {
      for (String txt : text) {
        String wildCardText = wrapWithWildCards(txt);
        condition = condition.and(
            SITE.SOURCE.like(wildCardText).or(
            SITE.USERNAME.like(wildCardText)).or(
            SITE.PASSWORD.like(wildCardText)).or(
            SITE.NOTES.like(wildCardText)));
      }
      return getFromQuery(orderBy, siteRecords, condition);
    }
  }

  @Override
  public Collection<SiteRecord> findInField(
      final String text, 
      final @NonNull SiteField findBy, 
      final @Nullable SiteField orderBy
  ) throws SQLException {
    String wildCardText = wrapWithWildCards(text);

    DSLContext dslContext = getDslContext();

    final @NonNull TableField<SiteRecord, ?> findByField = Objects.requireNonNull(fieldMap.get(findBy));
    try (
        final SelectWhereStep<SiteRecord> siteRecords = dslContext.selectFrom(SITE);
        final SelectConditionStep<SiteRecord> where = siteRecords.where((findByField.like(wildCardText)))
    ) {
      final ResultQuery<SiteRecord> query;
      query = (orderBy == null) ?
        where :
        where.orderBy(getField(orderBy));
      return query.fetch();
    }
  }

  @Override
  public Collection<SiteRecord> findAnyInField(final @NonNull SiteField findBy, final @Nullable SiteField orderBy, final String... text) throws SQLException {
    DSLContext dslContext = getDslContext();

    final @NonNull TableField<SiteRecord, ?> findByField = Objects.requireNonNull(fieldMap.get(findBy));
    Condition condition = SITE.SOURCE.lt(""); // Should always be false
    try (SelectWhereStep<SiteRecord> siteRecords = dslContext.selectFrom(SITE)) {
      for (String txt : text) {
        String wildCardText = wrapWithWildCards(txt);
        condition = condition.or(findByField.like(wildCardText));
      }
      return getFromQuery(orderBy, siteRecords, condition);
    }
  }

  @Override
  public Collection<SiteRecord> findAllInField(final @NonNull SiteField findBy, final @Nullable SiteField orderBy, final String... text) throws SQLException {

    DSLContext dslContext = getDslContext();

    final @NonNull TableField<SiteRecord, ?> findByField = Objects.requireNonNull(fieldMap.get(findBy));
    Condition condition = SITE.SOURCE.ge(""); // Should always be true
    try (SelectWhereStep<SiteRecord> siteRecords = dslContext.selectFrom(SITE)) {
      for (String txt : text) {
        String wildCardText = wrapWithWildCards(txt);
        condition = condition.and(findByField.like(wildCardText));
      }
      return getFromQuery(orderBy, siteRecords, condition);
    }
  }

  private Collection<SiteRecord> getFromQuery(
      final @Nullable SiteField orderBy,
      final SelectWhereStep<SiteRecord> siteRecords,
      final Condition condition
  ) {
    final ResultQuery<SiteRecord> query;
    try (SelectConditionStep<SiteRecord> where = siteRecords.where(condition)) {
      query = (orderBy == null) ?
          where :
          where.orderBy(getField(orderBy));
    }
    return query.fetch();
  }

  @Override
  public void update(final SiteRecord entity) { // throws SQLException {
    entity.store();
  }

  @Override
  public void insertOrUpdate(final SiteRecord entity) throws SQLException {
    final Integer id = entity.getId();
    if ((id == null) || (id == 0)) {
      insert(entity);
    } else {
      update(entity);
    }
  }

  @Override
  @SuppressWarnings("argument.type.incompatible")
  public void insert(final SiteRecord entity) throws SQLException {

    DSLContext dslContext = getDslContext();
    //noinspection ConstantConditions
    entity.setId(null); // argument.type.incompatible null assumed not allowed in generated code.
    Integer id = entity.getId(); 
    entity.setId(id);
    dslContext.attach(entity);
    entity.insert();
  }

  @Override
  public void delete(final SiteRecord entity) { // throws SQLException {
    entity.delete();
  }
  
  @Override
  public Integer getNextId() throws SQLException {

    DSLContext dslContext = getDslContext();
    try (
        final SelectSelectStep<Record1<Integer>> select = dslContext.select(max(Site.SITE.ID))
    ) {
      Result<Record1<Integer>> result = select.fetch();
      return result.get(0).getValue(1, Integer.class); // I'm guessing that Result (a List) is zero-based, but Record1 is one-based.
    }
  }

  @Override
  public Integer getPrimaryKey(final SiteRecord entity) {
    return entity.getId();
  }

  @Override
  public void setPrimaryKey(final SiteRecord entity, final Integer primaryKey) {
    entity.setId(primaryKey);
  }

  @Override
  public int getTotal() throws SQLException {
    DSLContext dslContext = getDslContext();
    return dslContext.fetchCount(Tables.SITE);
  }
}
