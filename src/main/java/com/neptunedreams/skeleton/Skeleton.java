package com.neptunedreams.skeleton;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import com.ErrorReport;
import com.neptunedreams.skeleton.data.Dao;
import com.neptunedreams.skeleton.data.Record;
import com.neptunedreams.skeleton.data.RecordDao;
import com.neptunedreams.skeleton.ui.RecordController;
import com.neptunedreams.skeleton.ui.RecordUI;
import com.neptunedreams.skeleton.ui.RecordView;

/**
 * Hello world!
 *
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr", "HardCodedStringLiteral"})
public final class Skeleton extends JPanel
{
  private static final String DERBY_SYSTEM_HOME = "derby.system.home";
  // Done: Write an import mechanism.
  // Done: Test packaging
  // todo: Test Bundling: https://github.com/federkasten/appbundle-maven-plugin
  // Done: Add an info line: 4/15 (25 total)
  // Todo: Write a query thread to handle find requests.
  // Todo: immediately resort when changing sort field.
  // Done: BUG: Fix finding no records.
  // TODO: BUG: Fix search in field.
  // Done: disable buttons when nothing is found.
  // Done: QUESTION: Are we properly setting currentRecord after a find? for each find type? Before updating the screen?
  // Done: BUG: New database. Inserting and saving changed records is still buggy. (see prev note for hypothesis)
  // Done: Test boundary issues on insertion index.
  // Todo: enable buttons on new record. ??
  // Todo: Convert to jOOQ
  // Done: Add a getTotal method for info line.
  // Todo: Figure out a better way to get the ID of a new record. Can we ask the sequencer?

  private Connection connection;
  private JPanel mainPanel;
  private static JFrame frame;

  public static void main(String[] args ) throws IOException {
    frame = new JFrame("Skeleton");
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setLocationByPlatform(true);
    frame.add(new Skeleton().getPanel());
    frame.pack();
    frame.addWindowListener(shutdownListener());
//    UIMenus.Menu.installMenu(frame);
    frame.setVisible(true);
  }
  
  private Skeleton() throws IOException {
    super();
    String userHome = System.getProperty("user.home");
    System.out.printf("user.home: %s%n", userHome);
    //noinspection StringConcatenation,HardcodedFileSeparator
    final String derbySystemHome = userHome + "/.skeleton";
    System.setProperty(DERBY_SYSTEM_HOME, derbySystemHome);

//    Properties props = new Properties();
//    props.setProperty(DERBY_SYSTEM_HOME, derbySystemHome);
    init(derbySystemHome);
    try {
      Dao<?> dao = connect();
      final RecordView view = new RecordView();
      @SuppressWarnings("unchecked") final Dao<Record> recordDao = (Dao<Record>) dao;
      RecordController controller = new RecordController(recordDao, view);
      view.setController(controller);
      mainPanel = new RecordUI(controller.getModel(), view, controller);
      controller.findTextAnywhere("");
      controller.getModel().setTotalFromSize();

      // Make sure you save the last change before shutting down.
      frame.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(final WindowEvent e) {
          try {
            view.saveOnExit();
          } catch (SQLException e1) {
            ErrorReport.reportException("Saving last change", e1);
          }
        }
      });
      
//      // Export to JSON
//      List<Record> fullList = new LinkedList<>();
//      
//      ObjectMapper objectMapper = new ObjectMapper();
//      int size = controller.getModel().getSize();
//      for (int ii=0; ii<size; ++ii) {
//        fullList.add(controller.getModel().getRecordAt(ii));
//      }
//      String jsonString = objectMapper.writeValueAsString(fullList);
//      FileOutputStream fs = new FileOutputStream(new File(System.getProperty("user.home"), "skeletonRecords.json"));
//      OutputStreamWriter writer = new OutputStreamWriter(fs, "UTF-8");
////      FileWriter writer = new FileWriter(new File(System.getProperty("user.home"), "skeletonRecords.json"));
////      Writer w = new 
//      writer.write(jsonString);
//      writer.close();
//      System.out.println(jsonString);
      
    } catch (SQLException e) {
      e.printStackTrace();
      shutDownDatabase();
      throw new IOException(e); // don't even open the window!
    }
  }
  
  private JPanel getPanel() { return mainPanel; }

  @SuppressWarnings("HardCodedStringLiteral")
  private void init(String derbyHome) throws IOException {
//    final String derbyHome = System.getProperty("derby.system.home");
//    final String derbyHome = props.getProperty(DERBY_SYSTEM_HOME);
//    System.out.printf("derbyHome: %s%n", derbyHome);
    @SuppressWarnings("HardcodedFileSeparator")
    File dataDir = new File("/", derbyHome);
    System.out.printf("DataDir: %s%n", dataDir.getAbsolutePath());
    if (!dataDir.exists()) {
      //noinspection BooleanVariableAlwaysNegated
      boolean success = dataDir.mkdir();
      if (!success) {
        throw new IllegalStateException("Failed to create base directory");
      }
      final File propsFile = new File(dataDir, "derby.properties");
      if (!propsFile.exists()) {
        System.out.println("Creating props");
        //noinspection TooBroadScope,MismatchedQueryAndUpdateOfCollection
        Properties properties = new Properties();
        try (FileWriter writer = new FileWriter(propsFile)) {
          properties.store(writer, "");
        }
      }
    }

//    String connectionUrl = String.format("jdbc:derby:%s:skeleton", dataDir.getAbsolutePath());
  }

  private Dao<?> connect() throws SQLException {
    String connectionUrl = "jdbc:derby:skeleton;create=true;collation=TERRITORY_BASED:PRIMARY";
    System.out.printf("URL: %s%n", connectionUrl);
//    DataSource dataSource =
    try {
      //noinspection CallToDriverManagerGetConnection
      connection = DriverManager.getConnection(connectionUrl); // , props);
    } catch (SQLException e) {
      e.printStackTrace();
//      connection = DriverManager.getConnection(connectionUrl);
    }
    Map<String, Class<?>> typeMap =  connection.getTypeMap();
    int size = typeMap.size();
    System.out.printf("Total of %d types:%n", size);
    for (String s: typeMap.keySet()) {
      System.out.printf("%s: %s%n", s, typeMap.get(s));
    }

    ConnectionSource connectionSource = () -> connection;
    RecordDao recordDao = new RecordDao(connectionSource);

//    String drop = "DROP TABLE record";
//    PreparedStatement statement = connection.prepareStatement(drop);
//    statement.execute();
    
    recordDao.createTableIfNeeded();
    return recordDao;
  }

  @SuppressWarnings({"HardCodedStringLiteral", "unused", "HardcodedLineSeparator"})
  private void testDao(RecordDao dao) throws SQLException {
    Record record1 = new Record("TestSite", "testName", "testPw", "testNotes\nNote line 2\nNoteLine 3");
    dao.save(record1);
    Record record2 = new Record("t2Site", "t2User", "t2Pw", "t2Note");
    dao.save(record2);

    Collection<Record> allRecords = dao.getAll(Record.FIELD.SOURCE);
    System.out.printf("getAll() returned %d records, expecting 2%n", allRecords.size());

    //noinspection UnusedAssignment
    Collection<Record> foundRecords = dao.getAll(Record.FIELD.SOURCE);
    foundRecords = dao.find("line", Record.FIELD.SOURCE);
    System.out.printf("find(line) returned %d records, expecting 1%n", foundRecords.size());
    record1 = foundRecords.iterator().next();
    
    // Test update
    final String revisedName = "revisedName";
    String originalName = record1.getUserName();
    record1.setUserName(revisedName);
    System.out.printf("Changing Name from %s to %s%n", originalName, revisedName);
    dao.save(record1);
    foundRecords = dao.find("line", Record.FIELD.SOURCE);
    System.out.printf("Found %d records%n", foundRecords.size());
    Record revisedRecord = foundRecords.iterator().next();
    testShowRecord(revisedRecord);

    int deletedId = revisedRecord.getId();
    dao.delete(revisedRecord);
    allRecords = dao.getAll(Record.FIELD.SOURCE);
    System.out.printf("Total of %d records after deleting id %d%n", allRecords.size(), deletedId);
    Record remainingRecord = allRecords.iterator().next();
    dao.delete(remainingRecord);
    allRecords = dao.getAll(Record.FIELD.SOURCE);
    System.out.printf("Total of %d records after deleting 1%n", allRecords.size());
  }

  private void testShowRecord(final Record record) {
    System.out.printf("Record: %n  id: %d%n  sr: %s%n  un: %s%n  pw: %s%n  Nt: %s%n",
        record.getId(), record.getSource(), record.getUserName(),
        record.getPassword(), record.getNotes());
  }

  private static WindowListener shutdownListener() {
    return new WindowAdapter() {
      @Override
      public void windowClosed(final WindowEvent e) {
        shutDownDatabase();
      }
    };
  }

  private static void shutDownDatabase() {
    try {
      // NO need to safely close. We're shutting down!
      //noinspection CallToDriverManagerGetConnection,JDBCResourceOpenedButNotSafelyClosed
      DriverManager.getConnection("jdbc:derby:;shutdown=true");
    } catch (SQLException e1) {
      if (!e1.getMessage().contains("Derby system shutdown.")) {
        e1.printStackTrace();
      }
    }
  }
}

/**/
