package com.neptunedreams.skeleton.data.derby;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

import com.neptunedreams.framework.data.AbstractDatabaseInfo;
import com.neptunedreams.framework.data.ConnectionSource;
import com.neptunedreams.framework.data.DBField;
import com.neptunedreams.framework.data.Dao;
import com.neptunedreams.skeleton.data.Record;
import org.jetbrains.annotations.NotNull;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/10/17
 * <p>Time: 11:24 PM
 *
 * @author Miguel Muñoz
 */
public class DerbyInfo extends AbstractDatabaseInfo {
  private static final String DERBY_SYSTEM_HOME = "derby.system.home";
  private static final Class<Record> RECORD_CLASS = Record.class;

  private DerbyDaoFactory derbyDaoFactory;
  
  DerbyInfo() {
    // noinspection HardcodedFileSeparator,HardCodedStringLiteral
    this("/.skeleton");
  }
  
  DerbyInfo(@NotNull String derbyHomDir) {
    super(derbyHomDir);
    final String homeDir = getHomeDir();
    System.setProperty(DERBY_SYSTEM_HOME, homeDir); // method.invocation.invalid: getHomeDir

//    init();
  }

  @Override
  public String getUrl() {
    //noinspection HardCodedStringLiteral
    return "jdbc:derby:skeleton;create=true;collation=TERRITORY_BASED:PRIMARY";
  }

  @Override
  public <T, PK, F extends DBField> Dao<T, PK, @NotNull F> getDao(Class<T> eClass, ConnectionSource source) {
    Dao<T, PK, @NotNull F> dao = derbyDaoFactory.getDao(eClass);
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
      //noinspection CallToDriverManagerGetConnection,JDBCResourceOpenedButNotSafelyClosed
      DriverManager.getConnection("jdbc:derby:;shutdown=true");
    } catch (SQLException e1) {
      if (!Objects.toString(e1.getMessage()).contains("Derby system shutdown.")) {
        e1.printStackTrace();
      }
    }
  }
}
