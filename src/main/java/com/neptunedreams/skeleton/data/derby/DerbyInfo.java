package com.neptunedreams.skeleton.data.derby;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;
import com.neptunedreams.skeleton.data.AbstractDatabaseInfo;
import com.neptunedreams.skeleton.data.ConnectionSource;
import com.neptunedreams.skeleton.data.Dao;
import com.neptunedreams.skeleton.data.Record;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/10/17
 * <p>Time: 11:24 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public class DerbyInfo extends AbstractDatabaseInfo {
  private static final String DERBY_SYSTEM_HOME = "derby.system.home";
  private static final Class<Record> RECORD_CLASS = Record.class;

  private DerbyDaoFactory derbyDaoFactory;
  
  DerbyInfo() throws SQLException, IOException {
    //noinspection StringConcatenation,HardcodedFileSeparator,HardCodedStringLiteral
    this("/.skeleton");
  }
  
  @SuppressWarnings({"initialization.fields.uninitialized","method.invocation.invalid"}) // Constructor doesn't initialize derbyDaoFactory
  DerbyInfo(@NonNull String derbyHomDir) throws SQLException, IOException {
    super(derbyHomDir);
    System.setProperty(DERBY_SYSTEM_HOME, getHomeDir()); // method.invocation.invalid: getHomeDir

//    init();
  }

  @Override
  public String getUrl() {
    //noinspection HardCodedStringLiteral
    return "jdbc:derby:skeleton;create=true;collation=TERRITORY_BASED:PRIMARY";
  }

  @Override
  public <T, PK> Dao<T, PK> getDao(Class<T> eClass, ConnectionSource source) {
    Dao<T, PK> dao = derbyDaoFactory.getDao(eClass);
    if (dao == null) {
      throw new IllegalArgumentException(eClass.toString());
    }
    return dao;
  }

  @Override
  public Class<?> getRecordClass() {
    return RECORD_CLASS;
  }

  @Override
  public void init() throws IOException, SQLException {
    String dataDir = getHomeDir();
    final File propsFile = new File(dataDir, "derby.properties");
    if (!propsFile.exists()) {
      //noinspection UseOfSystemOutOrSystemErr
      System.out.println("Creating props");
      //noinspection TooBroadScope,MismatchedQueryAndUpdateOfCollection
      Properties properties = new Properties();
      try (FileWriter writer = new FileWriter(propsFile)) {
        properties.store(writer, "");
      }
    }
    initialize();
    derbyDaoFactory = new DerbyDaoFactory(getConnectionSource());
  }

  @Override
  public boolean isCreateSchemaAllowed() {
    return false;
  }

  @Override
  public void createSchema() {
    // just temporary. We should probably be able to create this.
    //noinspection ProhibitedExceptionThrown
    throw new RuntimeException("Can't Create Schema");
  }

  @Override
  public void shutdown() {
    try {
      // NO need to safely close. We're shutting down!
      //noinspection CallToDriverManagerGetConnection,JDBCResourceOpenedButNotSafelyClosed,resource
      DriverManager.getConnection("jdbc:derby:;shutdown=true");
    } catch (SQLException e1) {
      if (!Objects.toString(e1.getMessage()).contains("Derby system shutdown.")) {
        e1.printStackTrace();
      }
    }
  }
}
