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
import com.neptunedreams.framework.ErrorReport;
import com.neptunedreams.framework.data.ConnectionSource;
import com.neptunedreams.framework.data.Dao;
import com.neptunedreams.framework.data.DatabaseInfo;
import com.neptunedreams.framework.data.RecordModel;
import com.neptunedreams.framework.data.SearchOption;
import com.neptunedreams.framework.ui.RecordController;
import com.neptunedreams.framework.ui.StandardCaret;
import com.neptunedreams.framework.ui.TangoUtils;
import com.neptunedreams.skeleton.data.SiteField;
import com.neptunedreams.skeleton.data.sqlite.SQLiteInfo;
import com.neptunedreams.skeleton.gen.tables.records.SiteRecord;
import com.neptunedreams.skeleton.ui.RecordUI;
import com.neptunedreams.skeleton.ui.RecordView;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Skeleton Key Application
 * <p>
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
  // todo   all the records except the one I just added. Doing another find all finds everything.
  // TODO: Replace CountDownDoor with CyclicBarrier?
  // TODO: On changing sort column, search for previously selected card. (Search by id)
  // TODO: Redo layout: 
  // todo  1. Put Search Option Panel (in RecordUI) to the right of search field.
  // done  2. Dim instead of hide search options. (I had forgotten all about them!)
  // todo  3. Put search field options in a new sidebar. Allow show/hide.
  // todo  4. Add column header to sort buttons.
  
  // https://db.apache.org/ojb/docu/howtos/howto-use-db-sequences.html
  // https://db.apache.org/derby/docs/10.8/ref/rrefsqljcreatesequence.html 
  // https://db.apache.org/derby/docs/10.9/ref/rrefsistabssyssequences.html
  // Derby System Tables: https://db.apache.org/derby/docs/10.7/ref/rrefsistabs38369.html
  // .

//  private static final String DERBY_SYSTEM_HOME = "derby.system.home";
//  private Connection connection;
  
  /*
  TODO: Fix Arrow-Key Support
  I have implemented keyboard arrow support, but it has 2 problems.
  
  1. It works properly when a text component has the focus, in that it moves among the text characters. But the menu 
  flickers each time an arrow key is struck, even though it's not performing the menu function. Somehow, it knows to
  swallow the menu operation before it gets executed. 
  
  2. Holding down the arrow key will move the cards quickly, bypassing any swipe effect. To move and swipe at the same
  time, hold the mouse button down on one of the arrow keys.
  
  I'm not sure what the best approach is to fix each of these problems. One idea is to write an AWTEventListener to 
  respond to the arrow keystrokes, instead of using the menu accelerator mechanism. This would be a lot more work, 
  since I would need to take care that it only gets executed when the proper components have the focus. 
  
  Also, I should add support to the swipe feature to handle keystroke events, delaying processing until a swipe is 
  done. I'm not sure how these two solutions should be integrated together.
   */
  
  private RecordUI<@NonNull SiteRecord> mainPanel;
  //    org.jooq.util.JavaGenerator generator;
  private static JFrame frame = new JFrame("Skeleton");
  private final @NonNull DatabaseInfo info;
  private final @NonNull RecordController<SiteRecord, Integer, SiteField> controller;

  @SuppressWarnings("OverlyBroadThrowsClause")
  public static void main(String[] args) throws IOException, ClassNotFoundException {
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
    TangoUtils.replaceAllCarets(skeleton, StandardCaret::new);

    doExport(args, skeleton);
  }

  private static void doExport(final String[] args, final Skeleton skeleton) {
    if ((args.length > 0) && Objects.equals(args[0], "-export")) {
      try {
        // There has to be a delay, because there's a 1-second delay built into the launchInitialSearch() method,
        // and this needs to take place after that finishes, or we won't see any records. 
        Thread.sleep(1000); // Yeah, this is kludgy, but it's only for the export, which is only done in development.
      } catch (InterruptedException ignored) { }

      SwingUtilities.invokeLater(() -> {
        RecordModel<SiteRecord> model = skeleton.controller.getModel();
        // noinspection StringConcatenation
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

    info = new SQLiteInfo();
    try {
      info.init();
      final ConnectionSource connectionSource = info.getConnectionSource();
      Dao<SiteRecord, Integer, SiteField> dao = info.getDao(SiteRecord.class, connectionSource);
      SiteRecord dummyRecord = new SiteRecord(0, "", "", "", "");
      final RecordView<@NonNull SiteRecord> view = new RecordView.Builder<>(dummyRecord, SiteField.Source)
          .source  (SiteRecord::getSource,   SiteRecord::setSource)
          .id      (SiteRecord::getId,       SiteRecord::setId)
          .userName(SiteRecord::getUsername, SiteRecord::setUsername)
          .password(SiteRecord::getPassword, SiteRecord::setPassword)
          .notes   (SiteRecord::getNotes,    SiteRecord::setNotes)
          .withDao(dao)
          .withConstructor(this::recordConstructor)
          .build();
      controller = view.getController();
      final RecordModel<SiteRecord> model = controller.getModel();
      mainPanel = RecordUI.makeRecordUI(model, view, controller); // RecordUI launches the initial search

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
      shutDownDatabase(info);
      throw new IOException(e); // don't even open the window!
    }
  }

  private static void importFromFile(
      final Dao<? super SiteRecord, Integer, SiteField> dao, 
      RecordController<SiteRecord, Integer, ?> controller)
      throws SQLException, IOException, ClassNotFoundException {
    // noinspection StringConcatenation
    String exportPath = System.getProperty("user.home") + EXPORT_FILE;
    try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(exportPath))) {
      @SuppressWarnings("unchecked")
      RecordModel<SiteRecord> model = (RecordModel<SiteRecord>) objectInputStream.readObject();
      for (int ii=0; ii<model.getSize(); ++ii) {
        dao.insert(model.getRecordAt(ii));
      }
    }
    controller.findTextAnywhere("", SearchOption.findWhole);
  }

  @SuppressWarnings("unused")
  private @NonNull SiteRecord recordConstructor(@UnderInitialization Skeleton this) {
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
        shutDownDatabase(info);
      }
    };
  }

  private void shutDownDatabase(@UnknownInitialization Skeleton this, @NonNull DatabaseInfo databaseInfo) {
    databaseInfo.shutdown();
  }
}

/**/
