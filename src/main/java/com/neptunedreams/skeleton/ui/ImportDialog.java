package com.neptunedreams.skeleton.ui;

import com.neptunedreams.framework.data.Dao;
import com.neptunedreams.skeleton.data.Record;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Window;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/3/17
 * <p>Time: 9:51 PM
 *
 * @author Miguel Muñoz
 */
@SuppressWarnings("StringConcatenation")
public final class ImportDialog extends JDialog {
  private final Dao<Record, ?, ?> recordDao;
  private ImportDialog(Window parent, Dao<Record, ?, ?> dao) {
    super(parent, ModalityType.DOCUMENT_MODAL);
    recordDao = dao;
//    build();
  }
  
  static ImportDialog build(Window parent, Dao<Record, ?, ?> dao) {
    ImportDialog importDialog = new ImportDialog(parent, dao);
    importDialog.build();
    return importDialog;
  }

  private void build(ImportDialog this) {
    //noinspection MagicNumber
    JTextArea importArea = new JTextArea(40, 60);
    JScrollPane scrollPane = new JScrollPane(importArea, 
        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(scrollPane, BorderLayout.CENTER);
    //noinspection HardCodedStringLiteral
    JButton load = new JButton("Load");
    getContentPane().add(load, BorderLayout.PAGE_END);
    pack();
    importArea.setLineWrap(true);
    importArea.setWrapStyleWord(true);
    
    load.addActionListener((e -> doLoad(importArea.getText())));
  }
  
  private void doLoad(final String rawText) {
    String text = rawText + "\n\n"; // Need to have a blank last line, or we lose the last entry.
    try (BufferedReader reader = new BufferedReader(new StringReader(text))) {
      String line = "";
      ImportRecord importRecord = null;
      while (line != null) {
        if (line.isEmpty()) {
          if (importRecord != null) {
            importRecord.updateRecord();
          }
          importRecord = null;
        } else {
          if (importRecord == null) {
            importRecord = new ImportRecord();
          }
          importRecord.addString(line.trim());
        }
        line = reader.readLine();
      }
//    } catch (IOException | SQLException e) {
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  private class ImportRecord {
    public static final char SPACE = ' ';
    private final List<String> stringList = new LinkedList<>();
    
    public void addString(String s) {
      stringList.add(s);
    }
    
    public void updateRecord() {
      Record newRecord = readRecord(stringList);
//        printRecord(newRecord);
      try {
        recordDao.update(newRecord);
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

    @NonNull
    private Record readRecord(List<String> sList) {
      String pw;
      String userName;
      String source;
      String notes;
      final String firstLine = sList.get(0).trim();

      // If there is a word with an @, that's the username. Everything after it is the password, and
      // everything before it is the source.
      //noinspection MagicCharacter
      int atSpot = firstLine.indexOf('@');

      if (atSpot >= 0) {
        int userNameEnd = firstLine.indexOf(SPACE, atSpot);
        if (userNameEnd < 0) {
          userNameEnd = firstLine.length() - 1;
        }
        int userNameStart = firstLine.substring(0, atSpot).lastIndexOf(SPACE)+1;
        userName = firstLine.substring(userNameStart, userNameEnd).trim();
        pw = firstLine.substring(userNameEnd).trim();
        source = firstLine.substring(0, userNameStart).trim();
      } else {
        
        // No @ character found, so the password is the last word, the username is the previous word,
        // and the source is everything before that.
        int pwStart = firstLine.lastIndexOf(SPACE) + 1;
        pw = firstLine.substring(pwStart);
        String remainder = firstLine.substring(0, pwStart).trim();
        int unStart = remainder.lastIndexOf(SPACE) + 1;
        userName = remainder.substring(unStart);
        source = remainder.substring(0, unStart).trim();
        if (source.isEmpty()) {
          source = userName;
          userName = "";
        }
      }

      StringBuilder buffer = new StringBuilder();
      for (int i = 1; i < sList.size(); ++i) {
        //noinspection MagicCharacter
        buffer
            .append(sList.get(i).trim())
            .append('\n');
      }
      notes = buffer.toString().trim();
      Record newRecord = new Record();

      newRecord.setPassword(pw);
      newRecord.setUserName(userName);
      newRecord.setSource(source);
      newRecord.setNotes(notes);
      return newRecord;
    }
  }

//  public static void run() {
//
//    @SuppressWarnings("HardcodedLineSeparator") @NonNls
//    String stringData =
//        "lollygagging me@mydomain.com swfes\n" +
//        "Lots of Ads.\n" +
//        '\n' +
//        '\n' +
//        "aol.com miguelMunoz myPw\n" +
//        '\n' +
//        "stackOverflow other stack exchange sites, too. MiguelMunoz soPassword\n" +
//        '\n' +
//        "fiddleSticks.com: fsPw\n" +
//        "fs Line 2\n" +
//        "fs Line 3\n" +
//        "fs Line 4\n" +
//        "fs Last Line\n" +
//        '\n' +
//        "you@yourDomain.net yourPW\n" +
//        '\n' +
//        "SingleWord\n" +
//        '\n' +
//        '\n' +
//        '\n' +
//        "lastLine pwerd";
//
////    doLoad(stringData); // Need to make ImportRecord static to run this.
//
//  }
//
//  private static void printRecord(Record record) {
//    System.out.printf("Record: %n"); // NON-NLS
//    System.out.printf("source:   %s%n", record.getSource()); // NON-NLS
//    System.out.printf("UserName: %s%n", record.getUserName()); // NON-NLS
//    System.out.printf("Password: %s%n", record.getPassword()); // NON-NLS
//    System.out.printf("Notes:%n%s%n%n%n", record.getNotes()); // NON-NLS
//  }
}
