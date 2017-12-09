package com.neptunedreams.skeleton;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.sql.SQLException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import com.ErrorReport;
import com.neptunedreams.skeleton.data.ConnectionSource;
import com.neptunedreams.skeleton.data.Dao;
import com.neptunedreams.skeleton.data.DatabaseInfo;
import com.neptunedreams.skeleton.data.RecordField;
import com.neptunedreams.skeleton.data.sqlite.SQLiteInfo;
import com.neptunedreams.skeleton.gen.tables.records.RecordRecord;
import com.neptunedreams.skeleton.ui.RecordController;
import com.neptunedreams.skeleton.ui.RecordUI;
import com.neptunedreams.skeleton.ui.RecordView;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Hello world!
 *
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr", "HardCodedStringLiteral"})
public final class Skeleton extends JPanel
{
  private static final int DUMMY_ID = -999;
  // Done: Write an import mechanism.
  // Done: Test packaging
  // Done: Test Bundling: https://github.com/federkasten/appbundle-maven-plugin
  // Done: Add an info line: 4/15 (25 total)
  // Done: Write a query thread to handle find requests.
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
  // TODO: Figure out a better way to get the ID of a new record. Can we ask the sequencer?
  // Todo  For accessing a sequencer, see https://stackoverflow.com/questions/5729063/how-to-use-sequence-in-apache-derby
  // TODO: BUG: Search that produces no results gives the user a data-entry screen to doesn't get saved.
  // TODO: BUG: Search that produces one result gives the user an entry screen that gets treated as a new record 
  // TODO: BUG: Key Queue never reads the keys it saves. Can we get rid of it?
  
  // https://db.apache.org/ojb/docu/howtos/howto-use-db-sequences.html
  // https://db.apache.org/derby/docs/10.8/ref/rrefsqljcreatesequence.html 
  // https://db.apache.org/derby/docs/10.9/ref/rrefsistabssyssequences.html
  // Derby System Tables: https://db.apache.org/derby/docs/10.7/ref/rrefsistabs38369.html
  // .

//  private static final String DERBY_SYSTEM_HOME = "derby.system.home";
//  private Connection connection;
  private JPanel mainPanel;
  //    org.jooq.util.JavaGenerator generator;
  private static JFrame frame = new JFrame("Skeleton");
  private final @NonNull DatabaseInfo info;

  public static void main(String[] args ) throws IOException {
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setLocationByPlatform(true);
    final Skeleton skeleton = new Skeleton();
    frame.add(skeleton.getPanel());
    frame.pack();
    frame.addWindowListener(skeleton.shutdownListener());
//    UIMenus.Menu.installMenu(frame);
    frame.setVisible(true);
  }
  
  private Skeleton() throws IOException {
    super();
//    String userHome = System.getProperty("user.home");
////    System.out.printf("user.home: %s%n", userHome);
//    //noinspection StringConcatenation,HardcodedFileSeparator
//    final String derbySystemHome = userHome + "/.skeleton";

//    Properties props = new Properties();
//    props.setProperty(DERBY_SYSTEM_HOME, derbySystemHome);
//    DatabaseInfo info = new DerbyInfo();

//    String userHome = System.getProperty("user.home");
//    //noinspection StringConcatenation,HardcodedFileSeparator
//    final String derbySystemHome = userHome + "/.skeleton";
//    System.setProperty(DERBY_SYSTEM_HOME, derbySystemHome);
//
////    Properties props = new Properties();
////    props.setProperty(DERBY_SYSTEM_HOME, derbySystemHome);
//    init(derbySystemHome);

    try {
      info = new SQLiteInfo();
      info.init();
      final ConnectionSource connectionSource = info.getConnectionSource();
      Dao<RecordRecord, Integer> dao = info.getDao(RecordRecord.class, connectionSource);
      RecordRecord dummyRecord = new RecordRecord(DUMMY_ID, "D", "D", "D", "D");
      final RecordView<RecordRecord> view = new RecordView.Builder<>(dummyRecord, RecordField.SOURCE)
          .source  (RecordRecord::getSource,   RecordRecord::setSource)
          .id      (RecordRecord::getId,       RecordRecord::setId)
          .userName(RecordRecord::getUsername, RecordRecord::setUsername)
          .password(RecordRecord::getPassword, RecordRecord::setPassword)
          .notes   (RecordRecord::getNotes,    RecordRecord::setNotes)
          .build();
      @SuppressWarnings("unchecked") 
      RecordController<RecordRecord, Integer> controller = new RecordController<>(
          dao, 
          view, 
          RecordField.SOURCE,
          this::recordConstructor
      );
      view.setController(controller);
      mainPanel = new RecordUI<>(controller.getModel(), view, controller);
      controller.findTextAnywhere("");
      controller.getModel().setTotalFromSize();

      // Make sure you save the last change before shutting down.
      frame.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(final WindowEvent e) {
          try {
            // TODO: Fix this. We save twice, once in saveOnExit(), once here!
            if (view.saveOnExit()) {
              controller.getDao().insertOrUpdate(view.getCurrentRecord());
            }
          } catch (SQLException e1) {
            ErrorReport.reportException("Saving last change", e1);
          }
        }
      });
    } catch (SQLException e) {
      e.printStackTrace();
      shutDownDatabase();
      throw new IOException(e); // don't even open the window!
    }
  }

  @SuppressWarnings("unused")
  private RecordRecord recordConstructor(@UnknownInitialization Skeleton this, Void ignored) {
    //noinspection ConstantConditions
    return new RecordRecord(0, "", "", "", "");
  }
  
  private JPanel getPanel() { return mainPanel; }

//  private Dao<?> connect(DatabaseInfo info) throws SQLException {
//    String connectionUrl = info.getUrl();
//    System.out.printf("URL: %s%n", connectionUrl);
//      //noinspection CallToDriverManagerGetConnection
//    try (Connection connection = DriverManager.getConnection(connectionUrl)) { // , props);
////    Map<String, Class<?>> typeMap =  connection.getTypeMap();
////    int size = typeMap.size();
////    System.out.printf("Total of %d types:%n", size);
////    for (String s: typeMap.keySet()) {
////      System.out.printf("%s: %s%n", s, typeMap.get(s));
////    }
//
//      ConnectionSource connectionSource = () -> connection;
//      Dao<Record> recordDao = info.getDao(Record.class, connectionSource);
//
////    String drop = "DROP TABLE record";
////    PreparedStatement statement = connection.prepareStatement(drop);
////    statement.execute();
//
//      recordDao.createTableIfNeeded();
//      return recordDao;
//    }
//  }

  private WindowListener shutdownListener() {
    return new WindowAdapter() {
      @Override
      public void windowClosed(final WindowEvent e) {
        shutDownDatabase();
      }
    };
  }

  private void shutDownDatabase(@UnknownInitialization Skeleton this) {
    assert info != null;
    info.shutdown();
  }
}

/**/
