package com.neptunedreams.skeleton.data;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
//import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
import org.checkerframework.checker.nullness.qual.Nullable;
//import org.checkerframework.checker.nullness.qual.RequiresNonNull;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/12/17
 * <p>Time: 12:10 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public abstract class AbstractDatabaseInfo implements DatabaseInfo {
  private @Nullable ConnectionSource connectionSource;
  private final String homeDirectory;
  
  @SuppressWarnings("JavaDoc")
  protected AbstractDatabaseInfo(String homeDir) { // throws IOException {
    String userHome = System.getProperty("user.home");
    //noinspection StringConcatenation,StringConcatenationMissingWhitespace
    homeDirectory = userHome + homeDir;
    ensureHomeExists(homeDirectory);
  }

  @Override
  public ConnectionSource getConnectionSource() {
    if (connectionSource == null) {
      throw new IllegalStateException("initialize() must be called before calling getConnectionSource()");
    }
    return connectionSource;
  }

  private ConnectionSource connect() throws SQLException {
    String connectionUrl = getUrl();
    //noinspection UseOfSystemOutOrSystemErr
//    System.out.printf("URL: %s%n", connectionUrl);
    //noinspection CallToDriverManagerGetConnection,JDBCResourceOpenedButNotSafelyClosed
    Connection connection = DriverManager.getConnection(connectionUrl);
    return () -> connection;
//    Connection wrapped = new ConnectionWrapper(connection);
//      return () -> wrapped;
  }

  @Override
  public final String getHomeDir() {
    return homeDirectory;
  }

  @SuppressWarnings("JavaDoc")
  @EnsuresNonNull("connectionSource")
  protected void initialize() throws SQLException {
    connectionSource = connect();
  }

  @SuppressWarnings("HardCodedStringLiteral")
  private void ensureHomeExists(@UnderInitialization AbstractDatabaseInfo this, String databaseHome) { //throws IOException {
//    System.setProperty(DERBY_SYSTEM_HOME, databaseHome);
//    final String databaseHome = System.getProperty("derby.system.home");
//    final String databaseHome = props.getProperty(DERBY_SYSTEM_HOME);
//    System.out.printf("databaseHome: %s%n", databaseHome);
    @SuppressWarnings("HardcodedFileSeparator")
    File dataDir = new File(databaseHome);
//    System.out.printf("DataDir: %s%n", dataDir.getAbsolutePath());
    if (!dataDir.exists()) {
      //noinspection BooleanVariableAlwaysNegated
      boolean success = dataDir.mkdir();
      if (!success) {
        throw new IllegalStateException(String.format("Failed to create home directory: %s from %s",
            dataDir.getAbsolutePath(), databaseHome));
      }
    }

//    String connectionUrl = String.format("jdbc:derby:%s:skeleton", dataDir.getAbsolutePath());
  }

  @SuppressWarnings("NoopMethodInAbstractClass")
  @Override
  public void shutdown() { }
}
