package com.neptunedreams.skeleton;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.Objects;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import com.ErrorReport;
import com.neptunedreams.skeleton.data.ConnectionSource;
import com.neptunedreams.skeleton.data.Dao;
import com.neptunedreams.skeleton.data.DatabaseInfo;
import com.neptunedreams.skeleton.data.SiteField;
import com.neptunedreams.skeleton.data.sqlite.SQLiteInfo;
import com.neptunedreams.skeleton.gen.tables.records.SiteRecord;
import com.neptunedreams.skeleton.ui.RecordController;
import com.neptunedreams.skeleton.ui.RecordModel;
import com.neptunedreams.skeleton.ui.RecordUI;
import com.neptunedreams.skeleton.ui.RecordView;
import com.neptunedreams.skeleton.ui.SearchOption;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Skeleton Key Application
 * <p/>
 * Optional Arguments: <br>
 *   -export Upon launching, export all data to an xml file. <br>
 *   -import Upon launching, if there is no data, import all data from the same .xml file that you previously exported. <br>
 * Neither of these options assumes long-term storage. They use serialization, so import should be done 
 * immediately after exporting and deleting the database.
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr", "HardCodedStringLiteral"})
public final class Skeleton extends JPanel
{
  @SuppressWarnings("HardcodedFileSeparator")
  private static final String EXPORT_FILE = "/.SkeletonData.serial";
  // Done: Write an import mechanism.
  // Done: Test packaging
  // Done: Test Bundling: https://github.com/federkasten/appbundle-maven-plugin
  // Done: Add an info line: 4/15 (25 total)
  // Done: Write a query thread to handle find requests.
  // Done: immediately resort when changing sort field.
  // Done: BUG: Fix finding no records.
  // Done: BUG: Fix search in field.
  // Done: disable buttons when nothing is found.
  // Done: QUESTION: Are we properly setting currentRecord after a find? for each find type? Before updating the screen?
  // Done: BUG: New database. Inserting and saving changed records is still buggy. (see prev note for hypothesis)
  // Done: Test boundary issues on insertion index.
  // Done: enable buttons on new record. ??
  // Done: Convert to jOOQ
  // Done: Add a getTotal method for info line.
  // Done: Figure out a better way to get the ID of a new record. Can we ask the sequencer?
  //       For accessing a sequencer, see https://stackoverflow.com/questions/5729063/how-to-use-sequence-in-apache-derby
  // Done: BUG: Search that produces no results gives the user a data-entry screen to doesn't get saved.
  // Done: BUG: Search that produces one result gives the user an entry screen that gets treated as a new record 
  // Done: BUG: Key Queue in QueuedTask never reads the keys it saves. Can we get rid of it?
  // TODO:  Fix bug on adding: If I add a record, then do a find all by hitting return in the find field, it finds
  // todo   all the records except the on I just added. Doing another find all finds everything.
  
  // https://db.apache.org/ojb/docu/howtos/howto-use-db-sequences.html
  // https://db.apache.org/derby/docs/10.8/ref/rrefsqljcreatesequence.html 
  // https://db.apache.org/derby/docs/10.9/ref/rrefsistabssyssequences.html
  // Derby System Tables: https://db.apache.org/derby/docs/10.7/ref/rrefsistabs38369.html
  // .

//  private static final String DERBY_SYSTEM_HOME = "derby.system.home";
//  private Connection connection;
  private RecordUI<SiteRecord> mainPanel;
  //    org.jooq.util.JavaGenerator generator;
  private static JFrame frame = new JFrame("Skeleton");
  private final @NonNull DatabaseInfo info;
  private final @NonNull RecordController<SiteRecord, Integer> controller;
  //  private RecordController<>

  @SuppressWarnings("OverlyBroadThrowsClause")
  public static void main(String[] args ) throws IOException, ClassNotFoundException {
    boolean doImport = (args.length > 0) && Objects.equals(args[0], "-import");
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setLocationByPlatform(true);
    final Skeleton skeleton = new Skeleton(doImport);
    frame.add(skeleton.getPanel());
    frame.pack();
    frame.addWindowListener(skeleton.shutdownListener());
//    UIMenus.Menu.installMenu(frame);
    skeleton.mainPanel.launchInitialSearch();
    frame.setVisible(true);

    doExport(args, skeleton);
  }

