package com.neptunedreams.skeleton;

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
import com.neptunedreams.skeleton.ui.LFSizeAdjuster;
import com.neptunedreams.skeleton.ui.RecordUI;
import com.neptunedreams.skeleton.ui.RecordView;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Objects;
import java.util.prefs.Preferences;

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
  private static final String FONT_DELTA = "FONT_DELTA";
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
  
  private final RecordUI<@NonNull SiteRecord> mainPanel;
  //    org.jooq.util.JavaGenerator generator;
  public static final Preferences prefs = Preferences.userNodeForPackage(Skeleton.class);
  private final @NonNull DatabaseInfo info;
  private final @NonNull RecordController<SiteRecord, Integer, @NonNull SiteField> controller;
  @SuppressWarnings("OverlyBroadThrowsClause")
  public static void main(String[] args) throws IOException, ClassNotFoundException {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch(UnsupportedLookAndFeelException | InstantiationException | IllegalAccessException e){
      throw new IllegalStateException("Should not happen", e);
    }
    boolean doImport = (args.length > 0) && Objects.equals(args[0], "-import");
    int initialDelta = prefs.getInt(FONT_DELTA, 0);
    
    final Skeleton skeleton = makeMainFrame(doImport, initialDelta);
    TangoUtils.replaceAllCarets(skeleton, StandardCaret::new);

    doExport(args, skeleton);
    LFSizeAdjuster.instance.setRelaunch((delta) -> {
      try {
        makeMainFrame(false, delta);
      } catch (IOException | ClassNotFoundException e) {
        throw new IllegalStateException("Should not happen", e);
      }
    });
  }

  private static @Nullable JFrame frame;
  
  @SuppressWarnings("dereference.of.nullable") // I don't know why I'm getting this error! On statements marked // *
  private static @NonNull Skeleton makeMainFrame(final boolean doImport, int delta) throws IOException, ClassNotFoundException {
    LFSizeAdjuster.instance.setDelta(delta);
    Point priorLocation = null;
    if (frame != null) {
      priorLocation = frame.getLocation();
      frame.dispose(); // *
    }
    LFSizeAdjuster.instance.adjustLookAndFeel();
    frame = new JFrame("Skeleton");
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); // this one doesn't get a warning.
    if (priorLocation == null) {
      frame.setLocationByPlatform(true); // *
    } else {
      frame.setLocation(priorLocation); // *
    }
    Skeleton skeleton = new Skeleton(doImport);
    frame.add(skeleton.getPanel()); // *
    frame.pack(); // *
    frame.addWindowListener(skeleton.shutdownListener()); // *
//    UIMenus.Menu.installMenu(frame);
    skeleton.mainPanel.launchInitialSearch();
    frame.setVisible(true); // *
    prefs.putInt(FONT_DELTA, delta);
    return skeleton;
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
      Dao<SiteRecord, Integer, @NonNull SiteField> dao = info.getDao(SiteRecord.class, connectionSource);
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
      if (frame == null) { // frame should never be null here.
        throw new IllegalStateException("Null Frame!");
      } else {
        frame.addWindowListener(new WindowAdapter() {
          // Normally I override windowClosing, which can be cancelled. But I don't need to do that,
          // and it doesn't get sent when the window is disposed. This one does.
          @Override
          public void windowClosed(final WindowEvent e) {
            closeAndSave();
          }

          private void closeAndSave() {
            //noinspection ErrorNotRethrown
            try {
              if (view.saveOnExit()) {
                controller.getDao().insertOrUpdate(view.getCurrentRecord());
              }
            } catch (SQLException | RuntimeException | Error e1) {
              ErrorReport.reportException("Saving last change", e1);
            }
            //noinspection TooBroadScope
            StringBuilder builder = new StringBuilder();
            try {
              Collection<SiteRecord> allResults = controller.getDao().findAll(SiteField.Source, "");
              extracted(builder, allResults);
            } catch (SQLException ex) {
              ErrorReport.reportException("Saving All Records", ex);
              System.out.println("Error");
              ex.printStackTrace();
            }
          }
        });
      }

    } catch (SQLException e) {
      e.printStackTrace();
      shutDownDatabase(info);
      throw new IOException(e); // don't even open the window!
    }
  }

  private static void extracted(StringBuilder builder, Collection<SiteRecord> allResults) {
    //noinspection MagicCharacter,HardcodedLineSeparator
    final char lf = '\n';
    //noinspection UnnecessaryUnicodeEscape
    String delimiter = " \u205D ";
    for (SiteRecord siteRecord : allResults) {
      builder.append(siteRecord.getSource())
          .append(delimiter)
          .append(siteRecord.getUsername())
          .append(delimiter)
          .append(siteRecord.getPassword())
          .append(delimiter)
          .append(stripLf(siteRecord.getNotes()))
          .append(lf)
          .append(lf);
    }
    final File outputFile = new File(System.getProperty("user.home"), "Notes.txt");
    Path path = outputFile.toPath();

    try {
      //noinspection ResultOfMethodCallIgnored
      outputFile.createNewFile();
      Files.writeString(path, builder.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private static String stripLf(String txt) {
    StringBuilder builder = new StringBuilder(txt.trim());
    //noinspection HardcodedLineSeparator
    final String lineBreak = "\n";
    int lfSpot = builder.indexOf(lineBreak);
    while (lfSpot >= 0) {
      builder.replace(lfSpot, lfSpot+1, "\\n");
      lfSpot = builder.indexOf(lineBreak);
    }
    return builder.toString();
  }

//  public static void mainx(String[] args) {
//    List<SiteRecord> recordList = new LinkedList<>();
//    recordList.add(make("source1", "pw1", "un1", "Notes1"));
//    recordList.add(make("src2", "pw2", "un2", "Notes 2 Line 1\nNotes 2 line 2\nNotes 3 line 3\n\n\n"));
//    recordList.add(make("src3", "pw3", "un3", "Notes 3   "));
//    StringBuilder builder = new StringBuilder();
//    extracted(builder, recordList);
//    System.out.println(builder);
//    
//    recordList.add(make("source 4", "PW 4", "userName 4", "Notes 4"));
//    recordList.remove(1);
//    builder = new StringBuilder();
//    extracted(builder, recordList);
//    System.out.println("---");
//    System.out.println(builder);
//    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
//    StringSelection stringSelection = new StringSelection(builder.toString());
//    clipboard.setContents(stringSelection, stringSelection);
//  }
//  
//  private static SiteRecord make(String source, String password, String username, String notes) {
//    SiteRecord rd = new SiteRecord();
//    rd.setSource(source);
//    rd.setPassword(password);
//    rd.setUsername(username);
//    rd.setNotes(notes);
//    return rd;
//  }
//
  private static void importFromFile(
      final Dao<? super SiteRecord, Integer, @NonNull SiteField> dao, 
      RecordController<SiteRecord, Integer, ?> controller)
      throws SQLException, IOException, ClassNotFoundException {
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

/* Import Test Data:
test1Source test1Username test1 password
 testOne line of notes
test1 another line of notes  
  
test2Source test2@username.com test2password

test3 source test3 password

test4Source test4@username.com after test4Password

test5Source before test5@user.com test5Password

test6Source before test6@user.gov after test6Password
* */
