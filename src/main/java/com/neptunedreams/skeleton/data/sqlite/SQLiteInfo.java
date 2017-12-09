package com.neptunedreams.skeleton.data.sqlite;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import com.neptunedreams.skeleton.data.AbstractDatabaseInfo;
import com.neptunedreams.skeleton.data.ConnectionSource;
import com.neptunedreams.skeleton.data.Dao;
import com.neptunedreams.skeleton.gen.DefaultSchema;
import com.neptunedreams.skeleton.gen.tables.records.RecordRecord;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

import static org.jooq.SQLDialect.SQLITE;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/10/17
 * <p>Time: 11:31 PM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings({"StringConcatenation", "HardCodedStringLiteral"})
public class SQLiteInfo extends AbstractDatabaseInfo {

  private static final Class<RecordRecord> RECORD_RECORD_CLASS = RecordRecord.class;

  public SQLiteInfo() throws SQLException, IOException {
    this("/.sqlite.skeleton");
  }
  
  public SQLiteInfo(String homeDir) throws IOException, SQLException {
    super(homeDir);
//    init();
  }
  @Override
  public String getUrl() {
    //noinspection StringConcatenationMissingWhitespace
    return "jdbc:sqlite:" + getHomeDir() + getHome();
  }

  @Override
  public <T, PK> Dao<T, PK> getDao(final Class<T> entityClass, final ConnectionSource source) {
    //noinspection EqualityOperatorComparesObjects
    if (entityClass == RECORD_RECORD_CLASS) {
      //noinspection unchecked
      return (Dao<T, PK>) new SQLiteRecordDao(source);
    }
    throw new IllegalArgumentException(String.valueOf(entityClass));
  }

  @Override
  public Class<?> getRecordClass() {
    return RECORD_RECORD_CLASS;
  }

  private String getHome() {
    //noinspection HardcodedFileSeparator
    return "/skeleton.db";
  }

  @Override
  public void init() throws IOException, SQLException {
    File homeDir = new File(getHomeDir());
    File databaseFile = new File(homeDir, getHome());
    if (!databaseFile.exists()) {
      boolean failed = !databaseFile.createNewFile();
      if (failed) {
        throw new IOException("Failed to create database file at " + databaseFile.getAbsolutePath());
      }
    }
    initialize();
  }

  @Override
  public boolean isCreateSchemaAllowed() {
    return true;
  }

  @Override
  public void createSchema() {
    DSLContext dslContext = DSL.using(getConnectionSource().getConnection(), SQLITE);
    System.out.printf("DSLContext of %s%n", dslContext.getClass());
    DefaultSchema schema = DefaultSchema.DEFAULT_SCHEMA;
//    dslContext.createSchemaIfNotExists("skeleton").execute();
//    Name recordName = new 
    Query[] queries = dslContext.ddl(schema).queries();
    System.out.printf("Total of %d queries.%n", queries.length);
    for (Query q : queries) {
      System.out.printf("Query: %s%n", q);
      if (q.toString().contains("sqlite_sequence")) {
        System.out.println(("(Not Executed)\n"));
      } else {
        try {
          q.execute();
        } catch (DataAccessException e) {
          if (!Objects.toString(e.getMessage()).contains("table record already exists")) {
            throw e;
          }
        }
      }
    }
  }
}