  private static void doExport(final String[] args, final Skeleton skeleton) {
    if ((args.length > 0) && Objects.equals(args[0], "-export")) {
      try {
        // There has to be a delay, because there's a 1-second delay built into the launchInitialSearch() method,
        // and this needs to take place after that finishes, or we won't see any records. 
        //noinspection MagicNumber
        Thread.sleep(1000); // Yeah, this is kludgy, but it's only for the export, which is only done in development.
      } catch (InterruptedException ignored) { }

      SwingUtilities.invokeLater(() -> {
        RecordModel<SiteRecord> model = skeleton.controller.getModel();
        //noinspection StringConcatenation,StringConcatenationMissingWhitespace
        String exportPath = System.getProperty("user.home") + EXPORT_FILE;
        System.err.printf("Exporting %d records to %s%n", model.getSize(), exportPath); // NON-NLS
        //noinspection OverlyBroadCatchBlock
        try (ObjectOutputStream bos = new ObjectOutputStream(new FileOutputStream(exportPath))) {
          bos.writeObject(model);
        } catch (IOException e) {
          ErrorReport.reportException("Error during export", e);
        }
        System.err.printf("Export done%n"); // NON-NLS
      });
    }
  }

  @SuppressWarnings("OverlyBroadThrowsClause")
  private Skeleton(boolean doImport) throws IOException, ClassNotFoundException {
    super();

    try {
      info = new SQLiteInfo();
      info.init();
      final ConnectionSource connectionSource = info.getConnectionSource();
      Dao<SiteRecord, Integer> dao = info.getDao(SiteRecord.class, connectionSource);
      SiteRecord dummyRecord = new SiteRecord(0, "", "", "", "");
      final RecordView<SiteRecord> view = new RecordView.Builder<>(dummyRecord, SiteField.Source)
          .source  (SiteRecord::getSource,   SiteRecord::setSource)
          .id      (SiteRecord::getId,       SiteRecord::setId)
          .userName(SiteRecord::getUsername, SiteRecord::setUsername)
          .password(SiteRecord::getPassword, SiteRecord::setPassword)
          .notes   (SiteRecord::getNotes,    SiteRecord::setNotes)
          .build();
      controller = new RecordController<>(
          dao, 
          view, 
          SiteField.Source,
          this::recordConstructor
      );
      view.setController(controller);
      final RecordModel<SiteRecord> model = controller.getModel();
      mainPanel = new RecordUI<>(model, view, controller); // RecordUI launches the initial search

      if ((model.getSize() == 1) && (model.getRecordAt(0).getId() == 0) && doImport) {
        importFromFile(dao, controller); // throws ClassNotFoundException
      }

      // Make sure you save the last change before shutting down.
      frame.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(final WindowEvent e) {
          //noinspection ErrorNotRethrown
          try {
            if (view.saveOnExit()) {
              assert controller != null;
              controller.getDao().insertOrUpdate(view.getCurrentRecord());
            }
          } catch (SQLException | RuntimeException | Error e1) {
            ErrorReport.reportException("Saving last change", e1);
          }
        }
      });

//      // Import from Derby
//      ObjectMapper objectMapper = new ObjectMapper();
//      final File file = new File(System.getProperty("user.home"), "skeletonRecords.json");
//      
//      FileInputStream inputStream = new FileInputStream(file);
//      InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
//      List<SiteRecord> recordList = objectMapper.readValue(reader, new TypeReference<List<SiteRecord>>() {});
//      for (SiteRecord siteRecord: recordList) {
////        System.out.println(siteRecord);
////        objectMapper.writeValueAsString(siteRecord); // Didn't work.
//        dao.insert(siteRecord);
//      }
    } catch (SQLException e) {
      e.printStackTrace();
      shutDownDatabase();
      throw new IOException(e); // don't even open the window!
    }
  }

  private static void importFromFile(
      final Dao<SiteRecord, Integer> dao, 
      RecordController<SiteRecord, Integer> controller)
      throws SQLException, IOException, ClassNotFoundException {
    //noinspection StringConcatenation,StringConcatenationMissingWhitespace
    String exportPath = System.getProperty("user.home") + EXPORT_FILE;
    try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(exportPath))) {
      @SuppressWarnings("unchecked")
      RecordModel<SiteRecord> model = (RecordModel<SiteRecord>) objectInputStream.readObject();
      for (int ii=0; ii<model.getSize(); ++ii) {
        dao.insert(model.getRecordAt(ii));
      }
    }
    controller.findTextAnywhere("", SearchOption.findExact);
  }

  @SuppressWarnings("unused")
  private SiteRecord recordConstructor(@UnderInitialization Skeleton this, Void ignored) {
    //noinspection ConstantConditions
    return new SiteRecord(0, "", "", "", "");
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
